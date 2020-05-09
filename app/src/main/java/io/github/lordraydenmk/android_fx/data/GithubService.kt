package io.github.lordraydenmk.android_fx.data

import io.github.lordraydenmk.android_fx.R
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.*
import kotlin.random.Random

// API Service, in a real app implemented by Retrofit
interface GithubService {

    suspend fun getRepository(): RepositoryDto

    suspend fun getRepositoryDetails(uuid: UUID): RepositoryDto

    companion object {

        val model = RepositoryDto(
            UUID.randomUUID(),
            "arrow-kt/arrow",
            R.drawable.ic_arrow_core_brand_sidebar,
            "Functional companion to Kotlin's Standard Library http://arrow-kt.io",
            42  // Answer to the Ultimate Question of Life, the Universe, and Everything
        )

        // Fake implementation. Randomly returning errors
        fun create(errorProbability: Int = 25, delayMillis: Long = 1500): GithubService =
            object : GithubService {

                override suspend fun getRepository(): RepositoryDto {
                    delay(delayMillis) // simulate delay
                    val error = Random.nextInt(100) // simulate network errors
                    return if (error <= errorProbability) throw IOException("Bang!")
                    else model
                }

                override suspend fun getRepositoryDetails(uuid: UUID): RepositoryDto {
                    delay(delayMillis / 2) // simulate delay
                    val error = Random.nextInt(100) // simulate network errors
                    return if (error <= errorProbability) throw IOException("Bang!")
                    else model.copy(
                        id = uuid,
                        description = "${model.description} This is a fake details view that should contain more data, but for the purposes of the example, it only contains a longer description text. Why are you still reading this?"
                    )
                }
            }

        fun create(responses: List<suspend () -> RepositoryDto>): GithubService =

            object : GithubService {

                var count = 0

                override suspend fun getRepository(): RepositoryDto =
                    responses[count++]()

                override suspend fun getRepositoryDetails(uuid: UUID): RepositoryDto =
                    TODO("Not yet implemented")
            }

        val slowResponseThenErrorThenFast: List<suspend () -> RepositoryDto> =
            listOf<suspend () -> RepositoryDto>(
                { delay(1500); model },
                { delay(500); throw RuntimeException("Bang!") },
                { delay(500); model }
            )
    }
}