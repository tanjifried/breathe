(function () {
  "use strict";

  const modules = (window.BreatheModules = window.BreatheModules || {});
  const overlayTools = modules.overlay || {};

  const OVERLAY_ID = "breathe-calm-overlay";
  const DEFAULT_DURATION_SECONDS = 20 * 60;
  const SERVER_URL_KEY = "breathe_server_url";
  const JWT_KEY = "breathe_jwt";

  let timerId = null;
  let activeSessionId = null;

  function getStorage() {
    if (typeof chrome === "undefined") {
      return null;
    }
    return chrome.storage && chrome.storage.local ? chrome.storage.local : null;
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

  function loadSyncConfig() {
    const storage = getStorage();
    if (!storage) {
      return Promise.resolve(null);
    }

    return new Promise((resolve) => {
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
    });
  }

  async function postSession(path, body) {
    const config = await loadSyncConfig();
    if (!config) {
      return null;
    }

    try {
      const response = await fetch(`${config.serverUrl}${path}`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${config.token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(body || {}),
      });

      if (!response.ok) {
        return null;
      }

      return await response.json();
    } catch (_error) {
      return null;
    }
  }

  async function startServerSession() {
    return postSession("/api/sessions/start", { featureUsed: "calm" });
  }

  async function endServerSession(sessionId) {
    if (!sessionId) {
      return null;
    }
    return postSession("/api/sessions/end", { sessionId });
  }

  function stopTimer() {
    if (timerId) {
      window.clearInterval(timerId);
      timerId = null;
    }
  }

  function buildDoneState(container, onDismiss) {
    container.innerHTML = "";

    const doneTitle = document.createElement("h2");
    doneTitle.className = "breathe-overlay-title";
    doneTitle.textContent = "Ready to talk?";

    const doneBody = document.createElement("p");
    doneBody.className = "breathe-overlay-body";
    doneBody.textContent = "Take one more breath, then return with care.";

    const dismissButton = document.createElement("button");
    dismissButton.className = "breathe-primary-button";
    dismissButton.textContent = "Dismiss";
    dismissButton.addEventListener("click", onDismiss);

    container.append(doneTitle, doneBody, dismissButton);
  }

  function start(options) {
    const durationSeconds =
      options && Number.isFinite(options.durationSeconds)
        ? Math.max(5, Math.floor(options.durationSeconds))
        : DEFAULT_DURATION_SECONDS;

    stopTimer();

    const overlay = overlayTools.createOverlay
      ? overlayTools.createOverlay(OVERLAY_ID, "breathe-overlay breathe-calm-overlay")
      : null;

    if (!overlay) {
      return;
    }

    const shell = document.createElement("div");
    shell.className = "breathe-overlay-shell";

    const title = document.createElement("h2");
    title.className = "breathe-overlay-title";
    title.textContent = "Take a breath. You are safe.";

    const body = document.createElement("p");
    body.className = "breathe-overlay-body";
    body.textContent = "Breathe slowly in and out while the timer runs.";

    const orb = document.createElement("div");
    orb.className = "breathe-orb";

    const timer = document.createElement("div");
    timer.className = "breathe-overlay-timer";

    shell.append(title, body, orb, timer);
    overlay.appendChild(shell);

    let remaining = durationSeconds;
    const formatDuration = overlayTools.formatDuration || ((value) => `${value}`);
    timer.textContent = formatDuration(remaining);

    const dismissOverlay = () => {
      const sessionToEnd = activeSessionId;
      activeSessionId = null;
      void endServerSession(sessionToEnd);
      stopTimer();
      if (overlayTools.removeNode) {
        overlayTools.removeNode(overlay);
      } else if (overlay.parentNode) {
        overlay.parentNode.removeChild(overlay);
      }
    };

    const lockNotice = document.createElement("p");
    lockNotice.className = "breathe-overlay-body";
    lockNotice.style.display = "none";
    shell.appendChild(lockNotice);

    void startServerSession().then((payload) => {
      if (!payload || !payload.sessionId) {
        return;
      }

      activeSessionId = payload.sessionId;
      if (!payload.locked) {
        return;
      }

      lockNotice.textContent =
        "Partner cooldown is active. Keep re-entry gentle when this timer ends.";
      lockNotice.style.display = "block";
    });

    timerId = window.setInterval(() => {
      remaining -= 1;
      timer.textContent = formatDuration(remaining);

      if (remaining > 0) {
        return;
      }

      stopTimer();
      buildDoneState(shell, dismissOverlay);
    }, 1000);
  }

  modules.calm = {
    start,
    stopTimer,
  };
})();
