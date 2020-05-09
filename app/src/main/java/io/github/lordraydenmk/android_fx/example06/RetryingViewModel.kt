package io.github.lordraydenmk.android_fx.example06

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.*
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.monad.monad
import arrow.fx.extensions.io.monadDefer.monadDefer
import arrow.fx.typeclasses.milliseconds
import arrow.fx.typeclasses.seconds
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.ApiService
import io.github.lordraydenmk.android_fx.data.Model
import io.github.lordraydenmk.android_fx.view.ViewState
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class RetryingViewModel(
    private val service: ApiService = ApiService.create(errorProbability = 85, delayMillis = 300)
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<Model>>()
    val viewState: LiveData<ViewState<Model>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() {
        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            continueOn(Dispatchers.IO)
            val model = effect { service.getModel() }
                .onError { IO { Timber.e(it, "Error calling `getModel`") } }
                .retry(IO.concurrent(), complexPolicy())
                .bind()
            ViewState.Content(model)
        }
            .handleError { ViewState.Error(it.message ?: "Ooops!") }
            .unsafeRunScoped(viewModelScope) { result ->
                result.fold(
                    {},
                    { _viewState.postValue(it) })
            }
    }

    // exponential backoff with initial delay 1 sec and factor of 2
    private fun <A> exponential() =
        Schedule.exponential<ForIO, A>(IO.monad(), base = 1.seconds, factor = 2.0)

    //This policy will recur with exponential backoff as long as the delay is less than 60 seconds
    // and then continue with a spaced delay of 60 seconds. The delay is also randomized slightly to
    // avoid coordinated backoff from multiple services. Finally we also collect every input to the
    // schedule and return it. When used with retry this will return a list of exceptions that
    // occurred on failed attempts.
    private fun <A> complexPolicy() =
        Schedule.withMonad(IO.monad()) {
            exponential<A>(10.milliseconds).whileOutput { it.nanoseconds < 60.seconds.nanoseconds }
                .andThen(spaced<A>(60.seconds) and recurs(100)).jittered(IO.monadDefer())
                .zipRight(identity<A>().collect())
        }
}