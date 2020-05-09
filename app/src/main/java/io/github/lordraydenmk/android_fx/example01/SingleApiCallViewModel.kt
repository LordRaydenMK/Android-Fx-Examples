package io.github.lordraydenmk.android_fx.example01

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.IO.Companion.effect
import arrow.fx.extensions.fx
import arrow.fx.handleErrorWith
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
            val model = effect { service.getModel() }.bind()
            continueOn(Dispatchers.Default)
            val success = ViewState.Content(model)
            effect { _viewState.postValue(success) }.bind()
        }
            .handleErrorWith {
                effect { _viewState.postValue(ViewState.Error(it.message ?: "Ooops")) }
            }
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
            .flatMap { effect { _viewState.postValue(it) } }
            .handleErrorWith {
                effect { _viewState.postValue(ViewState.Error(it.message ?: "Ooops")) }
            }
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
            .flatMap { effect { _viewState.value = it } }
            .handleErrorWith {
                effect { _viewState.value = ViewState.Error(it.message ?: "Ooops") }
            }
            .unsafeRunScoped(viewModelScope) { }
    }
}