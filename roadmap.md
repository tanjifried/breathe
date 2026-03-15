# Breathe — Browser Extension
### *calm lives between the words*

A Brave/Chrome extension for Facebook Messenger that gives couples a shared emotion regulation toolkit — built around four systems: Safe Word, Traffic Light, Structured Timeout, and Re-entry Script.

---

## Project name

**Breathe**

Alternative names: *Pacify*, *Stillpoint*, *Soften*

---

## What it does

Breathe injects a floating widget directly into Facebook Messenger. Both partners install the same extension. When emotions run high, either person can trigger one of four regulation tools without leaving the chat.

**Systems included:**
- ① Safe Word — calming overlay + breathing guide + countdown timer
- ② Traffic Light — personal emotional status indicator (green / yellow / red)
- ③ Structured Timeout — blurs the chat and starts a 20-minute cooldown
- ⑤ Re-entry Script — guided fill-in template ("I felt ___ when ___. I need ___ from you.")

---

## Architecture

### File structure

```
breathe/
├── manifest.json       ← extension config (MV3)
├── background.js       ← service worker
├── content.js          ← injected into messenger.com
├── overlay.js          ← shared overlay logic
├── calm.js             ← ① safe word feature
├── status.js           ← ② traffic light feature
├── timeout.js          ← ③ timeout feature
├── reentry.js          ← ⑤ re-entry script feature
├── styles.css          ← all widget styles
└── icons/
    ├── icon16.png
    ├── icon48.png
    └── icon128.png
```

### Layers

```
Browser (Brave)
└── manifest.json · service worker · chrome.storage API

    Content Script → injected into messenger.com
    └── DOM observer · UI injector · event listener

        Features
        ├── calm.js      (① safe word)
        ├── status.js    (② traffic light)
        ├── timeout.js   (③ timeout)
        └── reentry.js   (⑤ re-entry)

            Outputs
            └── floating widget · fullscreen overlay · timer UI · pre-filled chat text

Privacy: all data stays in chrome.storage.local — no server, no backend
```

---

## Development roadmap

### Phase 1 — Scaffold the extension
*Estimated time: 1–2 hours*

**Goal:** Get a working extension that loads on Messenger.

1. Create the `breathe/` folder with all files listed above (empty for now)
2. Write `manifest.json` with:
   - `manifest_version: 3`
   - Permissions: `activeTab`, `storage`, `scripting`
   - Host permission: `*://www.messenger.com/*`
   - Content script pointing to `content.js`
3. Write a minimal `content.js` that logs `"Breathe loaded"` to the console
4. Open Brave → `brave://extensions` → enable Developer Mode → click **Load unpacked** → select your `breathe/` folder
5. Open messenger.com, open DevTools console, confirm you see `"Breathe loaded"`

That console log is your "hello world." Don't move to Phase 2 until you see it.

**Key APIs:** `manifest_version`, `content_scripts`, `host_permissions`

---

### Phase 2 — Build the floating widget
*Estimated time: 2–3 hours*

**Goal:** A small floating pill that lives in the corner of Messenger and expands to show the four feature buttons.

1. In `content.js`, create a `<div id="breathe-widget">` and append it to `document.body`
2. Style it in `styles.css`:
   - Position: `fixed`, bottom-right corner, high `z-index` (e.g. `999999`)
   - Default state: a small pill with a leaf/wave icon
   - Expanded state: shows four buttons — Calm, Status, Timeout, Talk
3. Wire each button to call its respective module (`calm.js`, `status.js`, etc.)
4. Add a `MutationObserver` that watches the Messenger DOM — if Breathe's widget disappears (because Messenger's React re-renders), re-inject it automatically

> ⚠️ **Critical:** Messenger is a React app. It will regularly destroy your injected DOM. The `MutationObserver` in step 4 is not optional — it's what keeps the widget alive.

**Key APIs:** `MutationObserver`, `z-index management`, CSS injection via `web_accessible_resources`

---

### Phase 3 — ① Safe Word: calm overlay + timer
*Estimated time: 2 hours*

**Goal:** One button that covers the screen with a calming overlay and starts a countdown.

1. In `calm.js`: on button press, inject a fullscreen `<div>` overlay over the entire Messenger window
2. Overlay contents:
   - A short calming message (e.g. "Take a breath. You've got this.")
   - A CSS breathing animation — a circle that slowly expands and contracts
   - A 20-minute countdown timer displayed prominently
3. Timer counts down using `setInterval`. When it hits zero, show: "Ready to talk?" with a dismiss button
4. Dismiss button removes the overlay and returns the user to the chat

**Design note:** The overlay should feel calming, not clinical. Soft colors, large text, generous spacing.

**Key APIs:** `setInterval`, `CSS @keyframes`, overlay injection

---

### Phase 4 — ② Traffic Light: personal status indicator
*Estimated time: 1–2 hours*

**Goal:** Three colored buttons that let you silently declare your emotional state to yourself.

1. In `status.js`: inside the expanded widget panel, show three pill buttons — green, yellow, red
2. Clicking one sets your current status and saves it to `chrome.storage.local`
3. The selected color shows as a small dot on the collapsed widget pill — always visible as a self-reminder
4. Optional: if the user selects red, show a soft nudge: "Consider using the Timeout feature"

**Design note:** This is for personal awareness, not to send to your partner. It's a mirror, not a message.

**Key APIs:** `chrome.storage.local`, state management, badge dot on widget

---

### Phase 5 — ③ Timeout + ⑤ Re-entry Script
*Estimated time: 2 hours*

**Goal:** Two features that handle what happens during and after a cooldown.

**③ Timeout (`timeout.js`):**
1. On button press, apply `filter: blur(8px)` to the Messenger chat area — the conversation becomes unreadable
2. Display a 20-minute timer in the center of the blurred area
3. After 20 minutes, remove the blur and show: "Are you ready to reconnect?"
4. User clicks Yes → blur removed, state reset

**⑤ Re-entry Script (`reentry.js`):**
1. On button press, show a small panel above the widget with the fill-in template:
   ```
   I felt [         ] when [         ].
   I need [         ] from you.
   ```
2. User fills in each blank with a text input
3. "Send to chat" button assembles the completed sentence and injects it into Messenger's message input box using DOM manipulation
4. The extension focuses the input box — the user reviews the message and manually presses Enter to send

> ⚠️ **Important:** Never auto-send the re-entry message. The act of reviewing and choosing to send is part of the regulation process.

**Key APIs:** `CSS filter: blur`, `textarea DOM injection`, `input focus`, `InputEvent` dispatch

---

### Phase 6 — Polish, test, and share
*Estimated time: 2–3 hours*

**Goal:** A stable, shareable extension both partners can install.

1. Test all four features end-to-end on messenger.com — check different chat views, mobile-sized windows, and after long sessions
2. Add an options/settings page (`options.html`) where you can customize:
   - The timeout duration (default: 20 minutes)
   - The re-entry template text
   - The calming message shown on the overlay
3. Generate extension icons at 16px, 48px, and 128px — a simple leaf, wave, or breath mark works well
4. To share with your partner: zip the `breathe/` folder and send it. They unzip, open `brave://extensions`, enable Developer Mode, and click **Load unpacked**

**No store publishing needed.** This is a private tool for two people — sideloading is simpler and keeps it fully private.

---

## Total estimated build time

| Phase | Task | Time |
|-------|------|------|
| 1 | Scaffold | 1–2 hrs |
| 2 | Floating widget | 2–3 hrs |
| 3 | Safe word overlay + timer | 2 hrs |
| 4 | Traffic light | 1–2 hrs |
| 5 | Timeout + re-entry script | 2 hrs |
| 6 | Polish + sharing | 2–3 hrs |
| **Total** | | **10–14 hrs** |

If you use Claude to generate each module's code, this can be cut significantly. Just ask: *"Write the `calm.js` module for the Breathe extension"* — and provide the architecture above as context.

---

## Key technical notes

- **Brave is Chromium-based** — Chrome extension APIs work identically. Build for Chrome, it runs on Brave.
- **Manifest V3** is required for all modern Chromium extensions. Background scripts are now service workers.
- **No backend required.** All state lives in `chrome.storage.local`. Fully offline and private.
- **Messenger's React re-renders** will destroy your injected DOM. Always use a `MutationObserver` to re-inject.
- **DOM input injection** for the re-entry script requires dispatching a native `InputEvent` — React's synthetic event system won't pick up a simple `.value =` assignment.

---

## The golden rule

> Agree on which features to use, and practice triggering them, **when you are both calm** — never during conflict. The extension only works if both partners trust it.
