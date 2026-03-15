(function () {
  "use strict";

  const modules = (window.BreatheModules = window.BreatheModules || {});
  const overlayTools = modules.overlay || {};

  const OVERLAY_ID = "breathe-calm-overlay";
  const DEFAULT_DURATION_SECONDS = 20 * 60;

  let timerId = null;

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
      stopTimer();
      if (overlayTools.removeNode) {
        overlayTools.removeNode(overlay);
      } else if (overlay.parentNode) {
        overlay.parentNode.removeChild(overlay);
      }
    };

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
