package io.github.lordraydenmk.android_fx.example09

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import io.github.lordraydenmk.android_fx.R
import io.github.lordraydenmk.android_fx.view.render

class OutliveScreenActivity : AppCompatActivity() {

    private val viewModel by viewModels<OutliveScreenViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outlive_screen)

        viewModel.viewState.observe(this, Observer(this::render))
    }
}
