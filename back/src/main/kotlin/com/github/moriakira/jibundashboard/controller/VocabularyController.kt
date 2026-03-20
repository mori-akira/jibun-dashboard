package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.VocabularyApi
import com.github.moriakira.jibundashboard.generated.model.Vocabulary
import com.github.moriakira.jibundashboard.generated.model.VocabularyBase
import com.github.moriakira.jibundashboard.generated.model.VocabularyId
import com.github.moriakira.jibundashboard.generated.model.VocabularyTag
import com.github.moriakira.jibundashboard.generated.model.VocabularyTagBase
import com.github.moriakira.jibundashboard.generated.model.VocabularyTagId
import com.github.moriakira.jibundashboard.service.VocabularyModel
import com.github.moriakira.jibundashboard.service.VocabularyService
import com.github.moriakira.jibundashboard.service.VocabularyTagModel
import com.github.moriakira.jibundashboard.service.VocabularyTagService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.util.UUID

@Suppress("TooManyFunctions")
@RestController
class VocabularyController(
    private val currentAuth: CurrentAuth,
    private val vocabularyService: VocabularyService,
    private val vocabularyTagService: VocabularyTagService,
) : VocabularyApi {

    override fun getVocabularies(
        vocabularyName: String?,
        tags: List<String>?,
    ): ResponseEntity<List<Vocabulary>> {
        val list =
            vocabularyService.listByConditions(
                userId = currentAuth.userId,
                vocabularyName = vocabularyName,
                tags = tags,
            )
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    @Suppress("ReturnCount")
    override fun postVocabularies(
        vocabularyBase: VocabularyBase?,
    ): ResponseEntity<VocabularyId> {
        requireNotNull(vocabularyBase) { "Request body is required." }
        val vocabularyId = vocabularyService.create(vocabularyBase.toModel())
        return ResponseEntity.status(HttpStatus.CREATED).body(VocabularyId(UUID.fromString(vocabularyId)))
    }

    @Suppress("ReturnCount")
    override fun getVocabulariesById(vocabularyId: UUID): ResponseEntity<Vocabulary> {
        val model =
            vocabularyService.getByVocabularyIdForUser(vocabularyId.toString(), currentAuth.userId)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(model.toApi())
    }

    @Suppress("ReturnCount")
    override fun putVocabulariesById(
        vocabularyId: UUID,
        vocabularyBase: VocabularyBase?,
    ): ResponseEntity<VocabularyId> {
        requireNotNull(vocabularyBase) { "Request body is required." }
        vocabularyService.getByVocabularyIdForUser(vocabularyId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        vocabularyService.put(vocabularyBase.toModel(vocabularyId.toString()))
        return ResponseEntity.ok(VocabularyId(vocabularyId))
    }

    @Suppress("ReturnCount")
    override fun deleteVocabulariesById(vocabularyId: UUID): ResponseEntity<Unit> {
        vocabularyService.getByVocabularyIdForUser(vocabularyId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        vocabularyService.delete(currentAuth.userId, vocabularyId.toString())
        return ResponseEntity.noContent().build()
    }

    override fun getVocabularyTags(
        vocabularyTag: String?,
    ): ResponseEntity<List<VocabularyTag>> {
        val list =
            vocabularyTagService.listByConditions(
                userId = currentAuth.userId,
                vocabularyTag = vocabularyTag,
            )
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    @Suppress("ReturnCount")
    override fun postVocabularyTags(
        vocabularyTagBase: VocabularyTagBase?,
    ): ResponseEntity<VocabularyTagId> {
        requireNotNull(vocabularyTagBase) { "Request body is required." }
        val vocabularyTagId = vocabularyTagService.create(vocabularyTagBase.toModel())
        return ResponseEntity.status(HttpStatus.CREATED).body(VocabularyTagId(UUID.fromString(vocabularyTagId)))
    }

    @Suppress("ReturnCount")
    override fun getVocabularyTagsById(vocabularyTagId: UUID): ResponseEntity<VocabularyTag> {
        val model =
            vocabularyTagService.getByVocabularyTagIdForUser(vocabularyTagId.toString(), currentAuth.userId)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(model.toApi())
    }

    @Suppress("ReturnCount")
    override fun putVocabularyTagsById(
        vocabularyTagId: UUID,
        vocabularyTagBase: VocabularyTagBase?,
    ): ResponseEntity<VocabularyTagId> {
        requireNotNull(vocabularyTagBase) { "Request body is required." }
        vocabularyTagService.getByVocabularyTagIdForUser(vocabularyTagId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        vocabularyTagService.put(vocabularyTagBase.toModel(vocabularyTagId.toString()))
        return ResponseEntity.ok(VocabularyTagId(vocabularyTagId))
    }

    @Suppress("ReturnCount")
    override fun deleteVocabularyTagsById(vocabularyTagId: UUID): ResponseEntity<Unit> {
        vocabularyTagService.getByVocabularyTagIdForUser(vocabularyTagId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        vocabularyTagService.delete(currentAuth.userId, vocabularyTagId.toString())
        return ResponseEntity.noContent().build()
    }

    private fun VocabularyModel.toApi(): Vocabulary =
        Vocabulary(
            vocabularyId = UUID.fromString(this.vocabularyId),
            name = this.name,
            description = this.description,
            tags =
            this.tags.map { tag ->
                VocabularyTag(
                    vocabularyTagId = UUID.fromString(tag.vocabularyTagId),
                    vocabularyTag = tag.vocabularyTag,
                )
            }.toSet(),
            createdDateTime = OffsetDateTime.parse(this.createdDateTime),
            updatedDateTime = OffsetDateTime.parse(this.updatedDateTime),
        )

    private fun VocabularyTagModel.toApi(): VocabularyTag =
        VocabularyTag(
            vocabularyTagId = UUID.fromString(this.vocabularyTagId),
            vocabularyTag = this.vocabularyTag,
        )

    private fun VocabularyBase.toModel(): VocabularyModel =
        VocabularyModel(
            vocabularyId = "",
            userId = currentAuth.userId,
            name = this.name,
            description = this.description,
            tags =
            this.tags?.map { tag ->
                VocabularyTagModel(
                    vocabularyTagId = tag.vocabularyTagId?.toString() ?: UUID.randomUUID().toString(),
                    userId = currentAuth.userId,
                    vocabularyTag = tag.vocabularyTag,
                )
            } ?: emptyList(),
        )

    private fun VocabularyBase.toModel(vocabularyId: String): VocabularyModel =
        VocabularyModel(
            vocabularyId = vocabularyId,
            userId = currentAuth.userId,
            name = this.name,
            description = this.description,
            tags =
            this.tags?.map { tag ->
                VocabularyTagModel(
                    vocabularyTagId = tag.vocabularyTagId?.toString() ?: UUID.randomUUID().toString(),
                    userId = currentAuth.userId,
                    vocabularyTag = tag.vocabularyTag,
                )
            } ?: emptyList(),
        )

    private fun VocabularyTagBase.toModel(): VocabularyTagModel =
        VocabularyTagModel(
            vocabularyTagId = "",
            userId = currentAuth.userId,
            vocabularyTag = this.vocabularyTag,
        )

    private fun VocabularyTagBase.toModel(vocabularyTagId: String): VocabularyTagModel =
        VocabularyTagModel(
            vocabularyTagId = vocabularyTagId,
            userId = currentAuth.userId,
            vocabularyTag = this.vocabularyTag,
        )
}
