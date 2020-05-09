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
import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import kotlinx.coroutines.Dispatchers

class SequentialApiCallsViewModel constructor(
    private val githubService: GithubService = GithubService.create()
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
        }.handleError { ViewState.Error(it.message ?: "Bang!") }
            .unsafeRunScoped(viewModelScope) { result ->
                result.fold({}, { _viewState.postValue(it) })
            }
    }
}