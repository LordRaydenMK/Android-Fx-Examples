package io.github.lordraydenmk.android_fx.fakes

import io.github.lordraydenmk.android_fx.data.ApiService
import io.github.lordraydenmk.android_fx.data.Model
import kotlinx.coroutines.delay
import java.util.*

fun successService(id: UUID): ApiService =
    object : ApiService {
        override suspend fun getModel(): Model {
            delay(100)
            return Model(id, "Model")
        }

        override suspend fun getModelDetails(uuid: UUID): Model {
            delay(100)
            return Model(id, "Model Details")
        }
    }

fun errorService(msg: String): ApiService =
    object : ApiService {
        override suspend fun getModel(): Model {
            delay(100)
            throw RuntimeException(msg)
        }

        override suspend fun getModelDetails(uuid: UUID): Model = TODO("Not yet implemented")
    }