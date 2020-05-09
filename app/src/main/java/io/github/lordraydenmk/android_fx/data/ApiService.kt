package io.github.lordraydenmk.android_fx.data

import kotlinx.coroutines.delay
import java.io.IOException
import java.util.*
import kotlin.random.Random

// API Service, in a real app implemented by Retrofit
interface ApiService {

    suspend fun getModel(): Model

    suspend fun getModelDetails(uuid: UUID): Model

    companion object {

        // Fake implementation. Randomly returning errors
        fun create(errorProbability: Int = 25, delayMillis: Long = 1500): ApiService =
            object : ApiService {

                override suspend fun getModel(): Model {
                    delay(delayMillis) // simulate delay
                    val error = Random.nextInt(100)
                    return if (error <= errorProbability) throw IOException("Bang!")
                    else Model(UUID.randomUUID(), "My Model")
                }

                override suspend fun getModelDetails(uuid: UUID): Model {
                    delay(1000) // simulate delay
                    val error = Random.nextInt(100)
                    return if (error <= errorProbability) throw IOException("Bang!")
                    else Model(uuid, "Model Details")
                }
            }
    }
}