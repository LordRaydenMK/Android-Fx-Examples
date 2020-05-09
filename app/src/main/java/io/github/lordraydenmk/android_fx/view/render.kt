package io.github.lordraydenmk.android_fx.view

import android.app.Activity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import kotlinx.android.synthetic.main.layout_lce.*

// In a real app this is really bad idea!
fun Activity.render(viewState: ViewState<RepositoryDto>) {
    progressBar.isVisible = viewState is ViewState.Loading
    groupContent.isVisible = viewState is ViewState.Content
    textError.isVisible = viewState is ViewState.Error

    when (viewState) {
        ViewState.Loading -> {
            // no-op
        }
        is ViewState.Content -> with(viewState.data) {
            if (logoResId != null) {
                imageLogo.setImageResource(logoResId)
            } else {
                imageLogo.isGone = true
            }
            textName.text = name
            textDescription.text = description
            textStarsCount.text = stars.toString()
        }
        is ViewState.Error -> textError.text = viewState.msg
    }
}
