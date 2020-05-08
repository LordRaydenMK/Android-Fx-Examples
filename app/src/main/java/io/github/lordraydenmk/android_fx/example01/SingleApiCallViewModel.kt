package io.github.lordraydenmk.android_fx.example01

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.ApiService
import io.github.lordraydenmk.android_fx.data.Model
import io.github.lordraydenmk.android_fx.view.ViewState
import kotlinx.coroutines.Dispatchers

class SingleApiCallViewModel(private val service: ApiService = ApiService.create()) : ViewModel() {

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
            val model = effect { service.getModel() }.bind()
            ViewState.Content(model)
        }
            .handleError { ViewState.Error(it.message ?: "Ooops!") }
            .unsafeRunScoped(viewModelScope) { result ->
                result.fold(
                    {},
                    { _viewState.postValue(it) })
            }
    }
}