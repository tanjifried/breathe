(function () {
  "use strict";

  const modules = (window.BreatheModules = window.BreatheModules || {});
  const STORAGE_KEY = "breathe_status";
  const ALLOWED_STATUSES = ["green", "yellow", "red"];

  let panelElement = null;
  let nudgeElement = null;
  let statusButtons = {};
  let statusDotCallback = null;
  let currentStatus = null;

  function getStorage() {
    if (typeof chrome === "undefined") {
      return null;
    }
    return chrome.storage && chrome.storage.local ? chrome.storage.local : null;
  }

  function isStatus(value) {
    return ALLOWED_STATUSES.includes(value);
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

    if (typeof statusDotCallback === "function") {
      statusDotCallback(currentStatus);
    }
  }

  function persistStatus(value) {
    const storage = getStorage();
    if (!storage) {
      return Promise.resolve();
    }

    return new Promise((resolve) => {
      storage.set({ [STORAGE_KEY]: value }, () => {
        resolve();
      });
    });
  }

  function setStatus(value) {
    if (!isStatus(value)) {
      return;
    }

    currentStatus = value;
    applyVisualState();
    persistStatus(value);
  }

  function loadStatus() {
    const storage = getStorage();
    if (!storage) {
      applyVisualState();
      return;
    }

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
  }

  function setStatusDotListener(callback) {
    statusDotCallback = callback;
    applyVisualState();
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
    setStatus,
    setStatusDotListener,
    togglePanel,
  };
})();
