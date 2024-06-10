package org.saudigitus.emis.data.model

data class Subject(
    val uid: String,
    val code: String?,
    val color: String?,
    val displayName: String?,
) {
    override fun toString() = displayName ?: ""
}
