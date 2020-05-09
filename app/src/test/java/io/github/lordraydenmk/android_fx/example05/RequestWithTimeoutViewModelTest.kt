package io.github.lordraydenmk.android_fx.example05

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import io.github.lordraydenmk.android_fx.fakes.successService
import io.github.lordraydenmk.android_fx.view.ViewState
import org.junit.Rule
import org.junit.Test
import java.util.*

class RequestWithTimeoutViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `viewState - request is delayed 1s or longer - Loading then timeout Error`() {
        val viewModel = RequestWithTimeoutViewModel(successService(UUID.randomUUID(), 1005))

        viewModel.viewState
            .test()
            .awaitNextValue()
            .assertValueHistory(
                ViewState.Loading,
                ViewState.Error("Duration(amount=1, timeUnit=SECONDS)") // timeout was set to 1s in the VM
            )
    }
}