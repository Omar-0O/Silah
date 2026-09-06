---
name: compose-ui-ux
description: Guide and patterns for modern Android Jetpack Compose UI/UX, Edge-to-Edge window insets, smooth animations, touch ergonomics, and Material 3 design.
---

# Jetpack Compose UI/UX Best Practices & Architecture

This skill provides standards and patterns for building world-class, premium user interfaces in Android using Jetpack Compose.

## 1. Edge-to-Edge & System Insets
- **Mandatory Insets Awareness**: Always handle system bars properly when `enableEdgeToEdge()` is active:
  - Top headers / status bar: Use `Modifier.statusBarsPadding()` on header content to prevent clipping by notches, pinholes, or clock/battery icons.
  - Bottom bars & navigation buttons: Always apply `Modifier.navigationBarsPadding()` on bottom sheets, persistent action bars, and dialogs to prevent overlap with the system navigation pill or 3-button bar.
  - Soft Keyboard: Use `Modifier.imePadding()` on scrollable form containers so text fields and action buttons remain visible and accessible.

## 2. Layout Stability & Navigation
- **Stationary Controls**: Persistent controls (navigation buttons, tab bars, bottom bars) must NOT be nested inside page-transition `AnimatedContent` blocks to avoid jumping, flickering, or width jitter.
- **Back Button Ergonomics**: Animate back buttons using `AnimatedVisibility(visible = canGoBack, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally())` to keep layout transitions smooth.
- **Scrollable Viewports**: Always wrap form content in `.verticalScroll(rememberScrollState())` inside a weighted Box/Column so small phone screens or scaled fonts never cause overflow errors.

## 3. Motion & Micro-Interactions
- **Physics-Based Springs**: For responsive UI elements (like progress indicators, toggle buttons, and milestone badges), prefer spring animations:
  ```kotlin
  spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
  ```
- **Page Transitions**: Use cohesive directional transitions:
  ```kotlin
  transitionSpec = {
      if (targetState > initialState) {
          slideInHorizontally(tween(300)) { it } + fadeIn(tween(200)) togetherWith
              slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(200))
      } else {
          slideInHorizontally(tween(300)) { -it } + fadeIn(tween(200)) togetherWith
              slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200))
      }
  }
  ```
- **Haptic Feedback**: Pair meaningful actions (saving, completing a check-in, toggling status) with tactile feedback:
  ```kotlin
  val view = LocalView.current
  view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
  ```

## 4. Compose Performance & Recomposition
- Use stable keys in `items(list, key = { it.id })`.
- Remember expensive calculations (like phone normalization or grouped lists) with `remember(deps) { ... }`.
- Use `derivedStateOf` for values that depend on scroll state or fast-changing flows.
