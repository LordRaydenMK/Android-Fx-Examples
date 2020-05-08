package io.github.lordraydenmk.android_fx.example05

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.fx.typeclasses.Duration
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.ApiService
import io.github.lordraydenmk.android_fx.data.Model
import io.github.lordraydenmk.android_fx.view.ViewState
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RequestWithTimeoutViewModel(
    private val service: ApiService = ApiService.create(errorProbability = 15)
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
            val race = IO.raceN(
                effect { service.getModel() },
                // random number 1-4 -> 33.33% timeout probability
                IO.sleep(Duration(Random.nextLong(3) + 1, TimeUnit.SECONDS))
            ).bind()
            race.fold(
                { ViewState.Content(it) },
                { ViewState.Error("Timeout :(") }
            )
        }
            .handleError { ViewState.Error(it.message ?: "Bang!") }
            .unsafeRunScoped(viewModelScope) { result ->
                result.fold({}, { _viewState.postValue(it) })
            }
    }
}