package com.example.timereminder.domain.model

/**
 * 标签领域模型
 */
data class Tag(
    val id: Long = 0,
    val name: String,
    val color: Int = 0xFF448AFF.toInt(),
    val icon: String? = null
)
