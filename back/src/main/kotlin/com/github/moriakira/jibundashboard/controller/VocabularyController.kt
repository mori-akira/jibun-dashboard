package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.VocabularyApi
import com.github.moriakira.jibundashboard.generated.model.PostVocabulariesRequest
import com.github.moriakira.jibundashboard.generated.model.Vocabulary
import com.github.moriakira.jibundashboard.generated.model.VocabularyCheckResult
import com.github.moriakira.jibundashboard.generated.model.VocabularyCheckResultStatus
import com.github.moriakira.jibundashboard.generated.model.VocabularyId
import com.github.moriakira.jibundashboard.generated.model.VocabularyQuizHistory
import com.github.moriakira.jibundashboard.generated.model.VocabularyQuizHistoryBase
import com.github.moriakira.jibundashboard.generated.model.VocabularyQuizHistoryId
import com.github.moriakira.jibundashboard.generated.model.VocabularyTag
import com.github.moriakira.jibundashboard.generated.model.VocabularyTagBase
import com.github.moriakira.jibundashboard.generated.model.VocabularyTagId
import com.github.moriakira.jibundashboard.mapper.toApi
import com.github.moriakira.jibundashboard.mapper.toModel
import com.github.moriakira.jibundashboard.service.VocabularyCheckResultService
import com.github.moriakira.jibundashboard.service.VocabularyQuizHistoryService
import com.github.moriakira.jibundashboard.service.VocabularyService
import com.github.moriakira.jibundashboard.service.VocabularyTagService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@Suppress("TooManyFunctions")
@RestController
class VocabularyController(
    private val currentAuth: CurrentAuth,
    private val vocabularyService: VocabularyService,
    private val vocabularyTagService: VocabularyTagService,
    private val vocabularyQuizHistoryService: VocabularyQuizHistoryService,
    private val vocabularyCheckResultService: VocabularyCheckResultService,
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

    override fun postVocabularies(postVocabulariesRequest: PostVocabulariesRequest?): ResponseEntity<VocabularyId> {
        requireNotNull(postVocabulariesRequest) { "Request body is required." }
        val vocabularyId = vocabularyService.create(postVocabulariesRequest.toModel(currentAuth.userId))
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
        postVocabulariesRequest: PostVocabulariesRequest?,
    ): ResponseEntity<VocabularyId> {
        requireNotNull(postVocabulariesRequest) { "Request body is required." }
        vocabularyService.getByVocabularyIdForUser(vocabularyId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        vocabularyService.put(postVocabulariesRequest.toModel(currentAuth.userId, vocabularyId.toString()))
        return ResponseEntity.ok(VocabularyId(vocabularyId))
    }

    @Suppress("ReturnCount")
    override fun deleteVocabulariesById(vocabularyId: UUID): ResponseEntity<Unit> {
        vocabularyService.getByVocabularyIdForUser(vocabularyId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        vocabularyService.delete(currentAuth.userId, vocabularyId.toString())
        return ResponseEntity.noContent().build()
    }

    override fun getVocabularyTags(vocabularyTag: String?): ResponseEntity<List<VocabularyTag>> {
        val list =
            vocabularyTagService.listByConditions(
                userId = currentAuth.userId,
                vocabularyTag = vocabularyTag,
            )
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    override fun postVocabularyTags(vocabularyTagBase: VocabularyTagBase?): ResponseEntity<VocabularyTagId> {
        requireNotNull(vocabularyTagBase) { "Request body is required." }
        val vocabularyTagId = vocabularyTagService.create(vocabularyTagBase.toModel(currentAuth.userId))
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
        vocabularyTagService.put(vocabularyTagBase.toModel(currentAuth.userId, vocabularyTagId.toString()))
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
        val quizHistoryId = vocabularyQuizHistoryService.create(vocabularyQuizHistoryBase.toModel(currentAuth.userId))
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

    override fun getVocabularyCheckResults(
        checkedAtFrom: LocalDate?,
        checkedAtTo: LocalDate?,
        severities: List<String>?,
        statuses: List<String>?,
    ): ResponseEntity<List<VocabularyCheckResult>> {
        val list = vocabularyCheckResultService.listByUser(
            userId = currentAuth.userId,
            checkedAtFrom = checkedAtFrom?.toString(),
            checkedAtTo = checkedAtTo?.toString(),
            severities = severities,
            statuses = statuses,
        )
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    @Suppress("ReturnCount")
    override fun putVocabularyCheckResultStatusById(
        checkResultId: UUID,
        vocabularyCheckResultStatus: VocabularyCheckResultStatus?,
    ): ResponseEntity<Unit> {
        requireNotNull(vocabularyCheckResultStatus) { "Request body is required." }
        vocabularyCheckResultService.updateStatus(
            checkResultId.toString(),
            currentAuth.userId,
            vocabularyCheckResultStatus.status.value,
        ) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.noContent().build()
    }
}
