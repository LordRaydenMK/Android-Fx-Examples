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
import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.view.ViewState
import kotlinx.coroutines.Dispatchers
import java.util.*

class MultipleNetworkRequestsViewModel(
    private val service: GithubService = GithubService.create(errorProbability = 5)
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<RepositoryDto>>()
    val viewState: LiveData<ViewState<RepositoryDto>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() {
        val idList = (1..10).map { UUID.randomUUID() }

        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            val modelList =
                idList.parTraverse(Dispatchers.IO) { effect { service.getRepositoryDetails(it) } }
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
                idList.traverse(IO.applicative()) { effect { service.getRepositoryDetails(it) } }
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