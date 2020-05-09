package io.github.lordraydenmk.android_fx.fakes

import io.github.lordraydenmk.android_fx.data.GithubService
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import java.util.*

class FakeGithubService : GithubService {

    private var responses: List<suspend () -> RepositoryDto> = emptyList()
    var count = 0
        private set

    override suspend fun getRepository(): RepositoryDto =
        responses[count++]()

    override suspend fun getRepositoryDetails(uuid: UUID): RepositoryDto =
        TODO(reason = "Not yet implemented")

    companion object {

        fun createWithResponses(responses: List<suspend () -> RepositoryDto>): FakeGithubService =
            FakeGithubService().also { it.responses = responses }
    }
}