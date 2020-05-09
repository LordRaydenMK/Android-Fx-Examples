package io.github.lordraydenmk.android_fx.example04

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.extensions.list.traverse.traverse
import arrow.core.fix
import arrow.fx.IO
import arrow.fx.IO.Companion.effect
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.fix
import arrow.fx.handleError
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.view.ViewState
import io.github.lordraydenmk.android_fx.view.errorMessage
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
        // A list of 10 random Github repo IDs
        val idList: List<UUID> = (1..10).map { UUID.randomUUID() }

        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            // For each UUID in the list we execute the supplied function
            // which here is fetching from network
            // if all requests succeed, we get the result in a list
            // if any request fails, we get the first exception as Throwable
            val modelList: List<RepositoryDto> =
                idList.parTraverse(Dispatchers.IO) { effect { service.getRepositoryDetails(it) } }
                    .fix()
                    .bind()
            ViewState.Content(modelList.first())
        }
            .handleError { ViewState.Error(it.errorMessage()) }
            .flatMap { effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) { }
    }

    fun executeSequential() {
        val idList: List<UUID> = (1..10).map { UUID.randomUUID() }

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