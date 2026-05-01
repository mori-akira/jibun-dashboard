package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.VocabularyApi
import com.github.moriakira.jibundashboard.generated.model.Vocabulary
import com.github.moriakira.jibundashboard.generated.model.VocabularyId
import com.github.moriakira.jibundashboard.generated.model.VocabularyQuizHistory
import com.github.moriakira.jibundashboard.generated.model.VocabularyQuizHistoryAnswer
import com.github.moriakira.jibundashboard.generated.model.VocabularyQuizHistoryBase
import com.github.moriakira.jibundashboard.generated.model.VocabularyQuizHistoryId
import com.github.moriakira.jibundashboard.generated.model.VocabularyRequest
import com.github.moriakira.jibundashboard.generated.model.VocabularyTag
import com.github.moriakira.jibundashboard.generated.model.VocabularyTagBase
import com.github.moriakira.jibundashboard.generated.model.VocabularyTagId
import com.github.moriakira.jibundashboard.service.VocabularyModel
import com.github.moriakira.jibundashboard.service.VocabularyQuizHistoryAnswerModel
import com.github.moriakira.jibundashboard.service.VocabularyQuizHistoryModel
import com.github.moriakira.jibundashboard.service.VocabularyQuizHistoryService
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
    private val vocabularyQuizHistoryService: VocabularyQuizHistoryService,
) : VocabularyApi {

    override fun getVocabularies(
        vocabularyName: String?,
        description: String?,
        tagIds: List<UUID>?,
    ): ResponseEntity<List<Vocabulary>> {
        val list =
            vocabularyService.listByConditions(
                userId = currentAuth.userId,
                vocabularyName = vocabularyName,
                description = description,
                tagIds = tagIds?.map { it.toString() },
            )
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    override fun postVocabularies(
        vocabularyRequest: VocabularyRequest?,
    ): ResponseEntity<VocabularyId> {
        requireNotNull(vocabularyRequest) { "Request body is required." }
        val vocabularyId = vocabularyService.create(vocabularyRequest.toModel())
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
        vocabularyRequest: VocabularyRequest?,
    ): ResponseEntity<VocabularyId> {
        requireNotNull(vocabularyRequest) { "Request body is required." }
        vocabularyService.getByVocabularyIdForUser(vocabularyId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        vocabularyService.put(vocabularyRequest.toModel(vocabularyId.toString()))
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

    override fun getVocabularyQuizHistories(): ResponseEntity<List<VocabularyQuizHistory>> {
        val list = vocabularyQuizHistoryService.listByUser(currentAuth.userId)
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    override fun postVocabularyQuizHistories(
        vocabularyQuizHistoryBase: VocabularyQuizHistoryBase?,
    ): ResponseEntity<VocabularyQuizHistoryId> {
        requireNotNull(vocabularyQuizHistoryBase) { "Request body is required." }
        val quizHistoryId = vocabularyQuizHistoryService.create(vocabularyQuizHistoryBase.toModel())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(VocabularyQuizHistoryId(UUID.fromString(quizHistoryId)))
    }

    @Suppress("ReturnCount")
    override fun deleteVocabularyQuizHistoriesById(quizHistoryId: UUID): ResponseEntity<Unit> {
        vocabularyQuizHistoryService.getByQuizHistoryIdForUser(quizHistoryId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        vocabularyQuizHistoryService.delete(currentAuth.userId, quizHistoryId.toString())
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
                    order = tag.order,
                )
            }.toSet(),
            createdDateTime = OffsetDateTime.parse(this.createdDateTime),
            updatedDateTime = OffsetDateTime.parse(this.updatedDateTime),
        )

    private fun VocabularyTagModel.toApi(): VocabularyTag =
        VocabularyTag(
            vocabularyTagId = UUID.fromString(this.vocabularyTagId),
            vocabularyTag = this.vocabularyTag,
            order = this.order,
        )

    private fun VocabularyRequest.toModel(): VocabularyModel =
        VocabularyModel(
            vocabularyId = "",
            userId = currentAuth.userId,
            name = this.name,
            description = this.description,
            tagIds = this.tagIds?.map { it.toString() } ?: emptyList(),
        )

    private fun VocabularyRequest.toModel(vocabularyId: String): VocabularyModel =
        VocabularyModel(
            vocabularyId = vocabularyId,
            userId = currentAuth.userId,
            name = this.name,
            description = this.description,
            tagIds = this.tagIds?.map { it.toString() } ?: emptyList(),
        )

    private fun VocabularyTagBase.toModel(): VocabularyTagModel =
        VocabularyTagModel(
            vocabularyTagId = "",
            userId = currentAuth.userId,
            vocabularyTag = this.vocabularyTag,
            order = this.order,
        )

    private fun VocabularyTagBase.toModel(vocabularyTagId: String): VocabularyTagModel =
        VocabularyTagModel(
            vocabularyTagId = vocabularyTagId,
            userId = currentAuth.userId,
            vocabularyTag = this.vocabularyTag,
            order = this.order,
        )

    private fun VocabularyQuizHistoryModel.toApi(): VocabularyQuizHistory =
        VocabularyQuizHistory(
            quizHistoryId = UUID.fromString(this.quizHistoryId),
            answeredAt = OffsetDateTime.parse(this.answeredAt),
            tagIds = this.tagIds.map { UUID.fromString(it) },
            questionCount = this.questionCount,
            direction = VocabularyQuizHistory.Direction.valueOf(this.direction),
            correctCount = this.correctCount,
            incorrectCount = this.incorrectCount,
            answers = this.answers.map { it.toApi() },
        )

    private fun VocabularyQuizHistoryAnswerModel.toApi(): VocabularyQuizHistoryAnswer =
        VocabularyQuizHistoryAnswer(
            vocabularyId = UUID.fromString(this.vocabularyId),
            result = VocabularyQuizHistoryAnswer.Result.valueOf(this.result),
        )

    private fun VocabularyQuizHistoryBase.toModel(): VocabularyQuizHistoryModel =
        VocabularyQuizHistoryModel(
            quizHistoryId = "",
            userId = currentAuth.userId,
            answeredAt = "",
            tagIds = this.tagIds?.map { it.toString() } ?: emptyList(),
            questionCount = this.questionCount,
            direction = this.direction.value,
            correctCount = 0,
            incorrectCount = 0,
            answers = this.answers.map { it.toModel() },
        )

    private fun VocabularyQuizHistoryAnswer.toModel(): VocabularyQuizHistoryAnswerModel =
        VocabularyQuizHistoryAnswerModel(
            vocabularyId = this.vocabularyId.toString(),
            result = this.result.value,
        )
}
