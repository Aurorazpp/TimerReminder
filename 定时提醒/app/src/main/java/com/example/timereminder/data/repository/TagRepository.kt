package com.example.timereminder.data.repository

import com.example.timereminder.data.db.dao.TagDao
import com.example.timereminder.data.db.entity.TagEntity
import com.example.timereminder.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 标签数据仓库
 */
class TagRepository(private val tagDao: TagDao) {

    /** 获取所有标签 */
    val allTags: Flow<List<Tag>> = tagDao.getAllTags().map { entities ->
        entities.map { it.toDomainModel() }
    }

    /** 按ID获取标签 */
    suspend fun getTagById(id: Long): Tag? {
        return tagDao.getTagById(id)?.toDomainModel()
    }

    /** 保存标签 */
    suspend fun saveTag(tag: Tag): Long {
        val entity = TagEntity.fromDomainModel(tag)
        return if (tag.id == 0L) {
            tagDao.insert(entity)
        } else {
            tagDao.update(entity)
            tag.id
        }
    }

    /** 删除标签 */
    suspend fun deleteTag(tag: Tag) {
        tagDao.delete(TagEntity.fromDomainModel(tag))
    }

    /** 按ID删除 */
    suspend fun deleteTagById(id: Long) {
        tagDao.deleteById(id)
    }
}
