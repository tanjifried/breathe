(function () {
  "use strict";

  const SERVER_URL_KEY = "breathe_server_url";
  const JWT_KEY = "breathe_jwt";
  const DARK_MODE_KEY = "breathe_dark_mode";
  const POSITION_KEY = "breathe_position";
  const CUSTOM_POS_KEY = "breathe_custom_pos";
  const TIMEOUT_DURATION_KEY = "breathe_timeout_duration";
  const DEFAULT_POSITION = "bottom-right";

  const form = document.getElementById("settings-form");
  const serverUrlInput = document.getElementById("server-url");
  const jwtInput = document.getElementById("jwt-token");
  const darkModeInput = document.getElementById("dark-mode");
  const timeoutDurationInput = document.getElementById("timeout-duration");
  const saveMessage = document.getElementById("save-message");
  const positionGrid = document.getElementById("pos-grid");
  const resetPositionButton = document.getElementById("reset-pos-btn");
  const authUsernameInput = document.getElementById("auth-username");
  const authPasswordInput = document.getElementById("auth-password");
  const authButton = document.getElementById("auth-btn");
  const pairingCodeInput = document.getElementById("pairing-code");
  const createCodeButton = document.getElementById("create-code-btn");
  const joinCodeButton = document.getElementById("join-code-btn");
  const generatedCodeElement = document.getElementById("generated-code");

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

  function setGeneratedCode(value) {
    if (!generatedCodeElement) {
      return;
    }

    const safeValue = typeof value === "string" && value ? value : "Not created yet";
    generatedCodeElement.textContent = safeValue;
  }

  function storageGet(storage, keys) {
    return new Promise((resolve, reject) => {
      try {
        storage.get(keys, (result) => {
          if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
            reject(new Error(chrome.runtime.lastError.message || "Storage read failed"));
            return;
          }
          resolve(result || {});
        });
      } catch (error) {
        reject(error);
      }
    });
  }

  function storageSet(storage, payload) {
    return new Promise((resolve, reject) => {
      try {
        storage.set(payload, () => {
          if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
            reject(new Error(chrome.runtime.lastError.message || "Storage write failed"));
            return;
          }
          resolve();
        });
      } catch (error) {
        reject(error);
      }
    });
  }

  function validateServerUrlForApi() {
    const serverUrl = normalizeServerUrl(serverUrlInput ? serverUrlInput.value : "");
    if (!serverUrl || !isValidHttpUrl(serverUrl)) {
      return null;
    }
    return serverUrl;
  }

  async function apiRequest(serverUrl, path, options) {
    const response = await fetch(`${serverUrl}${path}`, options || {});
    let payload = null;
    try {
      payload = await response.json();
    } catch (_error) {
      payload = null;
    }

    if (!response.ok) {
      const message = payload && payload.error ? payload.error : `Request failed (${response.status})`;
      throw new Error(message);
    }

    return payload || {};
  }

  async function getSavedServerAuth(storage) {
    const data = await storageGet(storage, [SERVER_URL_KEY, JWT_KEY]);
    const serverUrl = normalizeServerUrl(data[SERVER_URL_KEY]);
    const token = typeof data[JWT_KEY] === "string" ? data[JWT_KEY].trim() : "";

    if (!serverUrl || !isValidHttpUrl(serverUrl) || !token) {
      return null;
    }

    return { serverUrl, token };
  }

  async function authenticateAndSave() {
    const storage = getStorage();
    if (!storage) {
      setMessage("Storage is unavailable in this context.", "error");
      return;
    }

    const serverUrl = validateServerUrlForApi();
    if (!serverUrl) {
      setMessage("Enter a valid server URL first.", "error");
      return;
    }

    const username = (authUsernameInput && authUsernameInput.value ? authUsernameInput.value : "").trim();
    const password = (authPasswordInput && authPasswordInput.value ? authPasswordInput.value : "").trim();

    if (!username || password.length < 6) {
      setMessage("Enter username and a password of at least 6 characters.", "error");
      return;
    }

    try {
      let payload = null;
      try {
        payload = await apiRequest(serverUrl, "/api/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ username, password }),
        });
      } catch (error) {
        if (error && error.message === "Username already exists") {
          payload = await apiRequest(serverUrl, "/api/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
          });
        } else {
          throw error;
        }
      }

      const token = payload && typeof payload.token === "string" ? payload.token.trim() : "";
      if (!token) {
        throw new Error("Server did not return a token");
      }

      await storageSet(storage, {
        [SERVER_URL_KEY]: serverUrl,
        [JWT_KEY]: token,
      });

      if (jwtInput) {
        jwtInput.value = token;
      }

      setMessage("Account connected. You can now create or join a partner code.", "success");
    } catch (error) {
      setMessage(error && error.message ? error.message : "Could not connect account.", "error");
    }
  }

  async function createPairingCode() {
    const storage = getStorage();
    if (!storage) {
      setMessage("Storage is unavailable in this context.", "error");
      return;
    }

    try {
      const config = await getSavedServerAuth(storage);
      if (!config) {
        setMessage("Sign in first so we can generate a pairing code.", "error");
        return;
      }

      const payload = await apiRequest(config.serverUrl, "/api/pair", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${config.token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({}),
      });

      const pairingCode = payload && payload.pairingCode ? String(payload.pairingCode) : "";
      if (!pairingCode) {
        throw new Error("Server did not return a pairing code");
      }

      setGeneratedCode(pairingCode);
      setMessage(`Share code ${pairingCode} with your partner.`, "success");
    } catch (error) {
      setMessage(error && error.message ? error.message : "Could not create pairing code.", "error");
    }
  }

  async function joinWithPairingCode() {
    const storage = getStorage();
    if (!storage) {
      setMessage("Storage is unavailable in this context.", "error");
      return;
    }

    const pairingCode = (pairingCodeInput && pairingCodeInput.value ? pairingCodeInput.value : "")
      .trim()
      .replace(/\D/g, "");

    if (!/^\d{6}$/.test(pairingCode)) {
      setMessage("Enter a valid 6-digit partner code.", "error");
      return;
    }

    try {
      const config = await getSavedServerAuth(storage);
      if (!config) {
        setMessage("Sign in first before joining with a code.", "error");
        return;
      }

      await apiRequest(config.serverUrl, "/api/join", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${config.token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ pairingCode }),
      });

      setMessage("Linked to your partner successfully.", "success");
    } catch (error) {
      setMessage(error && error.message ? error.message : "Could not join with that code.", "error");
    }
  }

  function loadSettings() {
    const storage = getStorage();
    if (!storage) {
      setMessage("Storage is unavailable in this context.", "error");
      return;
    }

    storage.get([SERVER_URL_KEY, JWT_KEY, DARK_MODE_KEY, TIMEOUT_DURATION_KEY], (result) => {
      if (typeof chrome !== "undefined" && chrome.runtime && chrome.runtime.lastError) {
        setMessage("Could not load settings.", "error");
        return;
      }

      serverUrlInput.value = result && result[SERVER_URL_KEY] ? result[SERVER_URL_KEY] : "";
      jwtInput.value = result && result[JWT_KEY] ? result[JWT_KEY] : "";
      if (darkModeInput) {
        darkModeInput.checked = Boolean(result && result[DARK_MODE_KEY]);
      }
      if (timeoutDurationInput) {
        timeoutDurationInput.value = result && result[TIMEOUT_DURATION_KEY] ? result[TIMEOUT_DURATION_KEY] : "20";
      }
      setGeneratedCode("");
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
    let timeoutDuration = timeoutDurationInput ? parseInt(timeoutDurationInput.value, 10) : 20;
    
    if (isNaN(timeoutDuration) || timeoutDuration < 1) {
      timeoutDuration = 20;
    }

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
        [TIMEOUT_DURATION_KEY]: timeoutDuration,
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
  if (authButton) {
    authButton.addEventListener("click", authenticateAndSave);
  }
  if (createCodeButton) {
    createCodeButton.addEventListener("click", createPairingCode);
  }
  if (joinCodeButton) {
    joinCodeButton.addEventListener("click", joinWithPairingCode);
  }
  loadSettings();
})();
