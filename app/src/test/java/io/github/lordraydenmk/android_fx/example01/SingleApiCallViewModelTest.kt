package io.github.lordraydenmk.android_fx.example01

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import io.github.lordraydenmk.android_fx.fakes.errorService
import io.github.lordraydenmk.android_fx.fakes.repositoryDto
import io.github.lordraydenmk.android_fx.fakes.successService
import io.github.lordraydenmk.android_fx.view.ViewState
import org.junit.Rule
import org.junit.Test
import java.util.*

class SingleApiCallViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `viewState - successful service call - Loading then Content`() {
        val id = UUID.randomUUID()
        val viewModel = SingleApiCallViewModel(successService(id))

        viewModel.viewState
            .test()
            .awaitNextValue()
            .assertValueHistory(
                ViewState.Loading,
                ViewState.Content(repositoryDto(id, "Model"))
            )
    }

    @Test
    fun `viewState - failed service call - Loading then Error`() {
        val viewModel = SingleApiCallViewModel(errorService("Bang!"))

        viewModel.viewState
            .test()
            .awaitNextValue()
            .assertValueHistory(
                ViewState.Loading,
                ViewState.Error("Bang!")
            )
    }
}