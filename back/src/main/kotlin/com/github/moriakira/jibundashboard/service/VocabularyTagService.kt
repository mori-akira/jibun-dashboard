package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.VocabularyTagItem
import com.github.moriakira.jibundashboard.repository.VocabularyTagRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VocabularyTagService(
    private val vocabularyTagRepository: VocabularyTagRepository,
) {
    fun listByConditions(
        userId: String,
        vocabularyTag: String? = null,
    ): List<VocabularyTagModel> = vocabularyTagRepository.findByUser(userId, vocabularyTag).map { it.toDomain() }

    fun findByIds(userId: String, tagIds: List<String>): List<VocabularyTagModel> =
        vocabularyTagRepository.findByIds(userId, tagIds).map { it.toDomain() }

    fun getByVocabularyTagId(vocabularyTagId: String): VocabularyTagModel? =
        vocabularyTagRepository.getByVocabularyTagId(vocabularyTagId)?.toDomain()

    fun getByVocabularyTagIdForUser(vocabularyTagId: String, userId: String): VocabularyTagModel? =
        getByVocabularyTagId(vocabularyTagId)?.takeIf { it.userId == userId }

    fun create(model: VocabularyTagModel): String = put(model.copy(vocabularyTagId = UUID.randomUUID().toString()))

    fun put(model: VocabularyTagModel): String {
        vocabularyTagRepository.put(model.toItem())
        return model.vocabularyTagId
    }

    fun delete(userId: String, vocabularyTagId: String) {
        vocabularyTagRepository.delete(userId, vocabularyTagId)
    }

    private fun VocabularyTagItem.toDomain(): VocabularyTagModel =
        VocabularyTagModel(
            vocabularyTagId = this.vocabularyTagId!!,
            userId = this.userId!!,
            vocabularyTag = this.vocabularyTag!!,
            order = this.order!!,
        )

    private fun VocabularyTagModel.toItem(): VocabularyTagItem =
        VocabularyTagItem().also { item ->
            item.vocabularyTagId = this.vocabularyTagId
            item.userId = this.userId
            item.vocabularyTag = this.vocabularyTag
            item.order = this.order
        }
}

data class VocabularyTagModel(
    val vocabularyTagId: String,
    val userId: String,
    val vocabularyTag: String,
    val order: Int,
)
