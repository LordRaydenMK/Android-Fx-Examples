package io.github.lordraydenmk.android_fx.example05

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.fx.typeclasses.Duration
import arrow.fx.typeclasses.seconds
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.ApiService
import io.github.lordraydenmk.android_fx.data.Model
import io.github.lordraydenmk.android_fx.view.ViewState

class RequestWithTimeoutViewModel(
    private val service: ApiService = ApiService.create(errorProbability = 15)
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<Model>>()
    val viewState: LiveData<ViewState<Model>>
        get() = _viewState

    init {
        execute()
    }

    fun execute(timeout: Duration = 1.seconds) {
        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            val model = effect { service.getModel() }
                .waitFor(timeout)
                .bind()
            ViewState.Content(model)
        }
            .handleError { ViewState.Error(it.message ?: "Bang!") }
            .unsafeRunScoped(viewModelScope) { result ->
                result.fold({}, { _viewState.postValue(it) })
            }
    }
}