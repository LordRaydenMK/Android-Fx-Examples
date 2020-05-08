package io.github.lordraydenmk.android_fx.example04

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.extensions.list.traverse.traverse
import arrow.core.fix
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.fix
import arrow.fx.handleError
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.ApiService
import io.github.lordraydenmk.android_fx.data.Model
import io.github.lordraydenmk.android_fx.view.ViewState
import kotlinx.coroutines.Dispatchers
import java.util.*

class MultipleNetworkRequestsViewModel(
    private val service: ApiService = ApiService.create(errorProbability = 5)
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<Model>>()
    val viewState: LiveData<ViewState<Model>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() {
        val idList = (1..10).map { UUID.randomUUID() }

        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            val modelList =
                idList.parTraverse(Dispatchers.IO) { effect { service.getModelDetails(it) } }
                    .fix()
                    .bind()
            ViewState.Content(modelList.first())
        }
            .handleError { ViewState.Error(it.message ?: "Bang!") }
            .unsafeRunScoped(viewModelScope) { result ->
                result.fold({}, { _viewState.postValue(it) })
            }
    }

    fun executeSequential() {
        val idList = (1..10).map { UUID.randomUUID() }

        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            continueOn(Dispatchers.IO)
            val modelList =
                idList.traverse(IO.applicative()) { effect { service.getModelDetails(it) } }
                    .map { it.fix() }
                    .fix()
                    .bind()
            ViewState.Content(modelList.first())
        }
            .handleError { ViewState.Error(it.message ?: "Bang!") }
            .unsafeRunScoped(viewModelScope) { result ->
                result.fold({}, { _viewState.postValue(it) })
            }
    }
}