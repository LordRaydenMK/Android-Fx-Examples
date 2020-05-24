package io.github.lordraydenmk.android_fx.fakes

import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.milliseconds
import io.github.lordraydenmk.android_fx.data.RepositoryDto
import io.github.lordraydenmk.android_fx.example10.RepositoryCache

fun testCache(value: RepositoryDto?): RepositoryCache = object : RepositoryCache {

    override fun set(repositoryDto: RepositoryDto?): IO<Unit> = IO.unit

    override fun get(): IO<RepositoryDto?> = IO.fx {
        IO.sleep(100.milliseconds).bind()
        value
    }
}