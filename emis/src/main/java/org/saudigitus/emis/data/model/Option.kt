package org.saudigitus.emis.data.model

data class Option(
    val uid: String,
    val code: String?,
    val displayName: String?,
) {
    override fun toString() = displayName ?: ""
}
