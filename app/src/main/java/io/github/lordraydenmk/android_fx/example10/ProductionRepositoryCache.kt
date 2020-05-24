package io.github.lordraydenmk.android_fx.example10

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.Ref
import arrow.fx.extensions.io.monadDefer.monadDefer
import arrow.fx.fix
import io.github.lordraydenmk.android_fx.data.RepositoryDto

/**
 * [RepositoryCache] implementation for production use
 * It uses [Ref] to store the cached values
 */
class ProductionRepositoryCache(private val ref: Ref<ForIO, RepositoryDto?>) : RepositoryCache {

    override fun set(repositoryDto: RepositoryDto?): IO<Unit> =
        ref.set(repositoryDto)
            .fix()

    override fun get(): IO<RepositoryDto?> = ref.get()
        .fix()

    companion object {

        fun create(): IO<RepositoryCache> = Ref<ForIO, RepositoryDto?>(IO.monadDefer(), null)
            .fix()
            .map(::ProductionRepositoryCache)
    }
}