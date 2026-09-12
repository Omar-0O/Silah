---
name: web-ui-ux
description: Standards and guidelines for building premium, responsive, and accessible web pages, landing pages, and legal policies for Sila using Vanilla CSS and modern web design.
---

# Sila Web UI/UX Best Practices & Design Standards

This skill defines the technical and visual standards for web applications and standalone HTML pages in the Sila ecosystem (such as `privacy-policy.html`, landing pages, and web portals).

## 1. Visual Aesthetics & Sila Design Tokens
All web pages must align with the Sila Visual Identity:
- **Primary Colors**:
  - `PrimaryGreen`: `#1E5A35` (Deep olive for primary branding, headings, and accents).
  - `PrimaryGreenLight`: `#94DAB2` (Sage accent for dark mode and soft badges).
  - `SoftGold`: `#E9CE79` (Milestones, warm badges, and gold highlights).
- **Surface & Backgrounds**:
  - Light Mode: Warm sand paper `#FAF9F5`, pure card surfaces `#FFFFFF`, and soft green surface `#F0F6F2`.
  - Dark Mode: Deep charcoal `#141816`, elevated surface `#1B221E`, and container `#212A25`.
- **Soft Depth & Borders**:
  - Avoid harsh, heavy black shadows. Use subtle tinted borders (`rgba(30, 90, 53, 0.12)`) and multi-layer soft shadows.
  - Border Radiuses: `12px` (small badges/inputs), `18px-24px` (cards), and `999px` (pills and round action buttons).

## 2. Typography & Bilingual Support (RTL / LTR)
- **Arabic Typography**: Primary font is **Cairo** (or **Almarai**) via Google Fonts, ensuring balanced line height (`1.7` - `1.8`) and proper RTL legibility.
- **English Typography**: **Plus Jakarta Sans** (or **Inter**) for clean, modern readability.
- **Dynamic Direction**: Seamless toggling between `dir="rtl"` (Arabic) and `dir="ltr"` (English) without layout breakage. Directional icons must mirror automatically.

## 3. Ergonomics & Responsiveness
- **Touch Targets**: All interactive elements (buttons, pills, accordions) must have a minimum touch target of `48px` to `50px` on mobile devices.
- **Safe Areas & Insets**: Respect mobile viewports with `viewport-fit=cover`, sticky navigation with glassmorphism blur (`backdrop-filter: blur(12px)`), and comfortable padding on smaller screens.
- **Micro-Interactions**: Smooth CSS transitions (`cubic-bezier(0.16, 1, 0.3, 1)`), hover lift effects (`translateY(-2px)` to `-4px`), and immediate tactile feedback (toast notifications, clipboard copy feedback).

## 4. Theme Responsiveness (Light / Dark)
- Support system preference (`prefers-color-scheme`) by default, with manual user override stored in `localStorage`.
- High contrast and accessibility (WCAG AA compliant contrast ratios in both modes).

## 5. Performance & Self-Containment
- Prefer clean, semantic HTML5 (`<header>`, `<main>`, `<section>`, `<article>`, `<nav>`, `<footer>`).
- Embed lightweight, scalable SVGs for icons instead of heavy external icon libraries.
- Ensure dedicated print styles (`@media print`) for printable legal or policy documents.
