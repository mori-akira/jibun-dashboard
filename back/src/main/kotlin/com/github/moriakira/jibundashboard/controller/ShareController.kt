package com.github.moriakira.jibundashboard.controller

import com.github.moriakira.jibundashboard.generated.api.ShareApi
import com.github.moriakira.jibundashboard.generated.model.Qualification
import com.github.moriakira.jibundashboard.generated.model.Salary
import com.github.moriakira.jibundashboard.generated.model.Setting
import com.github.moriakira.jibundashboard.generated.model.SharedLinkBase
import com.github.moriakira.jibundashboard.generated.model.Vocabulary
import com.github.moriakira.jibundashboard.generated.model.VocabularyTag
import com.github.moriakira.jibundashboard.mapper.toApi
import com.github.moriakira.jibundashboard.mapper.toApiBase
import com.github.moriakira.jibundashboard.service.QualificationService
import com.github.moriakira.jibundashboard.service.SalaryService
import com.github.moriakira.jibundashboard.service.SettingService
import com.github.moriakira.jibundashboard.service.SharedLinkService
import com.github.moriakira.jibundashboard.service.VocabularyService
import com.github.moriakira.jibundashboard.service.VocabularyTagService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class ShareController(
    private val sharedLinkService: SharedLinkService,
    private val salaryService: SalaryService,
    private val qualificationService: QualificationService,
    private val vocabularyService: VocabularyService,
    private val vocabularyTagService: VocabularyTagService,
    private val settingService: SettingService,
) : ShareApi {

    override fun getShareInfo(token: UUID): ResponseEntity<SharedLinkBase> {
        val link = sharedLinkService.validateTokenOnly(token.toString())
        return ResponseEntity.ok(link.toApiBase())
    }

    override fun getShareSetting(token: UUID): ResponseEntity<Setting> {
        val link = sharedLinkService.validateTokenOnly(token.toString())
        return ResponseEntity.ok(settingService.getOrDefault(link.userId).toApi())
    }

    override fun getShareSalaries(token: UUID): ResponseEntity<List<Salary>> {
        val link = sharedLinkService.validateAndGet(token.toString(), "salary")
        return ResponseEntity.ok(salaryService.listAll(link.userId).map { it.toApi() })
    }

    override fun getShareQualifications(token: UUID): ResponseEntity<List<Qualification>> {
        val link = sharedLinkService.validateAndGet(token.toString(), "qualification")
        val list = qualificationService.listAll(link.userId).map { it.toApi().copy(certificationAssetId = null) }
        return ResponseEntity.ok(list)
    }

    override fun getShareVocabularies(token: UUID): ResponseEntity<List<Vocabulary>> {
        val link = sharedLinkService.validateAndGet(token.toString(), "vocabulary")
        return ResponseEntity.ok(vocabularyService.listByConditions(link.userId).map { it.toApi() })
    }

    override fun getShareVocabularyTags(token: UUID): ResponseEntity<List<VocabularyTag>> {
        val link = sharedLinkService.validateAndGet(token.toString(), "vocabulary")
        return ResponseEntity.ok(vocabularyTagService.listByConditions(link.userId).map { it.toApi() })
    }
}
