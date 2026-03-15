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

  modules.overlay = {
    createOverlay,
    formatDuration,
    removeNode,
  };
})();
