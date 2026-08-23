# 🎨 Software Requirements & UI/UX Design Specification (SRS & Design System)
## Application: "Silah" (صِلَةِ) — Kinship Ties & Family Connection Native Android App
### Optimized for AI UI Design Generators (Google Stitch, Figma, & Jetpack Compose)

---

| **Document Attribute** | **Specification Details** |
| :--- | :--- |
| **Project Name** | **Silah (صِلَةِ)** — Kinship Ties Tracker |
| **Document Version** | **v2.1.0 (Corrected & Enhanced from Source Code Review)** |
| **Primary Focus** | Comprehensive System SRS + Design System & UI Generation Prompts for **Stitch** |
| **Target Platforms** | Native Android (Jetpack Compose, Material 3, Soft-UI) |
| **Language** | English (with Arabic localization tokens & typography references) |
| **Target Tools** | Google Stitch, Figma, Jetpack Compose UI Engine |

---

## 📖 Table of Contents
1. [Executive Summary & Purpose](#1-executive-summary--purpose)
2. [Product Vision & Core Value Proposition](#2-product-vision--core-value-proposition)
3. [Design Philosophy & Visual Tokens (Design System)](#3-design-philosophy--visual-tokens-design-system)
   - 3.1 App Icon Architecture & Brand Motif
   - 3.2 Master Color Palette (Light & Dark Themes)
   - 3.3 Typography & Dynamic Font System
   - 3.4 Geometry, Radii, Depth & Shadows
   - 3.5 Status Color Matrix & Indicator Tokens
4. [Detailed Screen-by-Screen UI/UX Specifications](#4-detailed-screen-by-screen-uiux-specifications)
   - 4.1 Screen 1: Spiritual Splash Screen & Kinship Knot
   - 4.2 Screen 2: Onboarding Wizard (3-Step Guided Flow)
   - 4.3 Screen 3: Main Dashboard (Commitment Arc & Due Carousel)
   - 4.4 Screen 4: Relatives Management Tab & Filtering
   - 4.5 Screen 5: Relative Detail & Timeline View
   - 4.6 Screen 6: Smart Contact Importer & Relative Setup Dialog
   - 4.7 Screen 7: Activity Log Bottom Sheet
   - 4.8 Screen 8: Family Memories & Greeting Templates Tab
   - 4.9 Screen 9: Live Smart Home Screen Widget (4x2 Layout)
   - 4.10 Screen 10: Settings & Font Picker Modal
5. [AI UI Generator (Stitch) Prompts & Instructions](#5-ai-ui-generator-stitch-prompts--instructions)
6. [Functional System Requirements](#6-functional-system-requirements)
7. [System Architecture & Data Schema](#7-system-architecture--data-schema)
8. [Non-Functional & Privacy Requirements](#8-non-functional--privacy-requirements)

---

## 1. Executive Summary & Purpose

### 1.1 Purpose
This specification serves a dual purpose:
1. **Software Requirements Specification (SRS)**: Complete functional, structural, and technical guidelines for building the 100% Local-First Native Android app **Silah**.
2. **UI/UX Design Master Blueprint**: Detailed design system, component layouts, visual hierarchy, color tokens, micro-interactions, and ready-to-use **Stitch / AI Design Prompts** to generate world-class UI mockups.

### 1.2 Target Platform & Stack
- **UI Framework**: Jetpack Compose (Material Design 3, Soft-UI Aesthetic).
- **Architecture**: MVVM + Repository Pattern.
- **Data Layer**: Room Database (SQLite ORM) + Local Storage Access Framework (SAF).
- **Background Engine**: Android WorkManager + CallLogManager API.

---

## 2. Product Vision & Core Value Proposition

**Silah (صِلَةِ)** is a privacy-first, local-first Android mobile application designed to revive and nurture Islamic family ties and kinship (*Silat Al-Rahim*).

```text
               ┌─────────────────────────────────────────┐
               │         "Silah" Product Core            │
               └────────────────────┬────────────────────┘
                                    │
       ┌────────────────────────────┼────────────────────────────┐
       ▼                            ▼                            ▼
┌──────────────┐            ┌──────────────┐            ┌──────────────┐
│  Automatic   │            │ Smart Degree │            │  100% Privacy│
│  Call Tracking│            │  Reminders   │            │ Local-First  │
└──────────────┘            └──────────────┘            └──────────────┘
```

- **Zero Effort Tracking**: Automatically detects phone calls to/from relatives and updates relationship timestamps.
- **Warm & Non-Guilt-Inducing UI**: Encourages loving connection using soft pastel tones, progress arcs, and warm cultural typography instead of harsh red alarm badges.
- **Degree-Aware Intelligence**: Differentiates notification tone and relationship intervals based on kinship tier (*Parents*, *Siblings*, *Uncles & Aunts*, *Extended Family*).

---

## 3. Design Philosophy & Visual Tokens (Design System)

### 3.1 App Icon Architecture & Brand Motif: "The Kinship Knot"
- **Concept**: Moving away from overused heart or phone icons to **"The Infinity Kinship Knot"**. Interwoven Bezier curves with 3 central glowing nodes representing roots (Parents), present connections (Siblings/Relatives), and future bonds.
- **Visual Motif**: Dual intersecting rings in Champagne Gold (`#E9CE79`) and Deep Royal Olive (`#1E5A35`).

```text
           ( Node 1: Parents / Roots )
                       / \
                      /   \
  ( Node 2: Siblings ) ═══ ( Node 3: Extended )
           \             /
            \___________/
        [ The Kinship Knot Loop ]
```

### 3.2 Master Color Palette (Light & Dark Themes)

```kotlin
// Master Tokens (Color.kt)
val PrimaryGreen       = Color(0xFF1E5A35) // Deep Olive Green (Growth, Heritage, Stability)
val PrimaryGreenLight  = Color(0xFF94DAB2) // Soft Sage Emerald (Dark Theme Primary Accent)
val SecondaryGreyGreen = Color(0xFF4C6B56) // Muted Greyish Olive (Subtitles & Secondary Chips)
val SoftGold           = Color(0xFFE9CE79) // Warm Champagne Gold (Highlights, Badges, Commitment Ring)
val BackgroundSand     = Color(0xFFFAF9F5) // Warm Alabaster Sand (Light Theme Background - Glare-free)
val DeepCharcoal       = Color(0xFF141816) // Deep Charcoal Tinted Olive (Dark Theme Base Background)
val SurfaceDark        = Color(0xFF1B221E) // Soft Glare-Free Dark Surface (Cards & Dialogs)
val SurfaceLight       = Color(0xFFFFFFFF) // Pure White Card Base (Light Mode)
```

#### Color Mapping Matrix

| Design Token | Light Mode Value | Dark Mode Value | Usage Context |
| :--- | :--- | :--- | :--- |
| `colorScheme.primary` | `#1E5A35` (Deep Olive) | `#94DAB2` (Sage Emerald) | Main Action Buttons, Header Titles, Active Tabs |
| `colorScheme.secondary` | `#E9CE79` (Champagne Gold) | `#E9CE79` (Champagne Gold) | Badges, Commitment Ring, Highlights |
| `colorScheme.background` | `#FAF9F5` (Warm Sand) | `#141816` (Deep Charcoal) | Full Screen Canvas Background |
| `colorScheme.surface` | `#FFFFFF` (Pure White) | `#1B221E` (Soft Dark Charcoal) | Cards, Sheets, Dialog Containers |
| `colorScheme.onSurface` | `#141816` (Dark Charcoal) | `#F0F4F2` (Soft White) | Primary Body Text & Headings |

### 3.3 Typography & Dynamic Font System
- **Supported Arabic Typefaces**:
  1. **Cairo**: Default modern balanced typeface (high readability across age groups).
  2. **Thamanyah OS**: Cultural, high-end editorial typeface.
  3. **Almarai**: Soft, curved contemporary typeface.
  4. **Tajawal**: Geometric, crisp modern typeface.

#### Type Hierarchy Tokens

```text
Display H1   │ 24sp │ Black (900)   │ LineHeight: 32sp │ Greetings, Hadith Quotes
Header H2    │ 16sp │ Bold (700)    │ LineHeight: 22sp │ Card Titles, Section Headers
Body Text    │ 13sp │ Medium (500)  │ LineHeight: 18sp │ Notes, Relative Names, Time Elapsed
Caption/Label│ 11sp │ Normal (400)  │ LineHeight: 14sp │ Status Badges, Call Log Types
```

### 3.4 Geometry, Radii, Depth & Shadows
- **Card & Dialog Radius**: `24.dp` to `26.dp` continuous squircle corners.
- **Input Fields & Search**: `16.dp` rounded capsule shapes.
- **Interactive Chips & Buttons**: `14.dp` soft rounded edges.
- **Soft Ambient Elevation**:
  - *No harsh drop shadows.* Use 1dp ambient border stroke with 15% opacity primary green/gold:
  - Light Mode Border: `BorderStroke(1.dp, Color(0xFFE9CE79).copy(alpha = 0.3f))`
  - Shadow Tint: `Color(0x0A000000)` (Soft 4% blur shadow).

### 3.5 Status Color Matrix & Indicator Tokens

> [!IMPORTANT]
> The app implements **5 distinct status levels** (not 4) via a `RelativeStatus` enum. The emoji-only pill approach is intentional — it avoids overwhelming users with text-heavy warnings.

| Status Enum | Emoji Pill | Color (Light) | Color (Dark) | Countdown Text Example | Trigger Condition |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CONNECTED` | ✅ | `#2E7D32` | `#81C784` | `"✅ باقي 5 أيام على الموعد"` | Remaining days > 1 |
| `OK_SOON` | 🕐 | `#FBC02D` | `#FFF176` | `"🕐 غداً هو الموعد!"` | Remaining == 1 day |
| `NEEDS_CONTACT` | 🔔 | `#E65100` | `#FFB74D` | `"🔔 اليوم موعد التواصل"` | Remaining == 0 days |
| `NEEDS_CONTACT_URGENT` | ⚠️ | `#BF360C` | `#FF8A65` | `"🔴 تأخرت 2 أيام"` | Overdue 1–3 days |
| `OVERDUE_CRITICAL` | 🔴 | `#C62828` | `#E57373` | `"🔴 تأخرت 7 أيام"` | Overdue > 3 days |

**Urgency Score Formula** (used in Widget ranking):
```text
urgencyScore = (daysSinceContact / contactIntervalDays) × 100
```
The relative with the highest urgency score is pinned first in the Widget and the Due Carousel.

---

## 4. Detailed Screen-by-Screen UI/UX Specifications

---

### 4.1 Screen 1: Spiritual Splash Screen & Kinship Knot
- **Layout Type**: Full-screen centered hero layout with subtle organic glow background.
- **Visual Elements**:
  - Top/Center: Animated Vector canvas of **The Kinship Knot** pulsing smoothly (scale 0.9x to 1.05x).
  - Center: Typography header `"صلة"` in bold script, subtitle `"وَأَنْ يُنْسَأَ لَهُ فِي أَثَرِهِ، فَلْيَصِلْ رَحِمَهُ"`.
  - Bottom: Soft loading indicator with Sage Green progress bar.
- **Color Palette**: Background `#141816` (Dark Charcoal), Gold accent `#E9CE79`, Olive `#94DAB2`.

```text
┌──────────────────────────────────────────────────────────┐
│                                                          │
│                     ✨ [ Kinship Knot ] ✨               │
│                        (Pulsing Animation)               │
│                                                          │
│                            صِلَةِ                        │
│             «مَنْ سَرَّهُ أَنْ يُبْسَطَ لَهُ فِي رِزْقِهِ...»  │
│                                                          │
│                      [ ═══════════ ]                     │
│                         Loading...                       │
└──────────────────────────────────────────────────────────┘
```

---

### 4.2 Screen 2: Onboarding Wizard (4-Page Guided Flow)

> [!NOTE]
> The onboarding has **4 pages**, not 3. Each page uses a split-screen layout: a **Teal Gradient header** (`#0E7075 → #094E51`) on top with the app logo in a white circular badge, and a **white content panel** on the bottom.

- **Page 0 — Language Selection**: Two large radio options: `العربية` and `English`. Amber-colored (`#D97706`) radio buttons. Selecting a language immediately switches the entire onboarding to that language (full RTL ↔ LTR switch).
- **Page 1 — User Profile Setup (Name + Gender)**:
  - `OutlinedTextField` for the user's name (`border: #0E7075` when focused).
  - Two **Gender Selection Cards** side by side:
    - Male card: Teal-bordered (`#0E7075`) with `UserAvatarCharacter(gender="male")` illustration and `RadioButton`.
    - Female card: Pink-bordered (`#E91E63`) with `UserAvatarCharacter(gender="female")` illustration and `RadioButton`.
  - On "Next", saves `userName` and `userGender` to ViewModel/SharedPrefs.
- **Page 2 — Auto Call Tracking**: Feature explanation card for automatic call log synchronization.
- **Page 3 — Smart Reminders**: Feature explanation for degree-aware smart notifications.
- **Navigation**: Back/Forward circle buttons (`#E2E8F0` background) + wide pill `"التالي"` / `"ابدأ الآن 🌸"` button in Teal (`#0E7075`). 4-dot page indicator at the bottom with active dot in Amber Gold (`#D97706`).

---

### 4.3 Screen 3: Main Dashboard (Commitment Arc & Due Carousel)
- **Top Bar**: App logo, Dynamic Arabic Date, Font/Theme switcher, Settings gear icon.
- **Header Component — Commitment Header Card (`CommitmentHeaderCard.kt`)**:
  - Soft curved card (`24.dp` radius) in Deep Olive (`#1E5A35`) with background pattern.
  - Left/Center: **Circular Progress Arc (Commitment Ring)** in Warm Gold (`#E9CE79`) showing weekly connection percentage (e.g., `85%`).
  - Right: Greeting `"أهلاً بك 🌿"`, status message `"أنجزت 6 من 7 اتصالات هذا الأسبوع!"` (Completed 6 of 7 contacts this week).
- **Horizontal Carousel — "Due for Contact" (`DueRelativesCarousel.kt`)**:
  - Horizontal scrolling row of compact elevated cards.
  - Displays relatives with status 🟠 Due Today or 🔴 Overdue.
  - Avatar, Name, Relationship Degree, Countdown badge (`"تأخرت يومين ⏱️"`), and direct Phone Call action button.
- **Filter Tabs**: Pill-shaped horizontal selector chips: `[الكل]` `[الوالدان]` `[الأشقاء]` `[الأعمام والأخوال]` `[أقارب آخرون]`.
- **Search Bar**: Capsule-shaped text field with search icon and clear button.
- **Relative Cards List**: Vertical list of full cards (`RelativeCard.kt`).

```text
┌──────────────────────────────────────────────────────────┐
│ 🌸 صِلَةِ                   📅 23 أغسطس     🎨 🌙 ⚙️  │
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │  ┌──────┐   أهلاً بك 🌿                               │ │
│ │  │ 85%  │   نسبة الالتزام الأسبوعية: ممتاز           │ │
│ │  └──────┘   أنجزت 6 من 7 اتصالات هذا الأسبوع!        │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                          │
│  🔴 يحين موعد التواصل معهم قريباً                        │
│ ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│ │  ( 👵 )      │  │  ( 👨 )      │  │  ( 🧕 )      │     │
│ │ 🧓 أمي        │  │ 👨 خالو أحمد  │  │ 👩 أختي سارة │     │
│ │ 🔴 حان اليوم │  │ 🟠 غداً      │  │ 🔴 تأخرت 3أيام│     │
│ └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                          │
│ [ الكل ]  ( الوالدان )  ( الأشقاء )  ( الأعمام والأخوال )│
│                                                          │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ ( 👵 ) أمي — الوالدان                    🟢 بخير      │ │
│ │ 📞 آخر تواصل: منذ ساعتين | الهاتف: 0101234567       │ │
│ │ [ 📞 اتصل الآن ]            [ 📝 تسجيل تواصل ]       │ │
│ └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

---

### 4.4 Screen 4: Relatives Management Tab & Filtering
- **Function**: Full directory of saved relatives with rich sorting (Urgency, Alphabetical, Relationship Tier).
- **Card Anatomy (`RelativeCard.kt`)**:
  - **Avatar Circle**: 52dp gradient circle with the relative's **first initial** as a letter avatar. Background gradient is chosen from a 6-color palette deterministically based on `name.hashCode()`, covering Deep Teal, Warm Amber, Ocean Blue, Royal Purple, Rose, and Leaf Green tones. If the user has set a custom emoji avatar via `AvatarPickerSheet`, the emoji is shown instead.
  - **Name**: Bold 16sp, max 1 line with ellipsis.
  - **Relationship Degree**: 12sp Muted Olive subtitle.
  - **Status Pill**: Emoji-only pill (`✅ 🕐 🔔 ⚠️ 🔴`) with a soft tinted background matching the status color.
  - **Countdown Bar**: Full-width colored text row below the avatar showing e.g. `"✅ باقي 5 أيام على الموعد"` alongside a small chip `"/14ي"` (target interval indicator).
  - **Last Contact Line**: `"آخر تواصل: منذ ساعتين ⏱️"` in faint muted text (11sp).
  - **Bottom Action Bar**: Quick action row with **5 items** (not 3):
    1. `اتصال` — Primary Call Button (Deep Olive pill, `Intent.ACTION_DIAL`)
    2. `WhatsApp` — WhatsApp Intent Button (WhatsApp Green `#1B8A4A`, auto-formats international number)
    3. `سجّل` — Log Communication icon chip (Gold tint `#E9CE79`)
    4. `تعديل` — Edit Relative icon chip (Muted Olive tint)
    5. `حذف` — Delete with confirmation AlertDialog (Soft Red `#D32F2F`)

---

### 4.5 Screen 5: Relative Detail & Interactive Timeline View
- **Header**: Large profile avatar with golden border, name, relationship degree, and notes.
- **Visual Connection Timeline**:
  - Dotted vertical line in muted olive green.
  - Timeline nodes with pulsing action icons (Phone call 📞, Visit 🏡, Gift 🎁, Message 💬).
  - Time label, duration of call, and user-entered notes for each interaction.

```text
┌──────────────────────────────────────────────────────────┐
│ ←  الملف الشخصي للقريب                          ✏️  🗑️ │
├──────────────────────────────────────────────────────────┤
│                       ( 👵 )                             │
│                      والدتي الحبيبة                      │
│                  [ الوالدان | تذكير كل 3 أيام ]          │
│                                                          │
│ 📜 سجل التواصل والذكريات                                 │
│                                                          │
│   ● 📞 مكالمة هاتفية (مزامنة تلقائية)                     │
│   │   منذ ساعتين (مدة المكالمة: 12 دقيقة)                 │
│   │                                                      │
│   ● 🏡 زيارة عائلية                                      │
│   │   منذ 4 أيام — "زرنا أمي وقدمنا لها الهدية"          │
│   │                                                      │
│   ● 💬 رسالة نصية                                        │
│       منذ أسبوع — "تهنئة يوم الجمعة"                     │
└──────────────────────────────────────────────────────────┘
```

---

### 4.6 Screen 6: Smart Contact Importer & Setup Dialog
- **Component**: `ImportContactsDialog.kt` & `AddEditRelativeDialog.kt`.
- **Searchable List**: Instant search across phonebook.
- **Smart Degree Auto-Detection**: Automatically highlights degree chips based on phonebook names (e.g. "أمي" -> Parents, "خالي" -> Uncles/Aunts).
- **Interval Picker Buttons**: Smooth capsule row for reminder frequency:
  `[يومياً]` `[كل 3 أيام]` `[أسبوعياً]` `[كل أسبوعين]` `[شهرياً]`.

---

### 4.7 Screen 7: Activity Log Bottom Sheet (`RecordLogBottomSheet.kt`)
- **Type**: Soft modal bottom sheet (`26.dp` top radius).
- **Interaction Selection**: 4 visual icon selector cards:
  - 📞 Call (`اتصال`)
  - 💬 Message (`رسالة`)
  - 🏡 Visit (`زيارة`)
  - 🎁 Gift (`هدية`)
- **Date/Time Picker**: Defaulted to "Now" with option to adjust.
- **Notes Input**: Smooth `OutlinedTextField` for custom interaction notes.
- **Action Button**: Wide `#1E5A35` button with label `"حفظ التواصل ✨"`.

---

### 4.8 Screen 8: Family Memories & Greeting Templates Tab
- **Grid Layout**: 2-column masonry grid for family memories (Photo card, title, relative tag, date).
- **Quick Template Cards**: Category tabs (`[تهنئة الجمعة]` `[المناسبات]` `[الأعياد]`).
- **One-Tap Share**: Buttons to directly copy or share template text to WhatsApp/SMS.

---

### 4.9 Screen 9: Live Smart Home Screen Widget (4x2 Layout)
- **Widget Anatomy (`sila_widget.xml`)**:
  - Background: Warm Soft Sand background (`#FAF9F5`) with rounded corner container.
  - Header: Kinship Knot Logo + Title `"صلة الرحم"`.
  - Body: Highlights the single **Most Urgent Relative** needing contact.
  - Direct Action Buttons: `[📞 اتصل]` and `[💬 رسالة]` right on the phone's home screen.

```text
┌──────────────────────────────────────────────────────────┐
│ 🌸 صِلَةِ — الأولوية الأولى للتواصل                      │
├──────────────────────────────────────────────────────────┤
│ ( 👵 )  والدتي الحبيبة                              🔴   │
│         لم تتواصل منذ 5 أيام (المطلوب: كل 3 أيام)        │
│                                                          │
│    [ 📞 اتصل الآن ]               [ 💬 أرسل رسالة ]      │
└──────────────────────────────────────────────────────────┘
```

---

### 4.10 Screen 10: Settings & Font Picker Modal
- **Appearance Settings**: Toggle Dark/Light Mode.
- **Font Selector Grid**: Visual typography preview cards for **Thamanyah OS**, **Cairo**, **Almarai**, **Tajawal**.
- **Backup & Restore (SAF)**:
  - Button 1: 📤 `"تصدير نسخة احتياطية (JSON)"` (Launches SAF Create Document launcher).
  - Button 2: 📥 `"استعادة نسخة احتياطية"` (Launches SAF Open Document launcher).

---

### 4.11 Screen 11: Avatar Personalization Bottom Sheet (`AvatarPickerSheet.kt`)
- **Trigger**: Tapping the avatar area on a relative's card or inside `AddEditRelativeDialog`.
- **Type**: `ModalBottomSheet` with `topStart/topEnd` radius of `28.dp`.
- **Content**:
  - Header text `"اختر صورتك الشخصية"` with subtitle `"اضغط على الصورة للمعاينة، ثم اضغط حفظ"`.
  - **4-column `LazyVerticalGrid`** (max height `440.dp`) of **24 emoji avatar cells** organized into 3 categories:
    - **Family & People** (12): 👨 👩 👴 👵 🧑 👦 👧 🧔 🧕 👨‍🦳 👩‍🦳 🧒
    - **Professions & Roles** (6): 👨‍⚕️ 👩‍⚕️ 👨‍🏫 👩‍🏫 👨‍💼 👩‍💼
    - **Fun & Expressive** (6): 🤴 👸 🦸 🦸‍♀️ 🧙 🧑‍🚀
  - Each avatar cell: 62dp circle with a unique linear gradient background, emoji at 28sp, Arabic/English label at 9sp below.
  - **Selected State**: 3dp `SoftGold` border ring + semi-transparent dark overlay + ✅ checkmark icon.
  - **Save Button**: Full-width 50dp height button with `SoftGold` background (`#E9CE79`) and dark text `"حفظ الصورة"`.

```text
┌──────────────────────────────────────────────────────────┐
│  اختر صورتك الشخصية                                       │
│  اضغط على الصورة للمعاينة، ثم اضغط حفظ                    │
├──────────────────────────────────────────────────────────┤
│  ( 👨 )   ( 👩 )   ( 👴 )   ( 👵 )                        │
│   رجل    امرأة   رجل كبير  امرأة كبيرة                   │
│                                                          │
│  ( 🧑 )   ( 👦 )  [✅👨‍⚕️]   ( 👩‍⚕️ )                      │
│   شاب     فتى   [SELECTED]   طبيبة                       │
│                                                          │
│  ( 🤴 )   ( 👸 )   ( 🦸 )   ( 🧙 )                        │
│   أمير   أميرة    بطل      حكيم                          │
├──────────────────────────────────────────────────────────┤
│           [ ✅  حفظ الصورة  ]   (Gold Button)             │
└──────────────────────────────────────────────────────────┘
```

---

### 4.12 Feature: Precision Arabic Relative Time Engine (`DateUtils.kt`)
The `DateUtils.formatRelativeTimeExact()` function produces **fully grammatically correct Arabic dual/plural time strings**, which is a critical UX differentiator:

| Time Elapsed | Arabic Output | English Output |
| :--- | :--- | :--- |
| < 60 seconds | `"منذ لحظات ⚡"` | `"Just now ⚡"` |
| 1 minute | `"منذ دقيقة واحدة ⏱️"` | `"1 minute ago ⏱️"` |
| 2 minutes | `"منذ دقيقتين ⏱️"` | `"2 minutes ago ⏱️"` |
| 3–10 minutes | `"منذ X دقائق ⏱️"` | `"X minutes ago ⏱️"` |
| 2 hours | `"منذ ساعتين ⏱️"` | `"2 hours ago ⏱️"` |
| Yesterday | `"أمس (منذ 24 ساعة)"` | `"Yesterday"` |
| 2 days | `"منذ يومين"` | `"2 days ago"` |
| 2 weeks | `"منذ أسبوعين"` | `"2 weeks ago"` |
| 2 months | `"منذ شهرين"` | `"2 months ago"` |
| 2 years | `"منذ سنتين"` | `"2 years ago"` |
| Never contacted | `"لم يتم بعد 🌸"` | `"Not yet 🌸"` |

> [!NOTE]
> The dual form (مثنى) is handled explicitly for minutes, hours, days, weeks, months, and years. This is **non-trivial Arabic grammar** that differentiates Silah from generic reminder apps and must be preserved in any redesign.

---

### 4.13 Feature: Full Bilingual Support (AR ↔ EN)
- **Language Selector**: On Onboarding Page 0, the user selects Arabic or English once. This setting persists via `SharedPreferences`.
- **Layout Direction Switching**: The entire app switches between `LayoutDirection.Rtl` (Arabic) and `LayoutDirection.Ltr` (English) dynamically via `CompositionLocalProvider(LocalLayoutDirection provides layoutDirection)`.
- **All Strings Bilingual**: Every visible UI string — greetings, card labels, dialog titles, button text, notification copy, empty state messages, and backup toasts — has both Arabic and English versions driven by `val lang by viewModel.selectedLanguage.collectAsState()`.
- **User Name Personalization**: The user's name and gender (collected on Page 1 of Onboarding) are used in personalized greeting messages on the Dashboard header (e.g. `"أهلاً [Name] 🌿"`).

---

## 5. AI UI Generator (Stitch) Prompts & Instructions

> [!TIP]
> Copy and paste these exact structured prompts into Google Stitch or your AI UI Generator to create pixel-perfect UI mockups for the Silah app.

### Master Stitch System Prompt

```text
Generate a modern, high-end Mobile Native Android App UI design for an app named "Silah" (Kinship & Family Ties Tracker). 
Design Style: Soft-UI / Modern Neumorphism & Material Design 3 Hybrid. Soft ambient shadows, warm organic colors, rich rounded cards (24px corner radius), high readability Arabic typography. 
Color Palette:
- Primary: Deep Royal Olive Green (#1E5A35)
- Secondary Accent: Warm Champagne Gold (#E9CE79)
- Surface/Background: Warm Alabaster Sand (#FAF9F5) for Light Mode, Deep Charcoal (#141816) for Dark Mode.
- Status Indicators: Emerald Green (#2E7D32) for Connected, Amber Gold (#FBC02D) for Due Tomorrow, Terracotta Orange (#E65100) for Due Today, Soft Crimson (#C62828) for Overdue.
Language: Arabic (RTL layout direction), using clean modern typography like Cairo/Thamanyah OS.
```

---

### Prompt 1: Main Dashboard Screen (Light Mode)

```text
Create a mobile app screen UI mockup for "Silah" Main Dashboard in Light Mode (Warm Alabaster Background #FAF9F5).
Layout Structure (Top to Bottom):
1. Top Bar: App title "صِلَةِ" in bold Arabic typography with an elegant interwoven loop logo (The Kinship Knot). Top right icons for font picker and settings.
2. Hero Progress Card: Deep Olive Green background (#1E5A35) with rounded corners (24px). Contains a circular golden progress ring (#E9CE79) displaying "85%" inside, with text "أنجزت 6 من 7 اتصالات هذا الأسبوع!" (Completed 6 of 7 contacts this week).
3. Section Header: "يحين موعد التواصل معهم قريباً" (Due for Contact Soon).
4. Horizontal Carousel: 3 compact rounded cards showing overdue relatives. Each card has a circular profile avatar, name in Arabic, a red or orange countdown badge (e.g. "تأخرت 3 أيام"), and a quick call icon button.
5. Category Filter Chips: Row of pill-shaped chips: "[الكل]" (All - Active in Deep Olive), "(الوالدان)" (Parents), "(الأشقاء)" (Siblings), "(الأعمام والأخوال)" (Uncles & Aunts).
6. Main Vertical List: 2 full relative cards. Card 1 shows "أمي" (Mother) with a green status badge "بخير" (Connected) and last call "منذ ساعتين" (2 hours ago). Card 2 shows "خالي أحمد" with an orange badge "حان الموعد اليوم" (Due Today) and two action buttons: "اتصل الآن" (Call Now) and "تسجيل تواصل" (Record Log).
```

---

### Prompt 2: Relative Profile & Timeline Screen (Dark Mode)

```text
Create a mobile app screen UI mockup for "Silah" Relative Detail Page in Dark Mode (Deep Charcoal Background #141816, Soft Surface Dark #1B221E).
Layout Structure:
1. Top Navigation Bar: Back arrow button, screen title "الملف الشخصي", and top edit/delete icons.
2. Profile Header: Large circular avatar with a warm gold ring (#E9CE79). Name "والدتي الحبيبة" (My Beloved Mother) in bold 20sp text, relationship tag "الوالدان" (Parents), and subtitle "تذكير كل 3 أيام".
3. Action Bar: 3 large soft rounded action buttons in Sage Green (#94DAB2): "اتصال هاتفي" (Phone Call), "إرسال رسالة" (Send SMS/WhatsApp), "إضافة ذكرى" (Add Memory).
4. Interactive Timeline Section: Title "سجل التواصل والذكريات". A vertical dotted line in muted olive connecting 3 interaction nodes:
   - Node 1 (Phone Call Icon): "مكالمة هاتفية تلقائية" - "منذ ساعتين | مدة المكالمة 12 دقيقة".
   - Node 2 (Home Icon): "زيارة عائلية" - "منذ 4 أيام" with note card "زرنا أمي وقدمنا لها الهدية".
   - Node 3 (Gift Icon): "هدية بمناسبة العيد" - "منذ أسبوعين".
```

---

### Prompt 3: Add / Import Relative Dialog (Light Mode Modal)

```text
Create a mobile modal dialog UI mockup for "Import & Add Relative" in Light Mode.
Design Details:
- Soft floating dialog card with 26px rounded corners, subtle golden border outline, background pure white (#FFFFFF).
- Top Header: Title "إضافة قريب جديد ✨" with search input box "ابحث في جهات اتصال الهاتف...".
- Auto-Detected Degree Chips: Row of relationship tier tags: "[الوالدان]" (Parents), "[الأشقاء]" (Siblings), "[الأعمام والأخوال]" (Uncles & Aunts), "[أقارب آخرون]" (Other Relatives).
- Reminder Interval Selector: 5 capsule buttons for frequency: "[يومياً]", "[كل 3 أيام]" (Selected in Deep Olive Green #1E5A35), "[أسبوعياً]", "[كل أسبوعين]", "[شهرياً]".
- Bottom Action Buttons: Secondary button "إلغاء" (Cancel) and primary full-width button "حفظ وتفعيل التذكير ✨" in Deep Olive Green.
```

---

### Prompt 4: Android Home Screen Live Widget (4x2 Compact Widget)

```text
Create a mobile UI mockup for a 4x2 Android Home Screen Widget for the app "Silah".
Design Details:
- Warm Sand container background (#FAF9F5) with soft 16px corner radius and subtle shadow.
- Header: App logo (Kinship Knot) + text "صلة الرحم — الأولوية الأولى".
- Content Body: Relative profile card showing avatar of a grandmother, name "أمينة (الوالدة)", status pill in Soft Crimson Red "تأخرت 4 أيام" (Overdue 4 days).
- Bottom Action Row: Two side-by-side capsule buttons:
  - Left Button: Deep Olive background (#1E5A35), white text "📞 اتصل الآن".
  - Right Button: Soft Gold background (#E9CE79), dark text "💬 أرسل رسالة".
```

---

### Prompt 5: Avatar Picker Bottom Sheet

```text
Create a mobile UI mockup for a bottom sheet modal in the "Silah" app for picking a relative's profile avatar.
Design Details:
- ModalBottomSheet with 28px top-left and top-right corner radius. Background: Surface White (#FFFFFF) in Light Mode.
- Header: Title "اختر صورتك الشخصية" (Choose Your Avatar) in Bold 18sp, subtitle "اضغط على الصورة للمعاينة، ثم اضغط حفظ" in 12sp Muted Olive.
- Main Grid: 4-column grid of circular avatar cells. Each cell is a 62dp circle with a unique dark gradient background (examples: Deep Teal #0E4D7A→#1A78C2, Warm Amber #5C3B1A→#9D6A2A, Royal Purple #4A1A5C→#7A2A9D). Inside each circle is a large emoji (28sp).
- Below each circle: a small 9sp Arabic label (رجل, امرأة, طبيب, أمير, etc.).
- Selected State: The chosen avatar has a 3dp golden ring (#E9CE79) around its circle, a subtle dark overlay, and a white checkmark icon in the center.
- Bottom Save Button: Full-width 50dp height pill button with Champagne Gold background (#E9CE79) and dark charcoal text "✅ حفظ الصورة" in Bold 15sp.
```

---

## 6. Functional System Requirements

### 6.1 Call Log Synchronization Engine
- **FR-1.1**: The application shall automatically monitor incoming, outgoing, and missed call events using `CallLogManager`.
- **FR-1.2**: When a call matches a saved relative's phone number, the app shall create a `CommunicationLog` entry and update `Relative.lastContactDate` in Room DB.
- **FR-1.3**: Number matching must handle international dial codes, leading zeros, and spaces gracefully.

### 6.2 Degree-Aware Smart Notifications
- **FR-2.1**: Background execution via Android `WorkManager` scheduled periodically (every 6–12 hours).
- **FR-2.2**: Notification text copy must dynamically adjust based on relationship tier:
  - *Parents*: `"بقالك فترة مش بتطمن على والدتك 💚"`
  - *Uncles/Aunts*: `"بقالك فترة مش بتطمن على خالك ✨"`
  - *Siblings*: `"بقالك فترة مش بتطمن على أختك 🌸"`

### 6.3 Local Backup & Restore (SAF Integration)
- **FR-3.1**: Export full DB state to `silah_backup.json` using Storage Access Framework (`CreateDocument("application/json")`).
- **FR-3.2**: Restore DB state using SAF `OpenDocument()` with JSON validation and transactional database insertion.

---

## 7. System Architecture & Data Schema

### 7.1 Architecture Diagram

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│   (MainDashboard, RelativesTabScreen, Dialogs, Widgets, Theme Engine)   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ StateFlow / SharedFlow
┌───────────────────────────────────▼────────────────────────────────────┐
│                          RelativeViewModel                             │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Coroutine Flow
┌───────────────────────────────────▼────────────────────────────────────┐
│                         RelativeRepository                             │
└────────┬──────────────────────────┼───────────────────────────┬────────┘
         │                          │                           │
┌────────▼────────┐        ┌────────▼────────┐         ┌────────▼────────┐
│  Room Database  │        │ CallLogManager  │         │  BackupManager  │
│ (SQLite Tables) │        │ (READ_CALL_LOG) │         │   (SAF JSON)    │
└─────────────────┘        └─────────────────┘         └─────────────────┘
```

### 7.2 Room Database Schema

#### Entity 1: `relatives`
```kotlin
@Entity(tableName = "relatives")
data class Relative(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,                  // Full Name
    val phone: String,                 // Phone Number
    val relationshipDegree: String,    // Tier: "الوالدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون"
    val lastContactDate: Long = 0,     // Timestamp in ms
    val contactIntervalDays: Int = 14, // Interval target in days
    val notes: String = ""             // Custom notes
)
```

#### Entity 2: `communication_logs`
```kotlin
@Entity(tableName = "communication_logs")
data class CommunicationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val relativeId: Int,
    val type: String,                  // "اتصال", "رسالة", "زيارة", "هدية"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
```

#### Entity 3: `quick_templates`
```kotlin
@Entity(tableName = "quick_templates")
data class QuickTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,                 // e.g. "تهنئة يوم الجمعة"
    val content: String,
    val category: String               // e.g. "مناسبات", "جمعة"
)
```

#### Entity 4: `family_memories`
```kotlin
@Entity(tableName = "family_memories")
data class FamilyMemory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val relativeId: Int,
    val relativeName: String,
    val title: String,
    val description: String,
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

## 8. Non-Functional & Privacy Requirements

### 8.1 Performance Standards
- **App Launch Time**: Cold launch to main screen in `< 800ms`.
- **Search Latency**: Real-time filtering response in `< 50ms`.
- **Memory Footprint**: Active RAM usage capped at `< 70 MB`.
- **Battery Impact**: Background WorkManager tasks constrained to `< 1%` daily battery consumption.

### 8.2 100% Local-First Privacy Guarantee
- **Zero Cloud Tracking**: No external servers, no Firebase analytics, no third-party telemetry.
- **On-Device Sandbox**: All contacts, call logs, notes, and backup files remain strictly inside the user's Android sandbox.

---

> **Specification Approved & Ready for UI Generation in Google Stitch & Production Android Implementation.**
