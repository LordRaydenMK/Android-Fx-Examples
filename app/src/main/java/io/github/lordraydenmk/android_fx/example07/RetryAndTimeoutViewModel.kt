package io.github.lordraydenmk.android_fx.example07

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.Schedule
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.monad.monad
import arrow.fx.handleError
import arrow.fx.retry
import arrow.fx.typeclasses.seconds
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.view.ViewState
import io.github.lordraydenmk.android_fx.view.errorMessage
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class RetryAndTimeoutViewModel(
    private val service: GithubService = GithubService.create(GithubService.slowResponseThenErrorThenFast)
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<RepositoryDto>>()
    val viewState: LiveData<ViewState<RepositoryDto>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() {
        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            val model = effect { service.getRepository() }
                .waitFor(1.seconds)
                .onError { IO { Timber.e(it, "Error calling `getRepository`") } }
                .retry(IO.concurrent(), exponential())
                .bind()
            continueOn(Dispatchers.Default)
            ViewState.Content(model)
        }
            .handleError { ViewState.Error(it.errorMessage()) }
            .flatMap { IO.effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) { }
    }

    // exponential backoff with initial delay 1 sec and factor of 2
    // max of 5 retries
    private fun <A> exponential() = Schedule.withMonad(IO.monad()) {
        exponential<A>(base = 1.seconds, factor = 2.0) and recurs(5)
    }
}