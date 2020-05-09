package io.github.lordraydenmk.android_fx.example01

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.IO.Companion.effect
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.view.ViewState
import io.github.lordraydenmk.android_fx.view.errorMessage
import kotlinx.coroutines.Dispatchers

class SingleApiCallViewModel(private val service: GithubService = GithubService.create()) : ViewModel() {

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
            val repositoryDto: RepositoryDto = effect { service.getRepository() }.bind()
            continueOn(Dispatchers.Default)
            ViewState.Content(repositoryDto)
        }
            .handleError { ViewState.Error(it.errorMessage()) }
            .flatMap { effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) {}
    }

    // Does the same thing as `execute` but uses `flatMap` to chain sequential computations
    // uses map to manipulate pure values
    @Suppress("unused")
    fun alternative() {
        effect { _viewState.postValue(ViewState.Loading) }
            .continueOn(Dispatchers.IO)
            .flatMap { effect { service.getRepository() } }
            .continueOn(Dispatchers.Default)
            .map { ViewState.Content(it) }
            .handleError { ViewState.Error(it.message ?: "Ooops") }
            .flatMap { effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) { }
    }

    // Does the same thing as execute, always update the LiveData on main thread
    @Suppress("unused")
    fun alternative2() {
        IO(Dispatchers.Main) { _viewState.value = ViewState.Loading }
            .continueOn(Dispatchers.IO)
            .flatMap { effect { service.getRepository() } }
            .continueOn(Dispatchers.Default)
            .map { ViewState.Content(it) }
            .continueOn(Dispatchers.Main)
            .handleError { ViewState.Error(it.message ?: "Ooops") }
            .flatMap { effect { _viewState.value = it } }
            .unsafeRunScoped(viewModelScope) { }
    }
}