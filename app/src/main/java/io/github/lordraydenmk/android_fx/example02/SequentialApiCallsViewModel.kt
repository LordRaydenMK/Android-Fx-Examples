package io.github.lordraydenmk.android_fx.example02

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.view.ViewState
import io.github.lordraydenmk.android_fx.data.ApiService
import io.github.lordraydenmk.android_fx.data.Model
import kotlinx.coroutines.Dispatchers

class SequentialApiCallsViewModel constructor(
    private val apiService: ApiService = ApiService.create()
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<Model>>()
    val viewState: LiveData<ViewState<Model>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() {
        IO.fx {
            _viewState.postValue(ViewState.Loading)
            continueOn(Dispatchers.IO)
            val model = effect { apiService.getModel() }.bind()
            val modelDetails = effect { apiService.getModelDetails(model.id) }.bind()
            continueOn(Dispatchers.Default)
            ViewState.Content(modelDetails)
        }.handleError { ViewState.Error(it.message ?: "Bang!") }
            .unsafeRunScoped(viewModelScope) { result ->
                result.fold({}, { _viewState.postValue(it) })
            }
    }
}