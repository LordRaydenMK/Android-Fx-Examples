package io.github.lordraydenmk.android_fx.main

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.lordraydenmk.android_fx.R
import io.github.lordraydenmk.android_fx.example01.SingleApiCallActivity
import io.github.lordraydenmk.android_fx.example02.SequentialApiCallsActivity
import io.github.lordraydenmk.android_fx.example03.ConcurrentApiCallsActivity
import io.github.lordraydenmk.android_fx.example04.MultipleNetworkRequestsActivity
import io.github.lordraydenmk.android_fx.example05.RequestWithTimeoutActivity
import io.github.lordraydenmk.android_fx.example06.RetryingActivity
import kotlinx.android.synthetic.main.activity_main.*

data class ArrowAndroidExample(
    @StringRes val nameId: Int,
    val clazz: Class<*>
)

val allExamples = listOf(
    ArrowAndroidExample(R.string.example01, SingleApiCallActivity::class.java),
    ArrowAndroidExample(R.string.example02, SequentialApiCallsActivity::class.java),
    ArrowAndroidExample(R.string.example03, ConcurrentApiCallsActivity::class.java),
    ArrowAndroidExample(R.string.example04, MultipleNetworkRequestsActivity::class.java),
    ArrowAndroidExample(R.string.example05, RequestWithTimeoutActivity::class.java),
    ArrowAndroidExample(R.string.example06, RetryingActivity::class.java)
)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        with(list) {
            layoutManager = LinearLayoutManager(context)
            adapter = ExamplesAdapter(allExamples)
        }
    }
}
