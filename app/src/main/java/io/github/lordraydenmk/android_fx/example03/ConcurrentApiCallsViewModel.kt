package io.github.lordraydenmk.android_fx.example03

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Tuple3
import arrow.fx.IO
import arrow.fx.IO.Companion.effect
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.view.ViewState
import io.github.lordraydenmk.android_fx.view.errorMessage
import kotlinx.coroutines.Dispatchers

class ConcurrentApiCallsViewModel(
    private val service: GithubService = GithubService.create(errorProbability = 10)
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<RepositoryDto>>()
    val viewState: LiveData<ViewState<RepositoryDto>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() = IO.fx {
        effect { _viewState.postValue(ViewState.Loading) }.bind()
        // performs the requests concurrently
        // if all of them succeed, we get a Tuple3 (there are 3 requests)
        // if any of them fail, the whole computation (fx-block) short-circuits
        // in case of failure, the rest of the requests are canceled (if possible)
        val results: Tuple3<RepositoryDto, RepositoryDto, RepositoryDto> = IO.parTupledN(
            Dispatchers.IO,
            effect { service.getRepository() },
            effect { service.getRepository() },
            effect { service.getRepository() }
        ).bind()
        ViewState.Content(results.b)    // I'm picking the 2nd result because why not
    }
        .handleError { ViewState.Error(it.errorMessage()) }
        .flatMap { effect { _viewState.postValue(it) } }
        .unsafeRunScoped(viewModelScope) { }
}
