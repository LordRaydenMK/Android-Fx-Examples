package io.github.lordraydenmk.android_fx.example10

import arrow.fx.IO
import io.github.lordraydenmk.android_fx.data.RepositoryDto

interface RepositoryCache {

    fun set(repositoryDto: RepositoryDto?): IO<Unit>

    fun get(): IO<RepositoryDto?>
}