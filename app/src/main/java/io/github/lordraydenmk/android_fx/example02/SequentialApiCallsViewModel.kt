package io.github.lordraydenmk.android_fx.example02

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.IO.Companion.effect
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.view.ViewState
import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.view.errorMessage
import kotlinx.coroutines.Dispatchers

class SequentialApiCallsViewModel constructor(
    private val githubService: GithubService = GithubService.create(errorProbability = 10)
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<RepositoryDto>>()
    val viewState: LiveData<ViewState<RepositoryDto>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() {
        IO.fx {
            _viewState.postValue(ViewState.Loading)
            continueOn(Dispatchers.IO)
            val model = effect { githubService.getRepository() }.bind()
            val modelDetails = effect { githubService.getRepositoryDetails(model.id) }.bind()
            continueOn(Dispatchers.Default)
            ViewState.Content(modelDetails)
        }
            .handleError { ViewState.Error(it.errorMessage()) }
            .flatMap { effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) {}
    }

    fun bonus() {
        IO.fx {
            _viewState.postValue(ViewState.Loading)
            continueOn(Dispatchers.IO)
            val model = effect { githubService.getRepository() }.bind()
            val model2 = effect { githubService.getRepository() }.bind()
            val modelDetails = effect { githubService.getRepositoryDetails(model.id) }.bind()
            val ignored = effect { githubService.getRepositoryDetails(model2.id) }.bind()
            continueOn(Dispatchers.Default)
            ViewState.Content(modelDetails)
        }
            .handleError { ViewState.Error(it.errorMessage()) }
            .flatMap { effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) {}
    }
}