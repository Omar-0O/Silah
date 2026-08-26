package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    org.junit.Assert.assertNotNull(appName)
    org.junit.Assert.assertTrue(appName.isNotEmpty())
  }

  @Test
  fun `test viewModel and typography initialization`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.viewmodel.RelativeViewModel(context)
    org.junit.Assert.assertNotNull(vm)
    val typography = com.example.ui.theme.Typography
    org.junit.Assert.assertNotNull(typography)
  }
}
