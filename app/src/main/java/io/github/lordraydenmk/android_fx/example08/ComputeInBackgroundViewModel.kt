package io.github.lordraydenmk.android_fx.example08

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.IO
import arrow.fx.IO.Companion.effect
import arrow.fx.extensions.fx
import arrow.fx.handleError
import arrow.integrations.kotlinx.unsafeRunScoped
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.view.ViewState
import io.github.lordraydenmk.android_fx.view.errorMessage
import kotlinx.coroutines.Dispatchers
import java.math.BigInteger
import java.util.*

class ComputeInBackgroundViewModel : ViewModel() {

    private val _viewState = MutableLiveData<ViewState<RepositoryDto>>()
    val viewState: LiveData<ViewState<RepositoryDto>>
        get() = _viewState

    init {
        execute()
    }

    fun execute() {
        IO.fx {
            effect { _viewState.postValue(ViewState.Loading) }.bind()
            continueOn(Dispatchers.Default)
            val n = 30_000.toBigInteger()
            val fact = factorial(n)
            val model = RepositoryDto(
                UUID.randomUUID(),
                "fact($n)",
                null,
                "The result is: $fact",
                42
            ) // Abusing RepositoryDto for representing a number :see_no_evil:
            ViewState.Content(model)
        }
            .handleError { ViewState.Error(it.errorMessage()) }
            .flatMap { effect { _viewState.postValue(it) } }
            .unsafeRunScoped(viewModelScope) { }
    }

    // tail-recursive version of factorial
    private fun factorial(n: BigInteger): BigInteger {
        require(n >= BigInteger.ZERO)
        tailrec fun go(n: BigInteger, acc: BigInteger = BigInteger.ONE): BigInteger =
            if (n == BigInteger.ZERO) acc
            else go(n - BigInteger.ONE, acc * n)
        return go(n)
    }
}