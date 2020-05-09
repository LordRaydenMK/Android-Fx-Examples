package io.github.lordraydenmk.android_fx.example05

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.IO.Companion.effect
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.fx.typeclasses.Duration
import arrow.fx.typeclasses.seconds
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.view.ViewState
import io.github.lordraydenmk.android_fx.view.errorMessage

class RequestWithTimeoutViewModel(
    private val service: GithubService = GithubService.create(errorProbability = 15)
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<RepositoryDto>>()
    val viewState: LiveData<ViewState<RepositoryDto>>
        get() = _viewState

    init {
        execute()
    }

    fun execute(timeout: Duration = 1.seconds) {
        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            val model = effect { service.getRepository() }
                .waitFor(timeout)
                .bind()
            ViewState.Content(model)
        }
            .handleError { ViewState.Error(it.errorMessage()) }
            .flatMap { effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) { }
    }
}