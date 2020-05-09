package io.github.lordraydenmk.android_fx.fakes

import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import kotlinx.coroutines.delay
import java.util.*

fun successService(id: UUID, delayMs: Long = 100): GithubService =
    object : GithubService {
        override suspend fun getRepository(): RepositoryDto {
            delay(delayMs)
            return RepositoryDto(id, "Model", null, "", 0)
        }

        override suspend fun getRepositoryDetails(uuid: UUID): RepositoryDto {
            delay(delayMs)
            return RepositoryDto(id, "Model Details", null, "", 0)
        }
    }

fun errorService(msg: String, delayMs: Long = 100): GithubService =
    object : GithubService {
        override suspend fun getRepository(): RepositoryDto {
            delay(delayMs)
            throw RuntimeException(msg)
        }

        override suspend fun getRepositoryDetails(uuid: UUID): RepositoryDto =
            TODO("Not yet implemented")
    }

fun repositoryDto(id: UUID, name: String): RepositoryDto = RepositoryDto(id, name, null, "", 0)