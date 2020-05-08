package io.github.lordraydenmk.android_fx.example01

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.github.lordraydenmk.android_fx.R
import io.github.lordraydenmk.android_fx.view.render
import kotlinx.android.synthetic.main.layout_lce.*

class SingleApiCallActivity : AppCompatActivity() {

    private val viewModel by viewModels<SingleApiCallViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_api_call)

        textError.setOnClickListener { viewModel.execute() }

        viewModel.viewState.observe(this, Observer(::render))
    }

}
