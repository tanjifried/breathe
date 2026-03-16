(function () {
  "use strict";

  const modules = (window.BreatheModules = window.BreatheModules || {});
  const overlayTools = modules.overlay || {};

  const OVERLAY_ID = "breathe-timeout-overlay";
  const DEFAULT_DURATION_SECONDS = 20 * 60;
  const SERVER_URL_KEY = "breathe_server_url";
  const JWT_KEY = "breathe_jwt";
  const TARGET_SELECTORS = [
    '[role="main"]',
    'div[aria-label*="Conversation"]',
    'div[aria-label*="Messenger"]',
    'main',
  ];

  let timerId = null;
  let blurTarget = null;
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
    return postSession("/api/sessions/start", { featureUsed: "timeout" });
  }

  async function endServerSession(sessionId) {
    if (!sessionId) {
      return null;
    }
    return postSession("/api/sessions/end", { sessionId });
  }

  function findBlurTarget() {
    for (const selector of TARGET_SELECTORS) {
      const node = document.querySelector(selector);
      if (node) {
        return node;
      }
    }

    return document.body;
  }

  function stopTimer() {
    if (timerId) {
      window.clearInterval(timerId);
      timerId = null;
    }
  }

  function clearBlur() {
    if (blurTarget) {
      blurTarget.classList.remove("breathe-timeout-blurred");
      blurTarget = null;
    }
  }

  function finish(shell, onDone) {
    shell.innerHTML = "";

    const title = document.createElement("h2");
    title.className = "breathe-overlay-title";
    title.textContent = "Are you ready to reconnect?";

    const body = document.createElement("p");
    body.className = "breathe-overlay-body";
    body.textContent = "If yes, return gently and keep your message short.";

    const doneButton = document.createElement("button");
    doneButton.type = "button";
    doneButton.className = "breathe-primary-button";
    doneButton.textContent = "Yes, I am ready";
    doneButton.addEventListener("click", onDone);

    shell.append(title, body, doneButton);
  }

  function stop() {
    const sessionToEnd = activeSessionId;
    activeSessionId = null;
    void endServerSession(sessionToEnd);

    stopTimer();
    clearBlur();

    const existing = document.getElementById(OVERLAY_ID);
    if (existing) {
      if (overlayTools.removeNode) {
        overlayTools.removeNode(existing);
      } else if (existing.parentNode) {
        existing.parentNode.removeChild(existing);
      }
    }
  }

  function start(options) {
    const durationSeconds =
      options && Number.isFinite(options.durationSeconds)
        ? Math.max(5, Math.floor(options.durationSeconds))
        : DEFAULT_DURATION_SECONDS;

    stop();

    blurTarget = findBlurTarget();
    if (blurTarget) {
      blurTarget.classList.add("breathe-timeout-blurred");
    }

    const overlay = overlayTools.createOverlay
      ? overlayTools.createOverlay(OVERLAY_ID, "breathe-overlay breathe-timeout-overlay")
      : null;

    if (!overlay) {
      return;
    }

    const shell = document.createElement("div");
    shell.className = "breathe-overlay-shell breathe-timeout-shell";

    const title = document.createElement("h2");
    title.className = "breathe-overlay-title";
    title.textContent = "Structured timeout";

    const body = document.createElement("p");
    body.className = "breathe-overlay-body";
    body.textContent = "Pause, reset your body, then choose your next words.";

    const timer = document.createElement("div");
    timer.className = "breathe-overlay-timer";

    const lockNotice = document.createElement("p");
    lockNotice.className = "breathe-overlay-body";
    lockNotice.style.display = "none";

    shell.append(title, body, lockNotice, timer);
    overlay.appendChild(shell);

    let remaining = durationSeconds;
    const formatDuration = overlayTools.formatDuration || ((value) => `${value}`);
    timer.textContent = formatDuration(remaining);

    timerId = window.setInterval(() => {
      remaining -= 1;
      timer.textContent = formatDuration(remaining);

      if (remaining > 0) {
        return;
      }

      stopTimer();
      clearBlur();
      finish(shell, stop);
    }, 1000);

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
  }

  modules.timeout = {
    start,
    stop,
  };
})();
