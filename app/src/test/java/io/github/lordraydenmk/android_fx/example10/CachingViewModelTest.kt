package io.github.lordraydenmk.android_fx.example10

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import io.github.lordraydenmk.android_fx.fakes.errorService
import io.github.lordraydenmk.android_fx.fakes.testCache
import io.github.lordraydenmk.android_fx.fakes.repositoryDto
import io.github.lordraydenmk.android_fx.fakes.successService
import io.github.lordraydenmk.android_fx.view.ViewState
import org.junit.Rule
import org.junit.Test
import java.util.*

class CachingViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `viewState - empty cache - returns data from service`() {
        val id = UUID.randomUUID()
        val viewModel = CachingViewModel(testCache(value = null), successService(id))

        viewModel.viewState
            .test()
            .awaitNextValue()
            .assertValueHistory(
                ViewState.Loading,
                ViewState.Content(repositoryDto(id, "Model"))
            )
    }

    @Test
    fun `viewState - cached value - returns from cache`() {
        val id = UUID.randomUUID()
        val viewModel = CachingViewModel(
            testCache(value = repositoryDto(id, "Cached Repo")),
            errorService("Bang!")
        )

        viewModel.viewState
            .test()
            .awaitNextValue()
            .assertValueHistory(
                ViewState.Loading,
                ViewState.Content(repositoryDto(id, "Cached Repo"))
            )
    }
}