package io.github.lordraydenmk.android_fx.example10

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.async.effectMap
import arrow.fx.handleError
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.view.ViewState
import io.github.lordraydenmk.android_fx.view.errorMessage
import kotlinx.coroutines.Dispatchers

class CachingViewModel(
    private val repositoryCache: RepositoryCache,
    private val service: GithubService = GithubService.create()
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<RepositoryDto>>()
    val viewState: LiveData<ViewState<RepositoryDto>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() {
        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            continueOn(Dispatchers.IO)
            val repo = cachedOrFetch(repositoryCache, service).bind()
            ViewState.Content(repo)
        }
            .handleError { ViewState.Error(it.errorMessage()) }
            .effectMap { _viewState.postValue(it) }
            .unsafeRunScoped(viewModelScope) {}
    }

    // This logic can be extracted into a Repository layer
    private fun cachedOrFetch(
        repoCache: RepositoryCache,
        service: GithubService
    ): IO<RepositoryDto> =
        IO.fx {
            val repo = repoCache.get().bind()
            if (repo == null) {
                //get and cache
                val result = effect { service.getRepository() }.bind()
                repoCache.set(result).bind()
                result
            } else {
                repo
            }
        }

    fun clearCache(): Unit =
        repositoryCache.set(null)
            .unsafeRunScoped(viewModelScope) {}

}