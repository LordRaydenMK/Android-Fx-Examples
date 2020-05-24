package io.github.lordraydenmk.android_fx.example10

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.lordraydenmk.android_fx.AndroidFxApp
import io.github.lordraydenmk.android_fx.R
import io.github.lordraydenmk.android_fx.view.render
import kotlinx.android.synthetic.main.activity_caching.*
import kotlinx.android.synthetic.main.layout_lce.*

class CachingActivity : AppCompatActivity() {

    @Suppress("UNCHECKED_CAST")
    private val viewModel by viewModels<CachingViewModel>() {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CachingViewModel(AndroidFxApp.app(this@CachingActivity).repositoryCache) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caching)

        textError.setOnClickListener { viewModel.execute() }
        btnClearCache.setOnClickListener { viewModel.clearCache() }

        viewModel.viewState.observe(this, Observer(this::render))
    }
}
