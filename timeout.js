(function () {
  "use strict";

  const modules = (window.BreatheModules = window.BreatheModules || {});
  const overlayTools = modules.overlay || {};

  const OVERLAY_ID = "breathe-timeout-overlay";
  const DEFAULT_DURATION_SECONDS = 20 * 60;
  const TARGET_SELECTORS = [
    '[role="main"]',
    'div[aria-label*="Conversation"]',
    'div[aria-label*="Messenger"]',
    'main',
  ];

  let timerId = null;
  let blurTarget = null;

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

    shell.append(title, body, timer);
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
  }

  modules.timeout = {
    start,
    stop,
  };
})();
