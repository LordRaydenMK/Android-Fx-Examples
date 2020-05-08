package io.github.lordraydenmk.android_fx.example03

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Tuple3
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.ApiService
import io.github.lordraydenmk.android_fx.data.Model
import io.github.lordraydenmk.android_fx.view.ViewState
import kotlinx.coroutines.Dispatchers

class ConcurrentApiCallsViewModel(
    private val service: ApiService = ApiService.create(errorProbability = 10)
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<Model>>()
    val viewState: LiveData<ViewState<Model>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() = IO.fx {
        effect { _viewState.postValue(ViewState.Loading) }.bind()
        // performs the requests concurrently
        // if all of the succeed, we get a Tuple3 (there are 3 requests)
        // if any of them fail, the whole computation (fx-block) short-circuits
        // in case of failure, the rest of the requests are canceled (if possible)
        val results: Tuple3<Model, Model, Model> = IO.parTupledN(
            Dispatchers.IO,
            effect { service.getModel() },
            effect { service.getModel() },
            effect { service.getModel() }
        ).bind()
        ViewState.Content(results.b)    // I'm picking the 2nd result because why not
    }
        .handleError { ViewState.Error(it.message ?: "Bang!") }
        .unsafeRunScoped(viewModelScope) { result ->
            result.fold({}, { _viewState.postValue(it) })
        }
}