package io.github.lordraydenmk.android_fx.view

sealed class ViewState<out A> {
    object Loading : ViewState<Nothing>()
    data class Content<A>(val data: A) : ViewState<A>()
    data class Error(val msg: String) : ViewState<Nothing>()
}