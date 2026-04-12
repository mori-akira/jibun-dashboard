package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.repository.VocabularyItem
import com.github.moriakira.jibundashboard.repository.VocabularyRepository
import com.github.moriakira.jibundashboard.repository.VocabularyTagEmbed
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class VocabularyService(
    private val vocabularyRepository: VocabularyRepository,
) {
    fun listByConditions(
        userId: String,
        vocabularyName: String? = null,
        description: String? = null,
        tags: List<String>? = null,
    ): List<VocabularyModel> =
        vocabularyRepository.findByUser(userId, vocabularyName, description)
            .filter { item ->
                tags.isNullOrEmpty() || item.tags?.any { tag -> tags.contains(tag.vocabularyTag) } == true
            }
            .map { it.toDomain() }

    fun getByVocabularyId(vocabularyId: String): VocabularyModel? =
        vocabularyRepository.getByVocabularyId(vocabularyId)?.toDomain()

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
            tags =
            this.tags?.map { embed ->
                VocabularyTagModel(
                    vocabularyTagId = embed.vocabularyTagId!!,
                    userId = this.userId!!,
                    vocabularyTag = embed.vocabularyTag!!,
                    order = embed.order!!,
                )
            } ?: emptyList(),
            createdDateTime = this.createdDateTime!!,
            updatedDateTime = this.updatedDateTime!!,
        )

    private fun VocabularyModel.toItem(): VocabularyItem =
        VocabularyItem().also { item ->
            item.vocabularyId = this.vocabularyId
            item.userId = this.userId
            item.name = this.name
            item.description = this.description
            item.tags =
                this.tags.map { tag ->
                    VocabularyTagEmbed().also { embed ->
                        embed.vocabularyTagId = tag.vocabularyTagId
                        embed.vocabularyTag = tag.vocabularyTag
                        embed.order = tag.order
                    }
                }
            item.createdDateTime = this.createdDateTime
            item.updatedDateTime = this.updatedDateTime
        }
}

data class VocabularyModel(
    val vocabularyId: String,
    val userId: String,
    val name: String,
    val description: String?,
    val tags: List<VocabularyTagModel>,
    val createdDateTime: String = "",
    val updatedDateTime: String = "",
)
