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
  let pillUpdaterId = null;

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

  async function endServerSession(sessionId, extraPayload) {
    if (!sessionId) {
      return null;
    }
    return postSession("/api/sessions/end", { sessionId, ...(extraPayload || {}) });
  }

  function stopTimer() {
    if (timerId) {
      window.clearInterval(timerId);
      timerId = null;
    }

    if (pillUpdaterId) {
      window.clearInterval(pillUpdaterId);
      pillUpdaterId = null;
    }
  }

  function removeMinimizedPill() {
    const pill = document.getElementById("breathe-mini-pill");
    if (pill) {
      if (overlayTools.removeNode) {
        overlayTools.removeNode(pill);
      } else if (pill.parentNode) {
        pill.parentNode.removeChild(pill);
      }
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

  function buildEarlyExitConfirm(container, onConfirm) {
    const confirmCard = document.createElement("div");
    confirmCard.className = "breathe-calm-confirm";

    const checkIcon = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    checkIcon.setAttribute("viewBox", "0 0 24 24");
    checkIcon.setAttribute("fill", "none");
    checkIcon.setAttribute("stroke", "currentColor");
    checkIcon.setAttribute("stroke-width", "3");
    checkIcon.setAttribute("class", "breathe-calm-confirm-icon");

    const checkPath = document.createElementNS("http://www.w3.org/2000/svg", "path");
    checkPath.setAttribute("d", "M5 13l4 4L19 7");
    checkIcon.appendChild(checkPath);

    const confirmText = document.createElement("div");
    confirmText.className = "breathe-calm-confirm-text";

    const confirmTitle = document.createElement("p");
    confirmTitle.className = "breathe-calm-confirm-title";
    confirmTitle.textContent = "Yes, I feel calm - end session";

    const confirmSub = document.createElement("p");
    confirmSub.className = "breathe-calm-confirm-sub";
    confirmSub.textContent = "Tap to confirm and return to chat.";

    confirmText.append(confirmTitle, confirmSub);
    confirmCard.append(checkIcon, confirmText);
    confirmCard.addEventListener("click", onConfirm);

    container.appendChild(confirmCard);
  }

  function stop(extraPayload) {
    const sessionToEnd = activeSessionId;
    activeSessionId = null;
    void endServerSession(sessionToEnd, extraPayload || null);
    stopTimer();
    removeMinimizedPill();

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

    const controlsRow = document.createElement("div");
    controlsRow.className = "breathe-overlay-controls";

    const minimizeButton = document.createElement("button");
    minimizeButton.type = "button";
    minimizeButton.className = "breathe-ghost-button";
    minimizeButton.textContent = "Minimize";

    controlsRow.appendChild(minimizeButton);

    const earlyExitLink = document.createElement("button");
    earlyExitLink.type = "button";
    earlyExitLink.className = "breathe-ghost-button";
    earlyExitLink.style.fontSize = "11px";
    earlyExitLink.style.marginTop = "4px";
    earlyExitLink.textContent = "I'm calm early ->";

    shell.append(title, body, orb, timer, controlsRow, earlyExitLink);
    overlay.appendChild(shell);

    let remaining = durationSeconds;
    const formatDuration = overlayTools.formatDuration || ((value) => `${value}`);
    timer.textContent = formatDuration(remaining);

    const dismissOverlay = (extraPayload) => {
      stop(extraPayload);
    };

    minimizeButton.addEventListener("click", () => {
      overlay.style.display = "none";

      if (!overlayTools.createMinimizedPill) {
        return;
      }

      removeMinimizedPill();
      overlayTools.createMinimizedPill(`Calm · ${formatDuration(remaining)}`, "calm", () => {
        overlay.style.display = "";
      });

      if (pillUpdaterId) {
        window.clearInterval(pillUpdaterId);
      }

      pillUpdaterId = window.setInterval(() => {
        const pillLabel = document.getElementById("breathe-mini-pill-label");
        if (!pillLabel) {
          window.clearInterval(pillUpdaterId);
          pillUpdaterId = null;
          return;
        }
        pillLabel.textContent = `Calm · ${formatDuration(remaining)}`;
      }, 1000);
    });

    earlyExitLink.addEventListener("click", () => {
      earlyExitLink.style.display = "none";
      buildEarlyExitConfirm(shell, () => {
        dismissOverlay({ calmedEarly: true });
      });
    });

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
      removeMinimizedPill();
      overlay.style.display = "";
      buildDoneState(shell, dismissOverlay);
    }, 1000);
  }

  modules.calm = {
    start,
    stop,
    stopTimer,
  };
})();
