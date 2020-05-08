package io.github.lordraydenmk.android_fx.example06

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import io.github.lordraydenmk.android_fx.R
import io.github.lordraydenmk.android_fx.view.render
import kotlinx.android.synthetic.main.layout_lce.*

class RetryingActivity : AppCompatActivity() {

    private val viewModel by viewModels<RetryingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrying)

        textError.setOnClickListener { viewModel.execute() }

        viewModel.viewState.observe(this, Observer(this::render))
    }
}
