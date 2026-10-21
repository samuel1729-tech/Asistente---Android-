package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.JarvisToolRegistry
import com.example.domain.ToolStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JARVIS", appName)
  }

  @Test
  fun `verify jarvis tool registry has operational tools`() {
    val tools = JarvisToolRegistry.tools
    assertTrue(tools.isNotEmpty())
    val operational = tools.filter { it.status == ToolStatus.OPERATIONAL }
    assertTrue(operational.isNotEmpty())
  }
}
