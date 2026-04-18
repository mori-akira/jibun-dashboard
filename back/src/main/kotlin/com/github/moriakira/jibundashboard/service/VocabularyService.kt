package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.VocabularyItem
import com.github.moriakira.jibundashboard.repository.VocabularyRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class VocabularyService(
    private val vocabularyRepository: VocabularyRepository,
    private val vocabularyTagService: VocabularyTagService,
) {
    fun listByConditions(
        userId: String,
        vocabularyName: String? = null,
        description: String? = null,
        tagIds: List<String>? = null,
    ): List<VocabularyModel> {
        val items = vocabularyRepository.findByUser(userId, vocabularyName, description)
            .filter { item -> tagIds.isNullOrEmpty() || item.tagIds?.any { tagIds.contains(it) } == true }
        val allTagIds = items.flatMap { it.tagIds ?: emptyList() }.distinct()
        val tagMap =
            if (allTagIds.isEmpty()) {
                emptyMap()
            } else {
                vocabularyTagService.findByIds(userId, allTagIds).associateBy { it.vocabularyTagId }
            }
        return items.map { item ->
            val resolvedTags = (item.tagIds ?: emptyList()).mapNotNull { tagId -> tagMap[tagId] }
            item.toDomain().copy(tags = resolvedTags)
        }
    }

    fun getByVocabularyId(vocabularyId: String): VocabularyModel? =
        vocabularyRepository.getByVocabularyId(vocabularyId)?.let { item ->
            val resolvedTags = vocabularyTagService.findByIds(item.userId!!, item.tagIds ?: emptyList())
            item.toDomain().copy(tags = resolvedTags)
        }

    fun getByVocabularyIdForUser(vocabularyId: String, userId: String): VocabularyModel? =
        getByVocabularyId(vocabularyId)?.takeIf { it.userId == userId }

    fun create(model: VocabularyModel): String = put(model.copy(vocabularyId = UUID.randomUUID().toString()))

    fun put(model: VocabularyModel): String {
        val existing = vocabularyRepository.getByVocabularyId(model.vocabularyId)?.toDomain()
        val now = OffsetDateTime.now().toString()
        val toSave =
            model.copy(
                createdDateTime = existing?.createdDateTime ?: now,
                updatedDateTime = now,
            )
        vocabularyRepository.put(toSave.toItem())
        return model.vocabularyId
    }

    fun delete(userId: String, vocabularyId: String) {
        vocabularyRepository.delete(userId, vocabularyId)
    }

    private fun VocabularyItem.toDomain(): VocabularyModel =
        VocabularyModel(
            vocabularyId = this.vocabularyId!!,
            userId = this.userId!!,
            name = this.name!!,
            description = this.description,
            tagIds = this.tagIds ?: emptyList(),
            createdDateTime = this.createdDateTime ?: "",
            updatedDateTime = this.updatedDateTime ?: "",
        )

    private fun VocabularyModel.toItem(): VocabularyItem =
        VocabularyItem().also { item ->
            item.vocabularyId = this.vocabularyId
            item.userId = this.userId
            item.name = this.name
            item.description = this.description
            item.tagIds = this.tagIds
            item.createdDateTime = this.createdDateTime
            item.updatedDateTime = this.updatedDateTime
        }
}

data class VocabularyModel(
    val vocabularyId: String,
    val userId: String,
    val name: String,
    val description: String?,
    val tagIds: List<String> = emptyList(),
    val tags: List<VocabularyTagModel> = emptyList(),
    val createdDateTime: String = "",
    val updatedDateTime: String = "",
)
