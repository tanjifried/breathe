(function () {
  "use strict";

  const modules = (window.BreatheModules = window.BreatheModules || {});

  function formatDuration(totalSeconds) {
    const safeSeconds = Math.max(0, Number(totalSeconds) || 0);
    const minutes = Math.floor(safeSeconds / 60);
    const seconds = safeSeconds % 60;
    return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
  }

  function removeNode(node) {
    if (node && node.parentNode) {
      node.parentNode.removeChild(node);
    }
  }

  function getStorage() {
    if (typeof chrome === "undefined") {
      return null;
    }
    return chrome.storage && chrome.storage.local ? chrome.storage.local : null;
  }

  function clampPosition(root, left, top) {
    const maxLeft = Math.max(0, window.innerWidth - root.offsetWidth);
    const maxTop = Math.max(0, window.innerHeight - root.offsetHeight);
    return {
      left: Math.max(0, Math.min(maxLeft, left)),
      top: Math.max(0, Math.min(maxTop, top)),
    };
  }

  function makeDraggable(root, onExpand) {
    const storage = getStorage();
    if (!root || !storage) return;

    let dragging = false;
    let moved = false;
    let startX = 0;
    let startY = 0;
    let startLeft = 0;
    let startTop = 0;

    root.addEventListener("mousedown", (event) => {
      if (event.button !== 0) return;

      dragging = true;
      moved = false;

      const rect = root.getBoundingClientRect();
      startX = event.clientX;
      startY = event.clientY;
      startLeft = rect.left;
      startTop = rect.top;

      root.classList.add("is-dragging");
      root.style.transition = "none";
      root.style.right = "auto";
      root.style.bottom = "auto";
      root.style.left = `${startLeft}px`;
      root.style.top = `${startTop}px`;

      event.preventDefault();
    });

    document.addEventListener("mousemove", (event) => {
      if (!dragging) return;

      const dx = event.clientX - startX;
      const dy = event.clientY - startY;
      moved = moved || Math.abs(dx) > 3 || Math.abs(dy) > 3;

      const clamped = clampPosition(root, startLeft + dx, startTop + dy);
      root.style.left = `${clamped.left}px`;
      root.style.top = `${clamped.top}px`;
    });

    document.addEventListener("mouseup", () => {
      if (!dragging) return;
      dragging = false;

      root.classList.remove("is-dragging");
      root.style.transition = "";

      if (moved) {
        try {
          storage.set({
            breathe_pill_pos: {
              left: parseFloat(root.style.left),
              top: parseFloat(root.style.top),
            },
          });
        } catch (e) {}
      }
    });

    root.addEventListener("click", (event) => {
      if (moved) {
        moved = false;
        event.preventDefault();
        event.stopPropagation();
        return;
      }
      removeNode(root);
      if (typeof onExpand === "function") {
        onExpand();
      }
    });
  }

  function createOverlay(id, className) {
    const existing = document.getElementById(id);
    removeNode(existing);

    const overlay = document.createElement("div");
    overlay.id = id;
    overlay.className = className;
    document.body.appendChild(overlay);
    return overlay;
  }

  function createMinimizedPill(labelText, type, onExpand) {
    const existing = document.getElementById("breathe-mini-pill");
    removeNode(existing);

    const pill = document.createElement("div");
    pill.id = "breathe-mini-pill";
    pill.className = "breathe-timer-pill";

    const orb = document.createElement("div");
    orb.className = `breathe-timer-pill-orb${type === "timeout" ? " is-timeout" : ""}`;
    orb.animate(
      [
        { transform: "scale(0.82)", opacity: 0.76 },
        { transform: "scale(1.14)", opacity: 1, offset: 0.5 },
        { transform: "scale(0.82)", opacity: 0.76 }
      ],
      {
        duration: 6000,
        iterations: Infinity,
        easing: "ease-in-out"
      }
    );

    const label = document.createElement("span");
    label.id = "breathe-mini-pill-label";
    label.textContent = labelText;

    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("fill", "none");
    svg.setAttribute("stroke", "currentColor");
    svg.setAttribute("stroke-width", "2");
    svg.setAttribute("class", "breathe-timer-pill-expand");

    const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
    path.setAttribute(
      "d",
      "M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3"
    );
    svg.appendChild(path);

    pill.append(orb, label, svg);

    document.body.appendChild(pill);

    const storage = getStorage();
    if (storage) {
      try {
        storage.get(["breathe_pill_pos"], (result) => {
          if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
            return;
          }
          if (result && result.breathe_pill_pos) {
            const pos = result.breathe_pill_pos;
            pill.style.right = "auto";
            pill.style.bottom = "auto";
            const clamped = clampPosition(pill, pos.left, pos.top);
            pill.style.left = `${clamped.left}px`;
            pill.style.top = `${clamped.top}px`;
          }
        });
      } catch (e) {}
    }

    makeDraggable(pill, onExpand);

    return pill;
  }

  modules.overlay = {
    createOverlay,
    createMinimizedPill,
    formatDuration,
    removeNode,
  };
})();
