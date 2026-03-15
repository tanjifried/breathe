(function () {
  "use strict";

  const EXTENSION_NS = "[Breathe]";

  chrome.runtime.onInstalled.addListener((details) => {
    console.log(`${EXTENSION_NS} installed:`, details.reason);
  });

  chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
    if (!message || message.type !== "BREATHE_PING") {
      return;
    }

    sendResponse({ ok: true, now: Date.now() });
  });
})();
