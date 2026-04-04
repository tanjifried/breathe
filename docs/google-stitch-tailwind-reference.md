# Google Stitch Tailwind Reference Digest

This file distills the provided Stitch/Tailwind mockups into a reusable design reference for Breathe.

## Source Intent

The provided mockups define a more mature, editorial, emotionally grounded visual system for the Android app.

The dominant direction is:

- softer than the current Compose scaffold
- more premium than the extension widget
- more spacious and immersive
- still warm, non-clinical, and regulation-first

## Typography Direction

Use these pairings as the preferred visual language:

- Headlines: `Newsreader`
- Body and labels: `Plus Jakarta Sans`

Desired feel:

- serif headlines for warmth, reflection, and softness
- clean sans-serif body text for clarity and calm readability

## Color System Direction

The mockups suggest the Android visual system should evolve toward a richer Material-like token set while preserving the sage/cream identity.

Primary colors:

- `primary`: `#53643a`
- `primary-container`: `#879a6b`
- `secondary`: `#775a00`
- `secondary-container`: `#fccf60`
- `tertiary`: `#8f4b3f`
- `tertiary-container`: `#ce7e70`

Surface colors:

- `background`: `#fbfbe2`
- `surface`: `#fbfbe2`
- `surface-container`: `#efefd7`
- `surface-container-low`: `#f5f5dc`
- `surface-container-high`: `#eaead1`
- `surface-container-highest`: `#e4e4cc`

Text colors:

- `on-surface`: `#1b1d0e`
- `on-surface-variant`: `#46483c`
- `outline`: `#76786b`

Interpretation:

- keep the existing sage and cream identity
- add a warmer ochre/yellow secondary system
- use a muted clay/rose tertiary for timeout or stronger states
- maintain low-contrast, paper-like surfaces

## Shape Direction

The mockups lean more heavily into soft, generous curvature than the current Compose shell.

Suggested shape language:

- default radius: `1rem`
- large cards: `2rem`
- highly emphasized surfaces: `3rem`
- pill actions: fully rounded

This should feel cocoon-like and touch-friendly, not sharp or dashboard-like.

## Core Screen Patterns

### Home

The mockup home screen introduces:

- a large hero card for live state
- emotional-weather framing instead of plain status text
- side-by-side self and partner state cards
- rounded icon-based regulation tool grid
- a reflection quote block
- small “insight bento” surfaces
- persistent bottom navigation

### Status Check-in

The mockup reframes status as an emotionally led selection flow:

- clear hero heading
- live-state context card
- large stacked choice cards for Green / Yellow / Red
- a contextual nudge panel that changes based on state
- a single explicit CTA

### Structured Timeout

The mockup adds:

- a timer-centric hero
- circular countdown emphasis
- lock-state cards
- re-entry framing
- soft explanatory containment text

This should feel protective and bounded, never punitive.

### Calm Session

The mockup adds:

- a breathing-orb/timer focal point
- asymmetric layout with session details beside the timer
- calm, premium visual emphasis on breath and time
- a stronger primary CTA and softer secondary action

### Quick Updates

The mockup also suggests a future “Quick Updates” or low-pressure relational ping surface:

- short preset updates
- custom note input
- recent update context
- compact, warm sharing UI

This can inform future work even if the screen is not yet implemented in Android.

## Navigation Direction

The mockups consistently use:

- fixed top app bar
- centered brand mark/title
- fixed rounded bottom nav
- 4-tab or 4-action bottom destinations

The Android app should move toward this shell as features stabilize.

## Interaction Direction

Buttons and touch targets in the mockups feel:

- soft
- rounded
- centered
- card-based
- icon-led where appropriate

Interactions should prioritize:

- visible touch comfort
- spacious spacing
- calm transitions
- single clear CTA per section

## Implementation Guidance For Agents

When translating these mockups into Android Compose:

- preserve the emotional tone over pixel-perfect HTML translation
- adapt the token system into Compose theme primitives
- prefer reusable shells and cards over one-off styling
- keep MVI and clean architecture boundaries intact
- do not let visual redesigns break offline-first flows

## Files To Treat As Design Sources Of Truth

- `docs/google-stitch-project-brief.md`
- `docs/google-stitch-android-ui-bundle.md`
- `docs/google-stitch-tailwind-reference.md`
- `styles.css`
