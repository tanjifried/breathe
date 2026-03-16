(function () {
  "use strict";

  const SERVER_URL_KEY = "breathe_server_url";
  const JWT_KEY = "breathe_jwt";
  const DARK_MODE_KEY = "breathe_dark_mode";

  const form = document.getElementById("settings-form");
  const serverUrlInput = document.getElementById("server-url");
  const jwtInput = document.getElementById("jwt-token");
  const darkModeInput = document.getElementById("dark-mode");
  const saveMessage = document.getElementById("save-message");

  function getStorage() {
    if (typeof chrome === "undefined") {
      return null;
    }
    return chrome.storage && chrome.storage.local ? chrome.storage.local : null;
  }

  function normalizeServerUrl(value) {
    return (value || "").trim().replace(/\/+$/, "");
  }

  function isValidHttpUrl(value) {
    try {
      const parsed = new URL(value);
      return parsed.protocol === "http:" || parsed.protocol === "https:";
    } catch (_error) {
      return false;
    }
  }

  function setMessage(text, type) {
    saveMessage.textContent = text;
    saveMessage.classList.remove("is-error", "is-success");
    if (type) {
      saveMessage.classList.add(type === "error" ? "is-error" : "is-success");
    }
  }

  function loadSettings() {
    const storage = getStorage();
    if (!storage) {
      setMessage("Storage is unavailable in this context.", "error");
      return;
    }

    storage.get([SERVER_URL_KEY, JWT_KEY, DARK_MODE_KEY], (result) => {
      if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
        setMessage("Could not load settings.", "error");
        return;
      }

      serverUrlInput.value = result && result[SERVER_URL_KEY] ? result[SERVER_URL_KEY] : "";
      jwtInput.value = result && result[JWT_KEY] ? result[JWT_KEY] : "";
      if (darkModeInput) {
        darkModeInput.checked = Boolean(result && result[DARK_MODE_KEY]);
      }
    });
  }

  function saveSettings(event) {
    event.preventDefault();

    const storage = getStorage();
    if (!storage) {
      setMessage("Storage is unavailable in this context.", "error");
      return;
    }

    const serverUrl = normalizeServerUrl(serverUrlInput.value);
    const token = (jwtInput.value || "").trim();
    const darkModeEnabled = darkModeInput ? Boolean(darkModeInput.checked) : false;

    if (serverUrl && !isValidHttpUrl(serverUrl)) {
      setMessage("Server URL must start with http:// or https://.", "error");
      return;
    }

    if (!serverUrl && token) {
      setMessage("Enter a server URL when saving a JWT token.", "error");
      return;
    }

    storage.set(
      {
        [SERVER_URL_KEY]: serverUrl,
        [JWT_KEY]: token,
        [DARK_MODE_KEY]: darkModeEnabled,
      },
      () => {
        if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
          setMessage("Could not save settings.", "error");
          return;
        }
        setMessage("Settings saved.", "success");
      }
    );
  }

  if (form) {
    form.addEventListener("submit", saveSettings);
  }
  loadSettings();
})();
