package io.github.lordraydenmk.android_fx.example02

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import io.github.lordraydenmk.android_fx.fakes.errorService
import io.github.lordraydenmk.android_fx.fakes.repositoryDto
import io.github.lordraydenmk.android_fx.fakes.successService
import io.github.lordraydenmk.android_fx.view.ViewState
import org.junit.Rule
import org.junit.Test
import java.util.*


internal class SequentialApiCallsViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `viewState - successful service call - Loading then Success`() {
        val id = UUID.randomUUID()
        val viewModel = SequentialApiCallsViewModel(successService(id))

        viewModel.viewState
            .test()
            .awaitNextValue()
            .assertValueHistory(
                ViewState.Loading,
                ViewState.Content(repositoryDto(id, "Model Details"))
            )
    }

    @Test
    fun `viewState - failed service call - Loading then Error`() {
        val viewModel = SequentialApiCallsViewModel(errorService("Bang!"))

        viewModel.viewState
            .test()
            .awaitNextValue()
            .assertValueHistory(
                ViewState.Loading,
                ViewState.Error("Bang!\n\nTap here to retry!")
            )
    }
}
