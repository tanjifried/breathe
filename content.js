(function () {
  "use strict";

  if (window.__BREATHE_BOOTED__) {
    return;
  }
  window.__BREATHE_BOOTED__ = true;

  const modules = (window.BreatheModules = window.BreatheModules || {});
  const ROOT_ID = "breathe-widget-root";
  const DARK_MODE_KEY = "breathe_dark_mode";

  let observer = null;
  let checkQueued = false;
  let hasThemeListener = false;

  function withGuard(taskName, fn) {
    try {
      fn();
    } catch (error) {
      console.warn(`[Breathe] ${taskName} failed`, error);
    }
  }

  function updateStatusDot(dotElement, state) {
    if (!dotElement) {
      return;
    }

    dotElement.className = "breathe-status-dot";
    const ownStatus = typeof state === "string" ? state : state && state.ownStatus;
    const partnerStatus = state && state.partnerStatus;
    const displayStatus = partnerStatus || ownStatus;

    if (!displayStatus) {
      return;
    }

    dotElement.classList.add("is-visible", `is-${displayStatus}`);
    if (partnerStatus) {
      dotElement.classList.add("is-partner");
    }
  }

  function hideSubPanels() {
    if (modules.status && modules.status.hidePanel) {
      modules.status.hidePanel();
    }
    if (modules.reentry && modules.reentry.hidePanel) {
      modules.reentry.hidePanel();
    }
  }

  function createSvgIcon(pathData, className) {
    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("aria-hidden", "true");
    svg.setAttribute("focusable", "false");
    svg.className = className;

    const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
    path.setAttribute("d", pathData);
    path.setAttribute("fill", "currentColor");
    svg.appendChild(path);
    return svg;
  }

  function createToggleIcon() {
    return createSvgIcon(
      "M3 8c2.2 2 4.8 2 7 0s4.8-2 7 0c1 .9 2.4 1.5 4 1.7v2.2c-2.2-.2-4.2-1.1-5.6-2.3-1.4-1.2-2.4-1.2-3.8 0-2.9 2.6-7.3 2.6-10.2 0L3 8zm0 6c2.2 2 4.8 2 7 0s4.8-2 7 0c1 .9 2.4 1.5 4 1.7v2.2c-2.2-.2-4.2-1.1-5.6-2.3-1.4-1.2-2.4-1.2-3.8 0-2.9 2.6-7.3 2.6-10.2 0L3 14z",
      "breathe-widget-icon"
    );
  }

  function createActionIcon(kind) {
    const iconMap = {
      calm:
        "M4 13.5c1.8 1.8 4.2 1.8 6 0s4.2-1.8 6 0 4.2 1.8 6 0v2c-2.5 1.8-5.5 1.8-8 0s-5.5-1.8-8 0-5.5 1.8-8 0v-2z",
      status:
        "M12 3a1 1 0 0 1 1 1v3h2a1 1 0 1 1 0 2h-2v6h2a1 1 0 1 1 0 2h-2v3a1 1 0 1 1-2 0v-3H9a1 1 0 1 1 0-2h2V9H9a1 1 0 1 1 0-2h2V4a1 1 0 0 1 1-1zm-5 1a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm10 12a2 2 0 1 0 0 4 2 2 0 0 0 0-4z",
      timeout:
        "M7 3h10a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zm1 3v12h8V6H8zm2 2h2v8h-2V8zm4 0h2v8h-2V8z",
      talk:
        "M4 5a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H9l-5 4v-4H6a2 2 0 0 1-2-2V5zm4 2h8v2H8V7zm0 4h6v2H8v-2z",
    };

    const pathData = iconMap[kind];
    if (!pathData) {
      return null;
    }

    return createSvgIcon(pathData, "breathe-action-icon");
  }

  function getStorage() {
    if (typeof chrome === "undefined") {
      return null;
    }
    return chrome.storage && chrome.storage.local ? chrome.storage.local : null;
  }

  function applyDarkMode(enabled) {
    document.documentElement.classList.toggle("breathe-dark-mode", Boolean(enabled));
  }

  function loadThemePreference() {
    const storage = getStorage();
    if (!storage) {
      return;
    }

    storage.get([DARK_MODE_KEY], (result) => {
      if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
        return;
      }
      applyDarkMode(result && result[DARK_MODE_KEY]);
    });
  }

  function ensureThemeSyncListener() {
    if (hasThemeListener || typeof chrome === "undefined") {
      return;
    }
    if (!chrome.storage || !chrome.storage.onChanged) {
      return;
    }

    chrome.storage.onChanged.addListener((changes, areaName) => {
      if (areaName !== "local" || !changes[DARK_MODE_KEY]) {
        return;
      }
      applyDarkMode(changes[DARK_MODE_KEY].newValue);
    });

    hasThemeListener = true;
  }

  function createButton(label, className, iconKind, onClick) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `breathe-action-button ${className}`;

    const icon = createActionIcon(iconKind);
    if (icon) {
      button.appendChild(icon);
    }

    const text = document.createElement("span");
    text.className = "breathe-action-label";
    text.textContent = label;
    button.appendChild(text);

    button.addEventListener("click", onClick);
    return button;
  }

  function createWidget() {
    const root = document.createElement("div");
    root.id = ROOT_ID;
    root.className = "breathe-widget is-collapsed";

    const toggle = document.createElement("button");
    toggle.type = "button";
    toggle.className = "breathe-widget-toggle";
    toggle.setAttribute("aria-label", "Toggle Breathe controls");

    const icon = createToggleIcon();

    const title = document.createElement("span");
    title.className = "breathe-widget-title";
    title.textContent = "Breathe";

    const statusDot = document.createElement("span");
    statusDot.className = "breathe-status-dot";

    toggle.append(icon, title, statusDot);

    const panel = document.createElement("div");
    panel.className = "breathe-widget-panel";

    const buttonsRow = document.createElement("div");
    buttonsRow.className = "breathe-buttons-row";

    const statusPanel = document.createElement("div");
    statusPanel.className = "breathe-subpanel is-hidden";

    const reentryPanel = document.createElement("div");
    reentryPanel.className = "breathe-subpanel is-hidden";

    const calmButton = createButton("Calm", "is-calm", "calm", () => {
      withGuard("calm", () => {
        hideSubPanels();
        if (modules.calm && modules.calm.start) {
          modules.calm.start();
        }
      });
    });

    const statusButton = createButton("Status", "is-status", "status", () => {
      withGuard("status", () => {
        if (!modules.status) {
          return;
        }
        if (modules.reentry && modules.reentry.hidePanel) {
          modules.reentry.hidePanel();
        }
        if (modules.status.togglePanel) {
          modules.status.togglePanel();
        }
      });
    });

    const timeoutButton = createButton("Timeout", "is-timeout", "timeout", () => {
      withGuard("timeout", () => {
        hideSubPanels();
        if (modules.timeout && modules.timeout.start) {
          modules.timeout.start();
        }
      });
    });

    const talkButton = createButton("Talk", "is-talk", "talk", () => {
      withGuard("reentry", () => {
        if (!modules.reentry) {
          return;
        }
        if (modules.status && modules.status.hidePanel) {
          modules.status.hidePanel();
        }
        if (modules.reentry.togglePanel) {
          modules.reentry.togglePanel();
        }
      });
    });

    buttonsRow.append(calmButton, statusButton, timeoutButton, talkButton);
    panel.append(buttonsRow, statusPanel, reentryPanel);
    root.append(toggle, panel);

    toggle.addEventListener("click", () => {
      const isCollapsed = root.classList.contains("is-collapsed");
      root.classList.toggle("is-collapsed", !isCollapsed);
      root.classList.toggle("is-expanded", isCollapsed);
      if (!isCollapsed) {
        hideSubPanels();
      }
    });

    document.body.appendChild(root);

    withGuard("status mount", () => {
      if (modules.status && modules.status.mountPanel) {
        modules.status.mountPanel(statusPanel);
      }
      if (modules.status && modules.status.setStatusDotListener) {
        modules.status.setStatusDotListener((status) => updateStatusDot(statusDot, status));
      }
    });

    withGuard("reentry mount", () => {
      if (modules.reentry && modules.reentry.mountPanel) {
        modules.reentry.mountPanel(reentryPanel);
      }
    });

    return root;
  }

  function ensureWidget() {
    if (!document.body) {
      return;
    }

    const existing = document.getElementById(ROOT_ID);
    if (existing) {
      return;
    }

    createWidget();
  }

  function queueEnsureWidget() {
    if (checkQueued) {
      return;
    }

    checkQueued = true;
    window.requestAnimationFrame(() => {
      checkQueued = false;
      ensureWidget();
    });
  }

  function startObserver() {
    if (observer) {
      return;
    }

    observer = new MutationObserver(() => {
      if (document.getElementById(ROOT_ID)) {
        return;
      }
      queueEnsureWidget();
    });

    const target = document.body || document.documentElement;
    if (target) {
      observer.observe(target, { childList: true, subtree: true });
    }
  }

  function init() {
    console.log("Breathe loaded");
    loadThemePreference();
    ensureThemeSyncListener();
    ensureWidget();
    startObserver();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init, { once: true });
  } else {
    init();
  }
})();
