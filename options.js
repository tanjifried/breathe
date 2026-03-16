(function () {
  "use strict";

  const SERVER_URL_KEY = "breathe_server_url";
  const JWT_KEY = "breathe_jwt";
  const DARK_MODE_KEY = "breathe_dark_mode";
  const POSITION_KEY = "breathe_position";
  const CUSTOM_POS_KEY = "breathe_custom_pos";
  const DEFAULT_POSITION = "bottom-right";

  const form = document.getElementById("settings-form");
  const serverUrlInput = document.getElementById("server-url");
  const jwtInput = document.getElementById("jwt-token");
  const darkModeInput = document.getElementById("dark-mode");
  const saveMessage = document.getElementById("save-message");
  const positionGrid = document.getElementById("pos-grid");
  const resetPositionButton = document.getElementById("reset-pos-btn");

  function getPositionButtons() {
    return Array.from(document.querySelectorAll(".pos-btn"));
  }

  function decoratePositionButton(button, isActive) {
    if (!button) {
      return;
    }

    const label = button.textContent.replace(/\s*\u2713$/, "");
    button.classList.toggle("is-active", isActive);
    button.textContent = isActive ? `${label} \u2713` : label;
  }

  function markActivePosition(position) {
    getPositionButtons().forEach((button) => {
      decoratePositionButton(button, button.dataset.pos === position);
    });
  }

  function wirePositionControls(storage) {
    if (!positionGrid || !resetPositionButton) {
      return;
    }

    positionGrid.addEventListener("click", (event) => {
      const button = event.target.closest(".pos-btn");
      if (!button) {
        return;
      }

      const selectedPosition = button.dataset.pos || DEFAULT_POSITION;
      storage.set(
        {
          [POSITION_KEY]: selectedPosition,
          [CUSTOM_POS_KEY]: null,
        },
        () => {
          if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
            setMessage("Could not update widget position.", "error");
            return;
          }
          markActivePosition(selectedPosition);
          setMessage("Widget position updated.", "success");
        }
      );
    });

    resetPositionButton.addEventListener("click", () => {
      storage.set(
        {
          [POSITION_KEY]: DEFAULT_POSITION,
          [CUSTOM_POS_KEY]: null,
        },
        () => {
          if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
            setMessage("Could not reset widget position.", "error");
            return;
          }
          markActivePosition(DEFAULT_POSITION);
          setMessage("Widget position reset to default.", "success");
        }
      );
    });
  }

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

    storage.get([POSITION_KEY], (result) => {
      if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
        return;
      }
      const savedPosition =
        result && typeof result[POSITION_KEY] === "string" ? result[POSITION_KEY] : DEFAULT_POSITION;
      markActivePosition(savedPosition);
    });

    wirePositionControls(storage);
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
