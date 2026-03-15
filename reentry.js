(function () {
  "use strict";

  const modules = (window.BreatheModules = window.BreatheModules || {});

  const INPUT_SELECTORS = [
    'div[role="textbox"][contenteditable="true"]',
    'div[contenteditable="true"][aria-label*="Message"]',
    'div[contenteditable="true"][data-lexical-editor="true"]',
    'textarea[aria-label*="Message"]',
    "textarea",
  ];

  let panelElement = null;
  let fields = null;
  let feedbackElement = null;

  function findComposerInput() {
    for (const selector of INPUT_SELECTORS) {
      const node = document.querySelector(selector);
      if (node && node.offsetParent !== null) {
        return node;
      }
    }

    for (const selector of INPUT_SELECTORS) {
      const node = document.querySelector(selector);
      if (node) {
        return node;
      }
    }

    return null;
  }

  function updateFeedback(message, isError) {
    if (!feedbackElement) {
      return;
    }

    feedbackElement.textContent = message;
    feedbackElement.classList.toggle("is-error", Boolean(isError));
  }

  function composeMessage() {
    const felt = fields.felt.value.trim();
    const when = fields.when.value.trim();
    const need = fields.need.value.trim();

    if (!felt || !when || !need) {
      return null;
    }

    return `I felt ${felt} when ${when}. I need ${need} from you.`;
  }

  function setContentEditableValue(element, message) {
    element.focus();

    element.textContent = message;
    element.dispatchEvent(
      new InputEvent("input", {
        bubbles: true,
        cancelable: true,
        data: message,
        inputType: "insertText",
      })
    );
  }

  function setTextAreaValue(element, message) {
    element.focus();

    const descriptor = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value");
    if (descriptor && descriptor.set) {
      descriptor.set.call(element, message);
    } else {
      element.value = message;
    }

    element.dispatchEvent(new Event("input", { bubbles: true }));
  }

  function injectMessage() {
    const message = composeMessage();
    if (!message) {
      updateFeedback("Fill in all three blanks first.", true);
      return;
    }

    const input = findComposerInput();
    if (!input) {
      updateFeedback("Could not find the Messenger input. Click the chat box and try again.", true);
      return;
    }

    try {
      if (input.matches("textarea")) {
        setTextAreaValue(input, message);
      } else {
        setContentEditableValue(input, message);
      }
      input.focus();
      updateFeedback("Inserted into chat input. Review, then send manually.", false);
    } catch (_error) {
      updateFeedback("Could not inject message into this chat view.", true);
    }
  }

  function mountPanel(container) {
    panelElement = container;
    panelElement.innerHTML = "";

    const title = document.createElement("p");
    title.className = "breathe-subpanel-title";
    title.textContent = "Re-entry script";

    const rowOne = document.createElement("p");
    rowOne.className = "breathe-reentry-row";
    rowOne.textContent = "I felt";

    const feltInput = document.createElement("input");
    feltInput.className = "breathe-reentry-input";
    feltInput.type = "text";
    feltInput.placeholder = "emotion";
    rowOne.appendChild(feltInput);

    const rowTwo = document.createElement("p");
    rowTwo.className = "breathe-reentry-row";
    rowTwo.textContent = "when";

    const whenInput = document.createElement("input");
    whenInput.className = "breathe-reentry-input";
    whenInput.type = "text";
    whenInput.placeholder = "what happened";
    rowTwo.appendChild(whenInput);

    const rowThree = document.createElement("p");
    rowThree.className = "breathe-reentry-row";
    rowThree.textContent = "I need";

    const needInput = document.createElement("input");
    needInput.className = "breathe-reentry-input";
    needInput.type = "text";
    needInput.placeholder = "specific request";
    rowThree.appendChild(needInput);

    const footer = document.createElement("div");
    footer.className = "breathe-reentry-footer";

    const injectButton = document.createElement("button");
    injectButton.type = "button";
    injectButton.className = "breathe-primary-button";
    injectButton.textContent = "Send to chat";
    injectButton.addEventListener("click", injectMessage);

    feedbackElement = document.createElement("p");
    feedbackElement.className = "breathe-reentry-feedback";

    footer.append(injectButton, feedbackElement);
    panelElement.append(title, rowOne, rowTwo, rowThree, footer);

    fields = {
      felt: feltInput,
      need: needInput,
      when: whenInput,
    };
  }

  function togglePanel() {
    if (!panelElement) {
      return;
    }

    panelElement.classList.toggle("is-hidden");
  }

  function hidePanel() {
    if (!panelElement) {
      return;
    }

    panelElement.classList.add("is-hidden");
  }

  modules.reentry = {
    hidePanel,
    mountPanel,
    togglePanel,
  };
})();
