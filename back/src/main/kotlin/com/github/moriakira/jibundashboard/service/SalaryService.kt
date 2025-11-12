package com.github.moriakira.jibundashboard.service

import com.github.moriakira.jibundashboard.prompts.SalaryPayslipOcrPromptsProvider
import com.github.moriakira.jibundashboard.repository.SalaryItem
import com.github.moriakira.jibundashboard.repository.SalaryRepository
import com.openai.client.OpenAIClient
import com.openai.models.ChatModel
import com.openai.models.files.FileCreateParams
import com.openai.models.files.FileObject
import com.openai.models.files.FilePurpose
import com.openai.models.responses.ResponseCreateParams
import com.openai.models.responses.ResponseInputFile
import com.openai.models.responses.ResponseInputItem
import com.openai.models.responses.ResponseInputItem.Message
import com.openai.models.responses.StructuredResponse
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
@Suppress("LongParameterList", "TooManyFunctions")
class SalaryService(
    private val salaryRepository: SalaryRepository,
    private val salaryPayslipOcrPromptsProvider: SalaryPayslipOcrPromptsProvider,
    private val s3Client: S3Client,
    private val openAIClient: OpenAIClient,
    private val fileUploadService: FileUploadService,
    @param:Value("\${app.s3.uploads-bucket.name}") private val uploadsBucketName: String,
    @param:Value("\${app.openai.model}") private val openAIModel: String,
    @param:Lazy private val self: SalaryService?,
) {

    fun get(userId: String, targetDate: String): SalaryModel? = salaryRepository.get(userId, targetDate)?.toDomain()

    fun listAll(userId: String): List<SalaryModel> = salaryRepository.findByUser(userId).map { it.toDomain() }

    fun listByExactDate(userId: String, date: String): List<SalaryModel> =
        salaryRepository.findByUserAndDate(userId, date).map { it.toDomain() }

    fun listByDateRange(userId: String, from: String?, to: String?): List<SalaryModel> =
        salaryRepository.findByUserAndDateRange(userId, from, to).map { it.toDomain() }

    fun getBySalaryId(salaryId: String): SalaryModel? = salaryRepository.getBySalaryId(salaryId)?.toDomain()

    fun deleteBySalaryId(userId: String, targetDate: String) {
        salaryRepository.delete(userId, targetDate)
    }

    fun put(model: SalaryModel): String {
        salaryRepository.put(model.toItem())
        return model.salaryId
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException", "NestedBlockDepth")
    fun runOcr(salaryId: String, userId: String, fileId: UUID, date: String): SalaryModel {
        val s3Key = fileUploadService.uploadKey(userId, fileId)
        val tmpDir = Files.createTempDirectory("payslip-")
        val tmpPdf = tmpDir.resolve("payslip-$fileId.pdf")
        try {
            try {
                // S3からダウンロード
                s3Client.getObject(
                    GetObjectRequest.builder().bucket(uploadsBucketName).key(s3Key).build(),
                    ResponseTransformer.toFile(tmpPdf),
                )
            } catch (_: Exception) {
                try {
                    Files.deleteIfExists(tmpPdf)
                } catch (_: Exception) {
                }
                error("Failed to download from S3: s3://$uploadsBucketName/$s3Key")
            }

            // OCR実行
            val ocrResult = self?.queryOpenAI(tmpPdf) ?: error("Self reference of SalaryService is not initialized.")

            return SalaryModel(
                salaryId = salaryId,
                userId = userId,
                targetDate = date,
                overview = ocrResult.overview,
                structure = ocrResult.structure,
                payslipData = ocrResult.payslipData,
            )
        } finally {
            try {
                Files.walk(tmpDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach { path ->
                        try {
                            Files.deleteIfExists(path)
                        } catch (_: Exception) {
                        }
                    }
            } catch (_: Exception) {
            }
        }
    }

    @Retry(name = "openaiClient")
    fun queryOpenAI(attachment: Path): OcrResult {
        // ファイルアップロード
        val uploaded: FileObject = openAIClient.files().create(
            FileCreateParams.builder().file(attachment).purpose(FilePurpose.ASSISTANTS).build(),
        )

        val systemPrompt = salaryPayslipOcrPromptsProvider.systemPrompt()
        val userPrompt = salaryPayslipOcrPromptsProvider.userPrompt()
        val params = ResponseCreateParams.builder()
            .model(ChatModel.of(openAIModel))
            .text(OcrResult::class.java)
            .inputOfResponse(
                listOf(
                    // システムプロンプト
                    ResponseInputItem.ofMessage(
                        Message.builder()
                            .role(Message.Role.SYSTEM)
                            .addInputTextContent(systemPrompt)
                            .build(),
                    ),
                    // ユーザプロンプト + 添付ファイル
                    ResponseInputItem.ofMessage(
                        Message.builder()
                            .role(Message.Role.USER)
                            .addInputTextContent(userPrompt)
                            .addContent(
                                ResponseInputFile.builder().fileId(uploaded.id()).build(),
                            )
                            .build(),
                    ),
                ),
            ).build()

        val response: StructuredResponse<OcrResult> = openAIClient.responses().create(params)
        val list: List<OcrResult?> = response.output()
            .map {
                it.message()
                    .getOrNull()
                    ?.content()
                    ?.firstOrNull()
                    ?.outputText()
                    ?.getOrNull()
            }
        return list.firstOrNull { it != null } ?: error("No valid response from OpenAI")
    }

    @Suppress("CyclomaticComplexMethod")
    private fun SalaryItem.toDomain(): SalaryModel = SalaryModel(
        salaryId = this.salaryId!!,
        userId = this.userId!!,
        targetDate = this.targetDate!!,
        overview = this.overview!!.let {
            OverviewModel(
                grossIncome = it.grossIncome ?: 0,
                netIncome = it.netIncome ?: 0,
                operatingTime = it.operatingTime ?: 0.0,
                overtime = it.overtime ?: 0.0,
                bonus = it.bonus ?: 0,
                bonusTakeHome = it.bonusTakeHome ?: 0,
            )
        },
        structure = this.structure!!.let {
            StructureModel(
                basicSalary = it.basicSalary ?: 0,
                overtimePay = it.overtimePay ?: 0,
                housingAllowance = it.housingAllowance ?: 0,
                positionAllowance = it.positionAllowance ?: 0,
                other = it.other ?: 0,
            )
        },
        payslipData = this.payslipData?.map { category ->
            PayslipCategoryModel(
                key = category.key!!,
                data = category.data?.map { data ->
                    PayslipEntryModel(
                        key = data.key!!,
                        data = data.data ?: 0.0,
                    )
                } ?: emptyList(),
            )
        } ?: emptyList(),
    )

    private fun SalaryModel.toItem(): SalaryItem = SalaryItem().also { item ->
        item.salaryId = this.salaryId
        item.userId = this.userId
        item.targetDate = this.targetDate
        item.overview = this.overview.let {
            SalaryItem.Overview().apply {
                grossIncome = it.grossIncome
                netIncome = it.netIncome
                operatingTime = it.operatingTime
                overtime = it.overtime
                bonus = it.bonus
                bonusTakeHome = it.bonusTakeHome
            }
        }
        item.structure = this.structure.let {
            SalaryItem.Structure().apply {
                basicSalary = it.basicSalary
                overtimePay = it.overtimePay
                housingAllowance = it.housingAllowance
                positionAllowance = it.positionAllowance
                other = it.other
            }
        }
        item.payslipData = this.payslipData.map { category ->
            SalaryItem.PayslipCategory().apply {
                key = category.key
                data = category.data.map { d ->
                    SalaryItem.PayslipEntry().apply {
                        key = d.key
                        data = d.data
                    }
                }
            }
        }
    }
}

data class SalaryModel(
    val salaryId: String,
    val userId: String,
    val targetDate: String,
    val overview: OverviewModel,
    val structure: StructureModel,
    val payslipData: List<PayslipCategoryModel>,
)

data class OverviewModel(
    val grossIncome: Int,
    val netIncome: Int,
    val operatingTime: Double,
    val overtime: Double,
    val bonus: Int,
    val bonusTakeHome: Int,
)

data class StructureModel(
    val basicSalary: Int,
    val overtimePay: Int,
    val housingAllowance: Int,
    val positionAllowance: Int,
    val other: Int,
)

data class PayslipCategoryModel(
    val key: String,
    val data: List<PayslipEntryModel>,
)

data class PayslipEntryModel(
    val key: String,
    val data: Double,
)

data class OcrResult(
    val overview: OverviewModel,
    val structure: StructureModel,
    val payslipData: List<PayslipCategoryModel>,
)
