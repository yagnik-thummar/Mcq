package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DifficultyLevel
import com.example.data.SampleDataGenerator
import com.example.data.SubjectEnum
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
        assertEquals("MCQ Generator", appName)
    }

    @Test
    fun `sample questions contain math and chemistry topics with latex`() {
        val samples = SampleDataGenerator.getSampleQuestions()
        assertTrue(samples.isNotEmpty())
        
        val mathQ = samples.firstOrNull { it.subject == SubjectEnum.MATHEMATICS.name }
        assertTrue(mathQ != null)
        assertTrue(mathQ!!.questionText.contains("int"))

        val chemQ = samples.firstOrNull { it.subject == SubjectEnum.CHEMISTRY.name }
        assertTrue(chemQ != null)
        assertTrue(chemQ!!.chapter.isNotEmpty())
    }
}
