(function () {
  "use strict";

  const modules = (window.BreatheModules = window.BreatheModules || {});
  const overlayTools = modules.overlay || {};

  const OVERLAY_ID = "breathe-timeout-overlay";
  const DEFAULT_DURATION_SECONDS = 20 * 60;
  const TIMEOUT_DURATION_KEY = "breathe_timeout_duration";
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
      try {
        storage.get([SERVER_URL_KEY, JWT_KEY, TIMEOUT_DURATION_KEY], (result) => {
          if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
            resolve(null);
            return;
          }

          const serverUrl = normalizeServerUrl(result && result[SERVER_URL_KEY]);
          const token = typeof (result && result[JWT_KEY]) === "string" ? result[JWT_KEY].trim() : "";
          const customDuration = result && result[TIMEOUT_DURATION_KEY] ? parseInt(result[TIMEOUT_DURATION_KEY], 10) * 60 : DEFAULT_DURATION_SECONDS;

          if (!serverUrl || !token || !isValidHttpUrl(serverUrl)) {
            resolve({ durationSeconds: customDuration });
            return;
          }

          resolve({ serverUrl, token, durationSeconds: customDuration });
        });
      } catch (e) {
        resolve(null);
      }
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

    if (pillUpdaterId) {
      window.clearInterval(pillUpdaterId);
      pillUpdaterId = null;
    }
  }

  function removeMinimizedPill() {
    const pill = document.getElementById("breathe-mini-pill");
    if (!pill) {
      return;
    }

    if (overlayTools.removeNode) {
      overlayTools.removeNode(pill);
    } else if (pill.parentNode) {
      pill.parentNode.removeChild(pill);
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
    confirmTitle.textContent = "Yes, I am ready - end timeout";

    const confirmSub = document.createElement("p");
    confirmSub.className = "breathe-calm-confirm-sub";
    confirmSub.textContent = "Tap to confirm and return to chat.";

    confirmText.append(confirmTitle, confirmSub);
    confirmCard.append(checkIcon, confirmText);
    confirmCard.addEventListener("click", onConfirm);

    container.appendChild(confirmCard);
  }

  function buildPrivateNote(container) {
    const noteWrap = document.createElement("div");
    noteWrap.className = "breathe-private-note";

    const textarea = document.createElement("textarea");
    textarea.className = "breathe-note-textarea";
    textarea.placeholder = "Write what you're feeling. No one else sees this.";
    textarea.rows = 4;

    const saveButton = document.createElement("button");
    saveButton.type = "button";
    saveButton.className = "breathe-primary-button";
    saveButton.style.marginTop = "8px";
    saveButton.style.width = "100%";
    saveButton.textContent = "Save note privately";

    saveButton.addEventListener("click", () => {
      const note = textarea.value.trim();
      if (!note) {
        return;
      }

      const storage = getStorage();
      if (storage) {
        try {
          const key = `breathe_note_${Date.now()}`;
          storage.set({
            [key]: {
              text: note,
              at: new Date().toISOString(),
            },
          });
        } catch (e) {}
      }

      saveButton.textContent = "Saved.";
      saveButton.disabled = true;
      textarea.disabled = true;
    });

    noteWrap.append(textarea, saveButton);
    container.appendChild(noteWrap);
    textarea.focus();
  }

  function buildSosPanel(container, onExtend) {
    const panel = document.createElement("div");
    panel.className = "breathe-sos-panel";

    const title = document.createElement("p");
    title.className = "breathe-sos-title";
    title.textContent = "That's okay. Try one of these.";
    panel.appendChild(title);

    const items = [
      {
        icon: "\ud83d\udeb6",
        label: "Step away briefly",
        sub: "Walk to another room for 5 minutes",
      },
      {
        icon: "\ud83d\udca7",
        label: "Cold water on your wrists",
        sub: "Fastest way to reset your body",
      },
      {
        icon: "\u270d\ufe0f",
        label: "Write it out",
        sub: "Private note - just for you",
        action: "write",
      },
      {
        icon: "\u23f1\ufe0f",
        label: "Extend timeout +10 min",
        sub: "More time, no pressure",
        action: "extend",
      },
    ];

    items.forEach((itemData) => {
      const item = document.createElement("div");
      item.className = `breathe-sos-item${itemData.action ? " is-actionable" : ""}`;

      const iconElement = document.createElement("div");
      iconElement.className = "breathe-sos-icon";
      iconElement.textContent = itemData.icon;

      const textElement = document.createElement("div");
      textElement.className = "breathe-sos-text";

      const labelElement = document.createElement("p");
      labelElement.className = "breathe-sos-label";
      labelElement.textContent = itemData.label;

      const subElement = document.createElement("p");
      subElement.className = "breathe-sos-sub";
      subElement.textContent = itemData.sub;

      textElement.append(labelElement, subElement);
      item.append(iconElement, textElement);

      if (itemData.action === "extend") {
        let didExtend = false;
        item.addEventListener("click", () => {
          if (didExtend) {
            return;
          }

          didExtend = true;
          onExtend(10 * 60);
          void postSession("/api/sessions/extend", {
            sessionId: activeSessionId,
            addedSeconds: 600,
          });

          labelElement.textContent = "+10 min added";
          subElement.textContent = "Hang in there.";
          item.classList.remove("is-actionable");
          item.style.cursor = "default";
        });
      }

      if (itemData.action === "write") {
        item.addEventListener("click", () => {
          buildPrivateNote(panel);
          item.remove();
        });
      }

      panel.appendChild(item);
    });

    container.appendChild(panel);
  }

  function buildSosLink(container, onExtend) {
    const sosLink = document.createElement("button");
    sosLink.type = "button";
    sosLink.className = "breathe-ghost-button is-danger";
    sosLink.style.fontSize = "11px";
    sosLink.style.marginTop = "8px";
    sosLink.textContent = "Still overwhelmed?";
    sosLink.addEventListener("click", () => {
      sosLink.remove();
      buildSosPanel(container, onExtend);
    });
    container.appendChild(sosLink);
  }

  function stop() {
    const sessionToEnd = activeSessionId;
    activeSessionId = null;
    void endServerSession(sessionToEnd);

    stopTimer();
    removeMinimizedPill();
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

  async function start(options) {
    const config = await loadSyncConfig();
    const durationSeconds =
      options && Number.isFinite(options.durationSeconds)
        ? Math.max(5, Math.floor(options.durationSeconds))
        : (config && config.durationSeconds ? config.durationSeconds : DEFAULT_DURATION_SECONDS);

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
    earlyExitLink.textContent = "I'm ready early ->";

    const lockNotice = document.createElement("p");
    lockNotice.className = "breathe-overlay-body";
    lockNotice.style.display = "none";

    shell.append(title, body, lockNotice, timer, controlsRow, earlyExitLink);
    overlay.appendChild(shell);

    let remaining = durationSeconds;
    let sosShown = false;
    const SOS_TRIGGER_SECONDS = 5 * 60;
    const formatDuration = overlayTools.formatDuration || ((value) => `${value}`);
    timer.textContent = formatDuration(remaining);

    minimizeButton.addEventListener("click", () => {
      overlay.style.display = "none";

      if (!overlayTools.createMinimizedPill) {
        return;
      }

      removeMinimizedPill();
      overlayTools.createMinimizedPill(`Timeout · ${formatDuration(remaining)}`, "timeout", () => {
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
        pillLabel.textContent = `Timeout · ${formatDuration(remaining)}`;
      }, 1000);
    });

    earlyExitLink.addEventListener("click", () => {
      earlyExitLink.style.display = "none";
      buildEarlyExitConfirm(shell, () => {
        stop();
      });
    });

    timerId = window.setInterval(() => {
      remaining -= 1;
      timer.textContent = formatDuration(remaining);

      if (!sosShown && durationSeconds - remaining >= SOS_TRIGGER_SECONDS) {
        sosShown = true;
        buildSosLink(shell, (addedSeconds) => {
          remaining += addedSeconds;
        });
      }

      if (remaining > 0) {
        return;
      }

      stopTimer();
      removeMinimizedPill();
      overlay.style.display = "";
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
