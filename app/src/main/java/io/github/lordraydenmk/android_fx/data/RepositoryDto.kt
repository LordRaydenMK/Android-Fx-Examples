package io.github.lordraydenmk.android_fx.data

import androidx.annotation.DrawableRes
import java.util.*

// DTO (Data transfer object) representing a Github Repository
// In a real app one would use Moshi or a similar library to convert from JSON to this model
// also the logo would probably be a Uri
data class RepositoryDto(
    val id: UUID,
    val name: String,
    @DrawableRes val logoResId: Int?,
    val description: String,
    val stars: Int
)