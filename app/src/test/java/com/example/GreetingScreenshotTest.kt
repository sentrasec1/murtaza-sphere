package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.EduQuestTheme
import com.example.ui.screens.DashboardScreen
import com.example.data.UserProgress
import com.example.data.SubjectEntity
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      EduQuestTheme {
        DashboardScreen(
          progress = UserProgress(spherePoints = 350, streakCount = 3),
          subjects = listOf(
            SubjectEntity("math", "Mathematics", "📐", "#5B52F0"),
            SubjectEntity("coding", "Coding", "💻", "#0099CC")
          ),
          onSubjectClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
