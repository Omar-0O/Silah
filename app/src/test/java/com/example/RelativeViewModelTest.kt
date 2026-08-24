package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.Relative
import com.example.viewmodel.RelativeStatus
import com.example.viewmodel.RelativeViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RelativeViewModelTest {

    private lateinit var viewModel: RelativeViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = RelativeViewModel(application)
    }

    @Test
    fun testSuggestRelationshipDegree() {
        assertEquals("والدان", viewModel.suggestRelationshipDegree("أمي الحبيبة"))
        assertEquals("والدان", viewModel.suggestRelationshipDegree("ماما غادة"))
        assertEquals("والدان", viewModel.suggestRelationshipDegree("والدي العزيز"))

        assertEquals("أشقاء", viewModel.suggestRelationshipDegree("أخي أحمد"))
        assertEquals("أشقاء", viewModel.suggestRelationshipDegree("أختي مريم"))

        assertEquals("أعمام/أخوال", viewModel.suggestRelationshipDegree("عمي محمد"))
        assertEquals("أعمام/أخوال", viewModel.suggestRelationshipDegree("خالتي منى"))

        assertEquals("أقارب آخرون", viewModel.suggestRelationshipDegree("سعيد"))
    }

    @Test
    fun testGetRelativeStatus_NeverCalled() {
        val relative = Relative(
            id = 1,
            name = "حسن",
            phone = "01000000000",
            relationshipDegree = "أقارب آخرون",
            lastContactDate = 0L,
            contactIntervalDays = 7
        )
        val status = viewModel.getRelativeStatus(relative)
        assertEquals(RelativeStatus.NEEDS_CONTACT_URGENT, status)
    }

    @Test
    fun testGetRelativeStatus_ConnectedRecently() {
        val relative = Relative(
            id = 2,
            name = "علي",
            phone = "01100000000",
            relationshipDegree = "أشقاء",
            lastContactDate = System.currentTimeMillis(),
            contactIntervalDays = 7
        )
        val status = viewModel.getRelativeStatus(relative)
        assertEquals(RelativeStatus.CONNECTED, status)
    }

    @Test
    fun testGetRelativeStatus_OverdueCritical() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 86_400_000L)
        val relative = Relative(
            id = 3,
            name = "كمال",
            phone = "01200000000",
            relationshipDegree = "أعمام/أخوال",
            lastContactDate = thirtyDaysAgo,
            contactIntervalDays = 7
        )
        val status = viewModel.getRelativeStatus(relative)
        assertEquals(RelativeStatus.OVERDUE_CRITICAL, status)
    }

    @Test
    fun testGenerateLocalSmartMessage() {
        val msgFriday = viewModel.generateLocalSmartMessage("أحمد", "أشقاء", "يوم الجمعة")
        assertTrue(msgFriday.contains("جمعة مباركة وطيبة"))

        val msgEid = viewModel.generateLocalSmartMessage("والدتي", "والدان", "عيد الفطر/الأضحى")
        assertTrue(msgEid.contains("حلول العيد المبارك"))

        val msgGeneral = viewModel.generateLocalSmartMessage("خالد", "أقارب آخرون", "سؤال عام عن الحال")
        assertTrue(msgGeneral.contains("الاطمئنان على أحوالكم وصحتكم"))
    }

    @Test
    fun testSelectLanguageAndDarkMode() {
        viewModel.selectLanguage("en")
        assertEquals("en", viewModel.selectedLanguage.value)

        viewModel.toggleDarkMode(true)
        assertTrue(viewModel.isDarkMode.value)

        viewModel.saveUserProfile("عمر", "male")
        assertEquals("عمر", viewModel.userName.value)
        assertEquals("male", viewModel.userGender.value)
    }
}
