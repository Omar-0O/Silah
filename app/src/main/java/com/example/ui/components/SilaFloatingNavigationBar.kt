package com.example.ui.components

import android.view.HapticFeedbackConstants
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SecondaryGreyGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceLight

// ── Tab Enum Definition ───────────────────────────────────────────────────────
enum class SilaTab(
    val labelAr: String,
    val labelEn: String,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector
) {
    DASHBOARD(
        labelAr = "الرئيسية",
        labelEn = "Dashboard",
        iconOutlined = Icons.Outlined.Home,
        iconFilled = Icons.Filled.Home
    ),
    RELATIVES(
        labelAr = "الأرحام",
        labelEn = "Relatives",
        iconOutlined = Icons.Outlined.People,
        iconFilled = Icons.Filled.People
    ),
    PROFILE(
        labelAr = "حسابي",
        labelEn = "Profile",
        iconOutlined = Icons.Outlined.Person,
        iconFilled = Icons.Filled.Person
    );

    fun label(lang: String) = if (lang == "en") labelEn else labelAr
}

/**
 * Scroll-aware state for the floating navigation bar.
 * Tracks scroll direction and exposes a label visibility fraction (1 = fully visible, 0 = hidden).
 * Adapted from NuvioMobile's responsive navbar architecture.
 */
@Stable
class SilaNavBarScrollState {
    /** 1f = labels fully visible (expanded), 0f = labels hidden (collapsed, icons only) */
    var labelVisibility by mutableFloatStateOf(1f)
        private set

    private var accumulatedDelta = 0f

    /** Call to expand (show labels) – e.g. when user scrolls back to top */
    fun expand() {
        labelVisibility = 1f
        accumulatedDelta = 0f
    }

    /** Call to collapse (hide labels) */
    fun collapse() {
        labelVisibility = 0f
        accumulatedDelta = 0f
    }

    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val deltaY = available.y
            if (deltaY == 0f) return Offset.Zero

            accumulatedDelta += deltaY

            if (accumulatedDelta < -SCROLL_THRESHOLD && labelVisibility != 0f) {
                // Scrolling down past threshold → snap collapse
                labelVisibility = 0f
                accumulatedDelta = 0f
            } else if (accumulatedDelta > SCROLL_THRESHOLD && labelVisibility != 1f) {
                // Scrolling up past threshold → snap expand
                labelVisibility = 1f
                accumulatedDelta = 0f
            }

            // Reset accumulator if direction changed
            if (deltaY < 0f && accumulatedDelta > 0f) accumulatedDelta = deltaY
            if (deltaY > 0f && accumulatedDelta < 0f) accumulatedDelta = deltaY

            return Offset.Zero // Don't consume any scroll
        }
    }

    companion object {
        private const val SCROLL_THRESHOLD = 50f
    }
}

@Composable
fun rememberSilaNavBarScrollState(): SilaNavBarScrollState {
    return remember { SilaNavBarScrollState() }
}

/**
 * World-class Floating Pill Navigation Bar.
 *
 * Features:
 * - Floating capsule geometry with soft elevation and tinted borders.
 * - Dynamic scroll responsiveness: collapses labels to compact capsule when scrolling down.
 * - Fluid tab selection with smooth pill background transition and micro-haptics.
 * - Edge-to-edge system insets aware.
 */
/**
 * SilaFloatingNavigationBar — شريط التنقل السفلي العائم (Icons-Only)
 *
 * المميزات:
 * - أيقونات نقية ومباشرة بدون أسماء لحداثة وأناقة الواجهة.
 * - كبسولة عائمة مدمجة (Compact Floating Pill) متمركزة في أسفل الشاشة.
 * - دعم صورة البروفايل الحقيقية للمستخدم في مكان الإعدادات مع إطار تفاعلي عند الاختيار.
 * - شارة تنبيهية لعدد الأرحام المستحقين للتواصل.
 * - تأثيرات تفاعلية ناعمة (Micro-interactions & Haptics).
 * - توافق كامل مع Edge-to-Edge وهوامش النظام.
 */


/**
 * World-class Floating Navigation Bar with Real-time Frosted Glass (Blur Effect).
 */
@Composable
fun SilaFloatingNavigationBar(
    selectedTab: SilaTab,
    onTabSelected: (SilaTab) -> Unit,
    modifier: Modifier = Modifier,
    lang: String = "ar",
    dueCount: Int = 0,
    userPhotoPath: String? = null,
    userName: String = "",
    userPhotoTimestamp: Long = 0L,
    userAvatarId: String = "avatar_01",
    scrollState: SilaNavBarScrollState? = null,
    hazeState: HazeState? = null,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val view = LocalView.current

    val labelFraction by animateFloatAsState(
        targetValue = scrollState?.labelVisibility ?: 1f,
        animationSpec = tween(
            durationMillis = 260,
            easing = FastOutSlowInEasing,
        ),
        label = "nav_label_fraction",
    )

    // Dynamic horizontal padding: expands across the screen width (20.dp) and contracts into compact pill on scroll down (64.dp)
    val expandedHorizontalPadding = 20.dp
    val collapsedHorizontalPadding = 64.dp
    val horizontalPadding = expandedHorizontalPadding + (collapsedHorizontalPadding - expandedHorizontalPadding) * (1f - labelFraction)

    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = navBarBottomInset + 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val containerBg = if (isDark) {
            SurfaceDark.copy(alpha = if (hazeState != null) 0.65f else 0.95f)
        } else {
            SurfaceLight.copy(alpha = if (hazeState != null) 0.70f else 0.97f)
        }
        val borderColor = if (isDark) {
            Color.White.copy(alpha = if (hazeState != null) 0.18f else 0.10f)
        } else {
            PrimaryGreen.copy(alpha = if (hazeState != null) 0.20f else 0.12f)
        }
        val pillShape = CircleShape

        Surface(
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = if (isDark) 8.dp else 14.dp,
                    shape = pillShape,
                    ambientColor = if (isDark) Color.Black.copy(alpha = 0.5f) else PrimaryGreen.copy(alpha = 0.14f),
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.35f) else PrimaryGreen.copy(alpha = 0.08f)
                )
                .then(
                    if (hazeState != null) {
                        Modifier.hazeEffect(state = hazeState) {
                            blurRadius = 24.dp
                        }
                    } else {
                        Modifier
                    }
                ),
            shape = pillShape,
            color = containerBg,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SilaTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val badge = if (tab == SilaTab.RELATIVES && dueCount > 0) dueCount else null

                    SilaNavBarItem(
                        tab = tab,
                        isSelected = isSelected,
                        badge = badge,
                        isDark = isDark,
                        userPhotoPath = userPhotoPath,
                        userName = userName,
                        userPhotoTimestamp = userPhotoTimestamp,
                        userAvatarId = userAvatarId,
                        lang = lang,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onTabSelected(tab)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SilaNavBarItem(
    tab: SilaTab,
    isSelected: Boolean,
    badge: Int?,
    isDark: Boolean,
    userPhotoPath: String? = null,
    userName: String = "",
    userPhotoTimestamp: Long = 0L,
    userAvatarId: String = "avatar_01",
    lang: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeColor = if (isDark) PrimaryGreenLight else PrimaryGreen
    val inactiveColor = if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else SecondaryGreyGreen.copy(alpha = 0.65f)

    val itemContentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(220),
        label = "nav_item_content_color"
    )

    val activePillBg = if (isDark) PrimaryGreenLight.copy(alpha = 0.18f) else PrimaryGreen.copy(alpha = 0.10f)
    val itemBgColor by animateColorAsState(
        targetValue = if (isSelected) activePillBg else Color.Transparent,
        animationSpec = tween(220),
        label = "nav_item_bg_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = tween(200),
        label = "nav_item_scale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(itemBgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (tab == SilaTab.PROFILE) {
            // User profile avatar thumbnail with active border indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(if (isSelected) 30.dp else 28.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            ) {
                SilaUserAvatar(
                    photoPath = userPhotoPath,
                    userName = userName,
                    timestamp = userPhotoTimestamp,
                    size = if (isSelected) 28.dp else 26.dp,
                    showBorder = false
                )
            }
        } else {
            // Badged Icon
            BadgedBox(
                badge = {
                    if (badge != null && badge > 0) {
                        Badge(
                            containerColor = AlertRed,
                            contentColor = Color.White,
                            modifier = Modifier.offset(x = (-3).dp, y = 2.dp)
                        ) {
                            Text(
                                text = if (badge > 99) "99+" else badge.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (isSelected) tab.iconFilled else tab.iconOutlined,
                    contentDescription = tab.label(lang),
                    tint = itemContentColor,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
        }
    }
}

/**
 * Backward compatibility alias for SilaBottomNavigationBar.
 */
@Composable
fun SilaBottomNavigationBar(
    selectedTab: SilaTab,
    onTabSelected: (SilaTab) -> Unit,
    lang: String,
    dueCount: Int = 0,
    userPhotoPath: String? = null,
    userName: String = "",
    userPhotoTimestamp: Long = 0L,
    userAvatarId: String = "avatar_01",
    scrollState: SilaNavBarScrollState? = null,
    modifier: Modifier = Modifier
) {
    SilaFloatingNavigationBar(
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
        lang = lang,
        dueCount = dueCount,
        userPhotoPath = userPhotoPath,
        userName = userName,
        userPhotoTimestamp = userPhotoTimestamp,
        userAvatarId = userAvatarId,
        scrollState = scrollState
    )
}

/**
 * Helper to blend content with a gradient brush (adapted from NuvioMobile).
 */
fun Modifier.gradientMask(brush: Brush): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            onDrawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.SrcIn)
            }
        }
