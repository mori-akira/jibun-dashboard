package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.component.CurrentAuth
import com.github.moriakira.jibundashboard.generated.api.SalaryApi
import com.github.moriakira.jibundashboard.generated.model.PostSalaryOcrTasksRequest
import com.github.moriakira.jibundashboard.generated.model.Salary
import com.github.moriakira.jibundashboard.generated.model.SalaryBase
import com.github.moriakira.jibundashboard.generated.model.SalaryId
import com.github.moriakira.jibundashboard.generated.model.SalaryOcrTask
import com.github.moriakira.jibundashboard.generated.model.SalaryOcrTaskId
import com.github.moriakira.jibundashboard.mapper.toApi
import com.github.moriakira.jibundashboard.mapper.toModel
import com.github.moriakira.jibundashboard.service.SalaryOcrTaskService
import com.github.moriakira.jibundashboard.service.SalaryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@Suppress("TooManyFunctions")
class SalaryController(
    private val currentAuth: CurrentAuth,
    private val salaryService: SalaryService,
    private val salaryOcrTaskService: SalaryOcrTaskService,
) : SalaryApi {

    override fun getSalaries(
        targetDate: LocalDate?,
        targetDateFrom: LocalDate?,
        targetDateTo: LocalDate?,
    ): ResponseEntity<List<Salary>> {
        val list = salaryService.list(
            currentAuth.userId,
            targetDate?.toString(),
            targetDateFrom?.toString(),
            targetDateTo?.toString(),
        )
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    override fun postSalaries(salaryBase: SalaryBase?): ResponseEntity<SalaryId> {
        requireNotNull(salaryBase) { "Request body is required." }
        val salaryId = salaryService.create(salaryBase.toModel(currentAuth.userId))
        return ResponseEntity.status(HttpStatus.CREATED).body(SalaryId(salaryId = UUID.fromString(salaryId)))
    }

    @Suppress("ReturnCount")
    override fun getSalariesById(salaryId: UUID): ResponseEntity<Salary> {
        val model = salaryService.getByIdForUser(salaryId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(model.toApi())
    }

    @Suppress("ReturnCount")
    override fun putSalariesById(salaryId: UUID, salaryBase: SalaryBase?): ResponseEntity<SalaryId> {
        requireNotNull(salaryBase) { "Request body is required." }
        val current = salaryService.getByIdForUser(salaryId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        salaryService.update(current, salaryBase.toModel(currentAuth.userId, salaryId.toString()))
        return ResponseEntity.ok().body(SalaryId(salaryId))
    }

    @Suppress("ReturnCount")
    override fun deleteSalariesById(salaryId: UUID): ResponseEntity<Unit> {
        val model = salaryService.getByIdForUser(salaryId.toString(), currentAuth.userId)
            ?: return ResponseEntity.notFound().build()
        salaryService.delete(currentAuth.userId, model.targetDate)
        return ResponseEntity.noContent().build()
    }

    override fun getSalaryOcrTasks(targetDate: LocalDate): ResponseEntity<List<SalaryOcrTask>> {
        val list = salaryOcrTaskService.listByUserAndDate(
            userId = currentAuth.userId,
            targetDate = targetDate.toString(),
        )
        return ResponseEntity.ok(list.map { it.toApi() })
    }

    @Suppress("ReturnCount")
    override fun getSalaryOcrTasksById(ocrTaskId: UUID): ResponseEntity<SalaryOcrTask> {
        val model = salaryOcrTaskService.getByTaskId(ocrTaskId.toString()) ?: return ResponseEntity.notFound().build()
        if (model.userId != currentAuth.userId) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(model.toApi())
    }

    override fun postSalaryOcrTasks(
        postSalaryOcrTasksRequest: PostSalaryOcrTasksRequest?,
    ): ResponseEntity<SalaryOcrTaskId> {
        requireNotNull(postSalaryOcrTasksRequest) { "Request body is required." }
        val taskId = salaryOcrTaskService.startTask(
            userId = currentAuth.userId,
            targetDate = postSalaryOcrTasksRequest.targetDate.toString(),
            fileId = postSalaryOcrTasksRequest.fileId.toString(),
        )
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(SalaryOcrTaskId(ocrTaskId = UUID.fromString(taskId)))
    }
}
