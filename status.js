(function () {
  "use strict";

  const modules = (window.BreatheModules = window.BreatheModules || {});
  const STORAGE_KEY = "breathe_status";
  const SERVER_URL_KEY = "breathe_server_url";
  const JWT_KEY = "breathe_jwt";
  const ALLOWED_STATUSES = ["green", "yellow", "red"];
  const MAX_RECONNECT_DELAY_MS = 30000;
  const PARTNER_NOTIFICATION_DEDUPE_MS = 2500;

  let panelElement = null;
  let nudgeElement = null;
  let statusButtons = {};
  let statusDotCallback = null;
  let currentStatus = null;
  let partnerStatus = null;
  let partnerEvent = null;
  let syncSocket = null;
  let reconnectTimerId = null;
  let reconnectAttempt = 0;
  let hasStorageListener = false;
  let lastPartnerNotificationKey = "";
  let lastPartnerNotificationAt = 0;

  function getStorage() {
    if (typeof chrome === "undefined") {
      return null;
    }
    return chrome.storage && chrome.storage.local ? chrome.storage.local : null;
  }

  function isStatus(value) {
    return ALLOWED_STATUSES.includes(value);
  }

  function normalizePartnerEvent(value) {
    return typeof value === "string" ? value.trim().toLowerCase() : "";
  }

  function getPartnerSessionEventLabel(eventName) {
    const normalized = normalizePartnerEvent(eventName);
    const labels = {
      timeout: "a timeout",
      calm: "a calm session",
      peace: "a session",
    };
    return labels[normalized] || "a session";
  }

  function normalizeServerUrl(value) {
    if (typeof value !== "string") {
      return "";
    }
    return value.trim().replace(/\/+$/, "");
  }

  function isValidHttpUrl(value) {
    try {
      const parsed = new URL(value);
      return parsed.protocol === "http:" || parsed.protocol === "https:";
    } catch (_error) {
      return false;
    }
  }

  function notifyStatusDot() {
    if (modules.ui && typeof modules.ui.setPartnerStatus === "function") {
      modules.ui.setPartnerStatus(partnerStatus, partnerEvent);
    }

    if (typeof statusDotCallback === "function") {
      statusDotCallback({
        ownStatus: currentStatus,
        partnerStatus,
        partnerEvent,
      });
    }
  }

  function applyVisualState() {
    ALLOWED_STATUSES.forEach((status) => {
      const button = statusButtons[status];
      if (!button) {
        return;
      }
      button.classList.toggle("is-selected", status === currentStatus);
    });

    if (nudgeElement) {
      nudgeElement.textContent =
        currentStatus === "red"
          ? "You are in red. Consider using Timeout for a reset."
          : "";
      nudgeElement.classList.toggle("is-visible", currentStatus === "red");
    }

    notifyStatusDot();
  }

  function persistStatus(value) {
    const storage = getStorage();
    if (!storage) {
      return Promise.resolve();
    }

    return new Promise((resolve) => {
      try {
        storage.set({ [STORAGE_KEY]: value }, () => {
          resolve();
        });
      } catch (e) {
        resolve();
      }
    });
  }

  function loadSyncConfig() {
    const storage = getStorage();
    if (!storage) {
      return Promise.resolve(null);
    }

    return new Promise((resolve) => {
      try {
        storage.get([SERVER_URL_KEY, JWT_KEY], (result) => {
          if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
            resolve(null);
            return;
          }

          const serverUrl = normalizeServerUrl(result && result[SERVER_URL_KEY]);
          const token = typeof (result && result[JWT_KEY]) === "string" ? result[JWT_KEY].trim() : "";

          if (!serverUrl || !token || !isValidHttpUrl(serverUrl)) {
            resolve(null);
            return;
          }

          resolve({ serverUrl, token });
        });
      } catch (e) {
        resolve(null);
      }
    });
  }

  function buildApiUrl(serverUrl, path) {
    const baseUrl = normalizeServerUrl(serverUrl);
    return `${baseUrl}${path}`;
  }

  function buildWebSocketUrl(serverUrl, token) {
    const parsed = new URL(normalizeServerUrl(serverUrl));
    parsed.protocol = parsed.protocol === "https:" ? "wss:" : "ws:";
    parsed.pathname = "/ws";
    parsed.search = "";
    parsed.searchParams.set("token", token);
    return parsed.toString();
  }

  async function fetchWithAuth(path, options) {
    const config = await loadSyncConfig();
    if (!config) {
      return null;
    }

    const headers = {
      Authorization: `Bearer ${config.token}`,
      "Content-Type": "application/json",
      ...(options && options.headers ? options.headers : {}),
    };

    try {
      const response = await fetch(buildApiUrl(config.serverUrl, path), {
        ...(options || {}),
        headers,
      });

      if (!response.ok) {
        return null;
      }

      if (response.status === 204) {
        return null;
      }

      try {
        return await response.json();
      } catch (_jsonError) {
        return null;
      }
    } catch (_fetchError) {
      return null;
    }
  }

  function setPartnerStatus(value, eventName) {
    const nextStatus = isStatus(value) ? value : null;
    const nextEvent = typeof eventName === "string" ? eventName : null;

    if (partnerStatus === nextStatus && partnerEvent === nextEvent) {
      return;
    }

    partnerStatus = nextStatus;
    partnerEvent = nextEvent;
    notifyStatusDot();
  }

  async function syncStatusToServer(value) {
    if (!isStatus(value)) {
      return;
    }

    await fetchWithAuth("/api/status", {
      method: "POST",
      body: JSON.stringify({ color: value }),
    });
  }

  async function fetchInitialPartnerStatus() {
    const payload = await fetchWithAuth("/api/status", { method: "GET" });
    if (!payload || !payload.partner) {
      return;
    }
    setPartnerStatus(payload.partner.color, payload.partner.event || null);
  }

  function clearReconnectTimer() {
    if (!reconnectTimerId) {
      return;
    }
    window.clearTimeout(reconnectTimerId);
    reconnectTimerId = null;
  }

  function closeSocket() {
    clearReconnectTimer();
    if (!syncSocket) {
      return;
    }

    syncSocket.onopen = null;
    syncSocket.onmessage = null;
    syncSocket.onclose = null;
    syncSocket.onerror = null;
    syncSocket.close();
    syncSocket = null;
  }

  function scheduleReconnect() {
    if (reconnectTimerId) {
      return;
    }

    const delay = Math.min(1000 * Math.pow(2, reconnectAttempt), MAX_RECONNECT_DELAY_MS);
    reconnectAttempt += 1;
    reconnectTimerId = window.setTimeout(() => {
      reconnectTimerId = null;
      openStatusSocket();
    }, delay);
  }

  async function openStatusSocket() {
    const config = await loadSyncConfig();
    if (!config) {
      closeSocket();
      setPartnerStatus(null);
      reconnectAttempt = 0;
      return;
    }

    if (syncSocket && (syncSocket.readyState === WebSocket.OPEN || syncSocket.readyState === WebSocket.CONNECTING)) {
      return;
    }

    let socket;
    try {
      socket = new WebSocket(buildWebSocketUrl(config.serverUrl, config.token));
    } catch (_error) {
      scheduleReconnect();
      return;
    }

    syncSocket = socket;

    socket.onopen = () => {
      reconnectAttempt = 0;
      clearReconnectTimer();
      fetchInitialPartnerStatus();
    };

    socket.onmessage = (event) => {
      let payload;
      try {
        payload = JSON.parse(event.data);
      } catch (_parseError) {
        return;
      }

      if (!payload || typeof payload.type !== "string") {
        return;
      }

      if (payload.type === "STATUS_UPDATE") {
        setPartnerStatus(payload.color, payload.event || null);
        return;
      }

      if (payload.type === "PARTNER_EVENT") {
        setPartnerStatus(payload.color || partnerStatus, payload.event || null);
        return;
      }

      if (payload.type === "SESSION_STARTED") {
        const eventName = payload.featureUsed === "timeout" ? "timeout" : "calm";
        setPartnerStatus(payload.color || partnerStatus, eventName);
        return;
      }

       if (payload.type === "SESSION_ENDED") {
         const eventName = normalizePartnerEvent(payload.event) || "peace";
         setPartnerStatus(payload.color || partnerStatus, eventName);

         if (modules.ui && typeof modules.ui.showPartnerNotification === "function") {
           const eventLabel = getPartnerSessionEventLabel(eventName);
           const notificationKey = `${payload.type}:${eventName}`;
           const now = Date.now();
           const recentlyNotified =
             notificationKey === lastPartnerNotificationKey &&
             now - lastPartnerNotificationAt < PARTNER_NOTIFICATION_DEDUPE_MS;

           if (!recentlyNotified) {
             modules.ui.showPartnerNotification(`Partner finished ${eventLabel} and is ready to connect`);
             lastPartnerNotificationKey = notificationKey;
             lastPartnerNotificationAt = now;
           }
         }

         return;
       }
    };

    socket.onclose = () => {
      if (syncSocket === socket) {
        syncSocket = null;
      }
      scheduleReconnect();
    };

    socket.onerror = () => {
      try {
        socket.close();
      } catch (_closeError) {
        scheduleReconnect();
      }
    };
  }

  function ensureStorageSyncListener() {
    if (hasStorageListener || typeof chrome === "undefined") {
      return;
    }

    if (!chrome.storage || !chrome.storage.onChanged) {
      return;
    }

    try {
      chrome.storage.onChanged.addListener((changes, areaName) => {
        if (areaName !== "local") {
          return;
        }
        if (!changes[SERVER_URL_KEY] && !changes[JWT_KEY]) {
          return;
        }
        openStatusSocket();
      });

      hasStorageListener = true;
    } catch (e) {}
  }

  function setStatus(value) {
    if (!isStatus(value)) {
      return;
    }

    currentStatus = value;
    applyVisualState();
    persistStatus(value);
    syncStatusToServer(value);
  }

  function loadStatus() {
    const storage = getStorage();
    if (!storage) {
      applyVisualState();
      return;
    }

    try {
      storage.get([STORAGE_KEY], (result) => {
        if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
          applyVisualState();
          return;
        }

        if (result && isStatus(result[STORAGE_KEY])) {
          currentStatus = result[STORAGE_KEY];
        }

        applyVisualState();
      });
    } catch (e) {
      applyVisualState();
    }
  }

  function createStatusButton(status, label) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `breathe-status-choice breathe-status-${status}`;
    button.textContent = label;
    button.addEventListener("click", () => {
      setStatus(status);
    });
    return button;
  }

  function mountPanel(container) {
    panelElement = container;
    panelElement.innerHTML = "";

    const title = document.createElement("p");
    title.className = "breathe-subpanel-title";
    title.textContent = "How am I right now?";

    const choices = document.createElement("div");
    choices.className = "breathe-status-choices";

    statusButtons = {
      green: createStatusButton("green", "Green"),
      yellow: createStatusButton("yellow", "Yellow"),
      red: createStatusButton("red", "Red"),
    };

    choices.append(statusButtons.green, statusButtons.yellow, statusButtons.red);

    nudgeElement = document.createElement("p");
    nudgeElement.className = "breathe-status-nudge";

    panelElement.append(title, choices, nudgeElement);
    loadStatus();
    ensureStorageSyncListener();
    openStatusSocket();
  }

  function setStatusDotListener(callback) {
    statusDotCallback = callback;
    notifyStatusDot();
  }

  function togglePanel() {
    if (!panelElement) {
      return;
    }

    panelElement.classList.toggle("is-hidden");
  }

  function hidePanel() {
    if (!panelElement) {
      return;
    }

    panelElement.classList.add("is-hidden");
  }

  modules.status = {
    hidePanel,
    loadStatus,
    mountPanel,
    openStatusSocket,
    setStatus,
    setStatusDotListener,
    togglePanel,
  };
})();
