package io.github.lordraydenmk.android_fx.example04

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import io.github.lordraydenmk.android_fx.fakes.successService
import io.github.lordraydenmk.android_fx.view.ViewState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.*
import kotlin.system.measureTimeMillis

class MultipleNetworkRequestsViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `viewState - service succeeds - ends with Content`() {
        val id = UUID.randomUUID()
        val viewModel = MultipleNetworkRequestsViewModel(successService(id, 500))

        val timeMillis = measureTimeMillis {
            viewModel.viewState
                .test()
                .awaitNextValue()
                .assertValue { it is ViewState.Content }
        }

        assertTrue(timeMillis < 1000) // VM does 10 calls concurrently, 500 ms each
    }
}