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
    pill.addEventListener("click", () => {
      removeNode(pill);
      if (typeof onExpand === "function") {
        onExpand();
      }
    });

    document.body.appendChild(pill);
    return pill;
  }

  modules.overlay = {
    createOverlay,
    createMinimizedPill,
    formatDuration,
    removeNode,
  };
})();
