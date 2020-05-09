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
            val model: Model = effect { service.getModel() }.bind()
            continueOn(Dispatchers.Default)
            ViewState.Content(model)
        }
            .handleError { ViewState.Error(it.message ?: "Ooops") }
            .flatMap { effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) {}
    }

    // Does the same thing as `execute` but uses `flatMap` to chain sequential computations
    // uses map to manipulate pure values
    fun alternative() {
        effect { _viewState.postValue(ViewState.Loading) }
            .continueOn(Dispatchers.IO)
            .flatMap { effect { service.getModel() } }
            .continueOn(Dispatchers.Default)
            .map { ViewState.Content(it) }
            .handleError { ViewState.Error(it.message ?: "Ooops") }
            .flatMap { effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) { }
    }

    // Does the same thing as execute, always update the LiveData on main thread
    fun alternative2() {
        IO(Dispatchers.Main) { _viewState.value = ViewState.Loading }
            .continueOn(Dispatchers.IO)
            .flatMap { effect { service.getModel() } }
            .continueOn(Dispatchers.Default)
            .map { ViewState.Content(it) }
            .continueOn(Dispatchers.Main)
            .handleError { ViewState.Error(it.message ?: "Ooops") }
            .flatMap { effect { _viewState.value = it } }
            .unsafeRunScoped(viewModelScope) { }
    }
}