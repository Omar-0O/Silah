---
name: design-system-sila
description: Reference and enforcement guide for Sila Design System, color tokens, Arabic/English typography, Soft UI depth, and Material 3 theme.
---

# Sila Visual Design System & Tokens

This skill defines the visual identity, tokens, and aesthetic principles for the Silah app.

## 1. Palette & Meaning (Color Tokens)
- **PrimaryGreen** (`0xFF1E5A35`): Deep luxurious olive representing family growth, trust, and life.
- **PrimaryGreenLight** (`0xFF94DAB2`): Calming pastel sage for dark mode and soft accents.
- **SecondaryGreyGreen** (`0xFF4C6B56`): Muted secondary text, outlines, and inactive icons.
- **SoftGold** (`0xFFE9CE79`): Warm gold reserved for milestones, primary calls-to-action, and priority reminders.
- **BackgroundSand** (`0xFFFAF9F5`): Warm paper/sand background in light mode (replaces harsh sterile white `#FFFFFF`).
- **DeepCharcoal** (`0xFF141816`): Warm charcoal base for dark mode surfaces.
- **SurfaceDark** (`0xFF1B221E`): Elevated dark surface with subtle olive undertone.

## 2. Typography Hierarchy (Arabic & English)
- Primary Font: **Almarai** (or **Cairo**) with native RTL balance.
- **Display H1** (`22.sp` to `24.sp`, `FontWeight.ExtraBold` or `Black`): Celebratory, onboarding, and milestone headers.
- **Header H2** (`16.sp` to `18.sp`, `FontWeight.Bold`): Dialog titles, card titles, section headers.
- **Body Large** (`14.sp`, `FontWeight.SemiBold`): Contact names, button labels.
- **Body Regular** (`12.sp` to `13.sp`, `FontWeight.Medium`): Descriptions, notes, relative status.
- **Caption / Meta** (`10.sp` to `11.sp`, `FontWeight.Normal`): Last contacted timestamp, reminder cadence.

## 3. Geometry & Soft Depth (Borders & Radii)
- **Cards & Dialogs**: `RoundedCornerShape(24.dp)` or `RoundedCornerShape(20.dp)` for a gentle, approachable look.
- **Text Fields**: `RoundedCornerShape(16.dp)` with soft container background (`0xFFF8FAFC` or surface variant).
- **Interactive Action Buttons**: `RoundedCornerShape(14.dp)` to `RoundedCornerShape(16.dp)`, minimum height `50.dp` to `54.dp`.
- **Soft Depth**: Prefer thin tinted borders (`BorderStroke(1.dp, color.copy(alpha = 0.2f))`) over heavy black drop shadows.

## 4. Internationalization & RTL
- Every layout must respect `LocalLayoutDirection.current`.
- Icons with direction (e.g. `ArrowBack`, `ArrowForward`) must use `Icons.AutoMirrored` to guarantee proper orientation in both Arabic (RTL) and English (LTR).
