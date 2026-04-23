# Design System Document: Cyber-Obsidian Interface

## 1. Overview & Creative North Star
**Creative North Star: The Ethereal Terminal**
This design system moves away from the "clunky" aesthetic of traditional 1980s cyberpunk and instead embraces a high-end, "Obsidian-style" minimalism. It is a digital sanctuary that feels like a high-security vault floating in a void. 

To break the "template" look, we utilize **intentional asymmetry**. Layouts should not always be perfectly centered; instead, use wide-margin gutters and "holographic" overlays that bleed off the edge of the viewport. We treat the UI not as a flat screen, but as a series of projected light layers. The goal is a "Quiet Cyberpunk" aesthetic: high-tech, high-contrast, but sophisticated and orderly.

---

## 2. Colors
Our palette is anchored in the deep void of `#0D0D12` and brought to life through luminant energy tokens.

*   **Primary (`#8ff5ff` / `#00F0FF`):** Electric Cyan. This is your "Action" color. It represents active data streams and primary CTAs.
*   **Secondary (`#ad89ff` / `#9B6DFF`):** Vibrant Purple. Used for encryption-related elements, secondary actions, and high-level security status.
*   **Surface Hierarchy (Nesting):** 
    *   **Background (`#0e0e13`):** The base "Obsidian" floor.
    *   **Surface-Container-Low:** Used for large structural areas like sidebar backgrounds.
    *   **Surface-Container-Highest (`#25252c`):** Used for elevated interactive elements.
*   **The "No-Line" Rule:** Do not use `1px solid` opaque borders to separate sections. Use background color shifts (e.g., a `surface-container-low` list sitting on a `surface` background).
*   **The "Glass & Gradient" Rule:** All primary cards must use `rgba(255,255,255,0.05)` with a `backdrop-filter: blur(12px)`. Main CTAs should feature a subtle linear gradient from `primary` to `primary_container` to simulate a glowing light-bar effect.

---

## 3. Typography
The typography strategy contrasts the "Technical/Futuristic" with the "Functional/Human."

*   **Display & Headlines (`Space Grotesk`):** While the prompt suggested Orbitron, we utilize **Space Grotesk** for its superior legibility at high-end editorial scales. It provides a "tech-intellectual" vibe without looking like a video game. Use `headline-lg` for vault titles with a `0.05em` letter-spacing.
*   **Titles & Body (`Inter`):** Inter provides a grounded, neutral balance. It ensures that complex passwords and technical data are instantly readable.
*   **Labels:** Use `label-sm` in `all-caps` with `0.1em` tracking for a "Terminal Command" look on metadata (e.g., LAST SYNCED, ENCRYPTION LEVEL).

---

## 4. Elevation & Depth
In this system, depth is a product of light and transparency, not physical shadows.

*   **The Layering Principle:** Stack `surface-container` tiers to create hierarchy. A password detail view should be a `surface-container-highest` glass card floating over a `surface-dim` background.
*   **Ambient Shadows:** Avoid black shadows. Use `primary` or `secondary` colors at 5% opacity with a `40px` blur to create a "Neon Glow" instead of a "Drop Shadow."
*   **The "Ghost Border":** For container definition, use `outline_variant` at 15% opacity. This creates a "holographic wireframe" feel rather than a solid box.
*   **Signature Texture:** Apply a global background pattern of hex grids at 5% opacity. This pattern should remain static while the glass cards "float" above it during scroll.

---

## 5. Components

### Buttons
*   **Primary:** High-glow Cyan background (`primary`). Text in `on_primary`. Apply a `box-shadow: 0 0 15px rgba(0, 240, 255, 0.4)`.
*   **Ghost (Tertiary):** No background. `outline_variant` (15% opacity) border. Text in `primary`. On hover, the background fills to 5% opacity.

### Holographic Inputs
*   **Style:** No solid background. Use a bottom-border only (`primary` at 30% opacity). 
*   **Interaction:** On focus, the bottom border glows at 100% opacity and a faint cyan vertical "scanline" (1px wide) pulses at the start of the input.

### Glass Cards
*   **Composition:** `rgba(255,255,255,0.05)` fill, `12px` backdrop-blur, and a `1px` border using a gradient of `rgba(255,255,255,0.2)` to `transparent`.
*   **Layout:** No dividers. Separate content using `spacing-6` (1.3rem) vertical gaps.

### Chips (Security Tags)
*   **Style:** Small, `none` or `sm` roundedness. 
*   **Status:** Use `secondary` (Purple) for "High Security" and `error` (Red-Coded) for "Compromised."

### Password Strength Meter
*   **Visual:** Instead of a bar, use a series of 5 vertical "Power Cells" (rectangles). As strength increases, cells fill with a `primary` glow.

---

## 6. Do's and Don'ts

### Do:
*   **Do** use asymmetrical spacing. Allow some elements to be "off-grid" to create a bespoke, high-end feel.
*   **Do** use thin-stroke icons (1px or 1.5px weight).
*   **Do** utilize `surface-container-lowest` (#000000) for "Heavy" modal backdrops to create total focus.

### Don't:
*   **Don't** use 100% opaque borders or dividers. It breaks the "Holographic" illusion.
*   **Don't** use standard "Material" rounded corners. Stick to `sm` (0.125rem) or `none` for a sharper, more aggressive tech feel.
*   **Don't** use pure grey for text. Use `on_surface_variant` which is slightly tinted to maintain the obsidian atmosphere.
*   **Don't** over-glow. If everything glows, nothing is important. Reserve the `primary` glow for the most critical user action.