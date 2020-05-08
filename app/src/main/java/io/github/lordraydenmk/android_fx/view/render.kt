package io.github.lordraydenmk.android_fx.view

import android.app.Activity
import androidx.core.view.isVisible
import io.github.lordraydenmk.android_fx.data.Model
import kotlinx.android.synthetic.main.layout_lce.*

// In a real app this is probably a bad idea
fun Activity.render(viewState: ViewState<Model>) {
    progressBar.isVisible = viewState is ViewState.Loading
    textView.isVisible = viewState is ViewState.Content
    textError.isVisible = viewState is ViewState.Error

    when (viewState) {
        ViewState.Loading -> {
            // no-op
        }
        is ViewState.Content -> textView.text = viewState.data.toString()
        is ViewState.Error -> textError.text = viewState.msg
    }
}
