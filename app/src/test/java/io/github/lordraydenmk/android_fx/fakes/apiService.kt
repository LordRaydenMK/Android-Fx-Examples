package io.github.lordraydenmk.android_fx.fakes

import io.github.lordraydenmk.android_fx.data.ApiService
import io.github.lordraydenmk.android_fx.data.Model
import kotlinx.coroutines.delay
import java.util.*

fun successService(id: UUID, delayMs: Long = 100): ApiService =
    object : ApiService {
        override suspend fun getModel(): Model {
            delay(delayMs)
            return Model(id, "Model")
        }

        override suspend fun getModelDetails(uuid: UUID): Model {
            delay(delayMs)
            return Model(id, "Model Details")
        }
    }

fun errorService(msg: String, delayMs: Long = 100): ApiService =
    object : ApiService {
        override suspend fun getModel(): Model {
            delay(delayMs)
            throw RuntimeException(msg)
        }

        override suspend fun getModelDetails(uuid: UUID): Model = TODO("Not yet implemented")
    }