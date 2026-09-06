# Front-End & Jetpack Compose Development Rules

These rules apply to all UI, Compose, and front-end development across the Silah codebase:

1. **System Insets & Edge-to-Edge**:
   - Always apply `Modifier.statusBarsPadding()` on top headers/app bars.
   - Always apply `Modifier.navigationBarsPadding()` on bottom navigation rows, dialog bottom controls, and bottom sheets.
   - Never let interactive buttons collide with the device navigation pill or gesture bar.

2. **Visual Consistency (Sila Palette)**:
   - Use tokens from `Color.kt` and `DESIGN_SYSTEM.md`: `PrimaryGreen` (`0xFF1E5A35`), `SoftGold` (`0xFFE9CE79`), `BackgroundSand` (`0xFFFAF9F5`).
   - Use gentle corner radiuses (`14.dp` - `24.dp`) for cards, text fields, and buttons.

3. **Motion & Stability**:
   - Keep navigation controls and bottom action bars stable outside animated transitions.
   - Use `AnimatedVisibility` for conditionally shown controls.
   - Mirror directional icons with `Icons.AutoMirrored` for proper RTL Arabic display.

4. **Touch & Ergonomics**:
   - Buttons must have minimum height of `50.dp` for effortless mobile tapping.
   - Include tactile feedback (`LocalView.current.performHapticFeedback`) for meaningful user actions.
