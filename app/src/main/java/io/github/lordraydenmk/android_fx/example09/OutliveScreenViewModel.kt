package io.github.lordraydenmk.android_fx.example09

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.IO.Companion.effect
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.bracket.onCancel
import arrow.fx.handleError
import arrow.fx.typeclasses.Duration
import arrow.fx.typeclasses.seconds
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.view.ViewState
import io.github.lordraydenmk.android_fx.view.errorMessage
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class OutliveScreenViewModel(
    private val backgroundTaskDuration: Duration = 10.seconds
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<RepositoryDto>>()
    val viewState: LiveData<ViewState<RepositoryDto>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() {
        // just a useless task that sleeps for demo purposes
        val backgroundTask = IO.fx {
            effect { Timber.d("starting sleep...") }.bind()
            sleep(backgroundTaskDuration).bind()
            effect { Timber.d("sleep ended...") }.bind()
            raiseError<Unit>(Throwable("Bang!!")).bind()
        }
            .onCancel(effect { Timber.d("This will never happen!") }) // for testing purposes

        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            continueOn(Dispatchers.Default)
            backgroundTask.fork()
                .bind()    // the cancellation token is ignored, we can never cancel this or get the result
            ViewState.Content(GithubService.model) // show static data
        }
            .handleError { ViewState.Error(it.errorMessage()) }
            .flatMap { IO.effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) { }
    }
}