package io.github.lordraydenmk.android_fx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.lordraydenmk.android_fx.example01.SingleApiCallActivity
import io.github.lordraydenmk.android_fx.example02.SequentialApiCallsActivity
import io.github.lordraydenmk.android_fx.example03.ConcurrentApiCallsActivity
import io.github.lordraydenmk.android_fx.example04.MultipleNetworkRequestsActivity
import io.github.lordraydenmk.android_fx.example05.RequestWithTimeoutActivity
import io.github.lordraydenmk.android_fx.example06.RetryingActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        example01.setOnClickListener {
            startActivity(Intent(this, SingleApiCallActivity::class.java))
        }
        example02.setOnClickListener {
            startActivity(Intent(this, SequentialApiCallsActivity::class.java))
        }
        example03.setOnClickListener {
            startActivity(Intent(this, ConcurrentApiCallsActivity::class.java))
        }
        example04.setOnClickListener {
            startActivity(Intent(this, MultipleNetworkRequestsActivity::class.java))
        }
        example05.setOnClickListener {
            startActivity(Intent(this, RequestWithTimeoutActivity::class.java))
        }
        example06.setOnClickListener {
            startActivity(Intent(this, RetryingActivity::class.java))
        }
    }
}
