package io.github.lordraydenmk.android_fx.example03

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import io.github.lordraydenmk.android_fx.fakes.successService
import io.github.lordraydenmk.android_fx.view.ViewState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.*
import kotlin.system.measureTimeMillis

class ConcurrentApiCallsViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `viewState - service returning success - Content`() {
        val id = UUID.randomUUID()

        val timeMillis = measureTimeMillis {
            val viewModel = ConcurrentApiCallsViewModel(successService(id, 1000))

            viewModel.viewState
                .test()
                .awaitNextValue()
                .assertValue { it is ViewState.Content }
        }

        assertTrue(timeMillis < 1500) // 3 concurrent request, 1000 ms each, result should be a bit above 1s
    }
}