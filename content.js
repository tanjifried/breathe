(function () {
  "use strict";

  if (window.__BREATHE_BOOTED__) {
    return;
  }
  window.__BREATHE_BOOTED__ = true;

  const modules = (window.BreatheModules = window.BreatheModules || {});
  const ROOT_ID = "breathe-widget-root";

  let observer = null;
  let checkQueued = false;

  function withGuard(taskName, fn) {
    try {
      fn();
    } catch (error) {
      console.warn(`[Breathe] ${taskName} failed`, error);
    }
  }

  function updateStatusDot(dotElement, status) {
    if (!dotElement) {
      return;
    }

    dotElement.className = "breathe-status-dot";
    if (!status) {
      return;
    }

    dotElement.classList.add("is-visible", `is-${status}`);
  }

  function hideSubPanels() {
    if (modules.status && modules.status.hidePanel) {
      modules.status.hidePanel();
    }
    if (modules.reentry && modules.reentry.hidePanel) {
      modules.reentry.hidePanel();
    }
  }

  function createButton(label, className, onClick) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `breathe-action-button ${className}`;
    button.textContent = label;
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

    const icon = document.createElement("span");
    icon.className = "breathe-widget-icon";
    icon.textContent = "~";

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

    const calmButton = createButton("Calm", "is-calm", () => {
      withGuard("calm", () => {
        hideSubPanels();
        if (modules.calm && modules.calm.start) {
          modules.calm.start();
        }
      });
    });

    const statusButton = createButton("Status", "is-status", () => {
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

    const timeoutButton = createButton("Timeout", "is-timeout", () => {
      withGuard("timeout", () => {
        hideSubPanels();
        if (modules.timeout && modules.timeout.start) {
          modules.timeout.start();
        }
      });
    });

    const talkButton = createButton("Talk", "is-talk", () => {
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
    ensureWidget();
    startObserver();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init, { once: true });
  } else {
    init();
  }
})();
