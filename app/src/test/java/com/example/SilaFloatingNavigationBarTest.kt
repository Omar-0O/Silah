package com.example

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import com.example.ui.components.SilaNavBarScrollState
import com.example.ui.components.SilaTab
import org.junit.Assert.assertEquals
import org.junit.Test

class SilaFloatingNavigationBarTest {

    @Test
    fun `initial scroll state has labels fully visible`() {
        val state = SilaNavBarScrollState()
        assertEquals(1f, state.labelVisibility, 0.001f)
    }

    @Test
    fun `collapse and expand methods change label visibility`() {
        val state = SilaNavBarScrollState()
        state.collapse()
        assertEquals(0f, state.labelVisibility, 0.001f)

        state.expand()
        assertEquals(1f, state.labelVisibility, 0.001f)
    }

    @Test
    fun `scrolling down past threshold collapses labels`() {
        val state = SilaNavBarScrollState()
        val connection = state.nestedScrollConnection

        // Scroll down delta (negative y) below threshold
        connection.onPreScroll(Offset(0f, -20f), NestedScrollSource.UserInput)
        assertEquals(1f, state.labelVisibility, 0.001f)

        // Scroll down further past threshold (total < -50f)
        connection.onPreScroll(Offset(0f, -40f), NestedScrollSource.UserInput)
        assertEquals(0f, state.labelVisibility, 0.001f)
    }

    @Test
    fun `scrolling up past threshold expands labels`() {
        val state = SilaNavBarScrollState()
        state.collapse()
        val connection = state.nestedScrollConnection

        // Scroll up delta (positive y) below threshold
        connection.onPreScroll(Offset(0f, 20f), NestedScrollSource.UserInput)
        assertEquals(0f, state.labelVisibility, 0.001f)

        // Scroll up further past threshold (total > 50f)
        connection.onPreScroll(Offset(0f, 40f), NestedScrollSource.UserInput)
        assertEquals(1f, state.labelVisibility, 0.001f)
    }

    @Test
    fun `tab enum labels and icons are defined properly`() {
        assertEquals("الرئيسية", SilaTab.DASHBOARD.label("ar"))
        assertEquals("Dashboard", SilaTab.DASHBOARD.label("en"))

        assertEquals("الأرحام", SilaTab.RELATIVES.label("ar"))
        assertEquals("Relatives", SilaTab.RELATIVES.label("en"))

        assertEquals("حسابي", SilaTab.PROFILE.label("ar"))
        assertEquals("Profile", SilaTab.PROFILE.label("en"))
    }
}
