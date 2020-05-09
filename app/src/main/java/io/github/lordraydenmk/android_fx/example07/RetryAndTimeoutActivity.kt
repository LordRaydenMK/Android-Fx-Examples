package io.github.lordraydenmk.android_fx.example07

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.github.lordraydenmk.android_fx.R
import io.github.lordraydenmk.android_fx.view.render
import kotlinx.android.synthetic.main.layout_lce.*

class RetryAndTimeoutActivity : AppCompatActivity() {

    private val viewModel by viewModels<RetryAndTimeoutViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retry_and_timeout)

        textError.setOnClickListener { viewModel.execute() }

        viewModel.viewState.observe(this, Observer(this::render))
    }
}
