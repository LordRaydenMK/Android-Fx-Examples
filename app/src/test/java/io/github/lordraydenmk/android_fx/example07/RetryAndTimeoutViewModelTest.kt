package io.github.lordraydenmk.android_fx.example07

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.fakes.FakeGithubService
import io.github.lordraydenmk.android_fx.fakes.repositoryDto
import io.github.lordraydenmk.android_fx.view.ViewState
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.lang.RuntimeException
import java.util.*
import kotlin.system.measureTimeMillis

class RetryAndTimeoutViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `viewState - long response then error response then success - Content`() {
        val id = UUID.randomUUID()
        val model = repositoryDto(id, "arrow-kt/arrow-fx")
        val responses = listOf<suspend () -> RepositoryDto>(
            { delay(1100); model }, // slow response > than timeout
            { delay(300); throw RuntimeException("Bang!") }, // fast but error
            { delay(500); model } // fast, success
        )

        val fakeService = FakeGithubService.createWithResponses(responses)
        val viewModel = RetryAndTimeoutViewModel(fakeService)

        val timeMillis = measureTimeMillis {
            viewModel.viewState
                .test()
                .awaitNextValue()
                .assertValue { it is ViewState.Content }
        }

        println("Total time: $timeMillis")
        // VM has exponential retry base 1 factor 2
        // that results in delays of [1s, 4s] -> 5s
        // total call time is 1.9s
        // total time should be greater than 6.9s
        assertTrue(timeMillis > 6900)
        assertEquals(3, fakeService.count)
    }
}