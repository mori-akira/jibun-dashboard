<template>
  <Form v-slot="{ meta, handleSubmit }">
    <div class="flex justify-end">
      <IconButton
        type="cancel"
        icon-class="w-6 h-6"
        @click:button="onCloseModal"
      />
    </div>
    <template v-for="def in editFieldDefs" :key="`filed-${def.key}`">
      <Field
        v-slot="{ field, errorMessage }"
        :name="def.key"
        :rules="validationRules[def.key]"
        :value="targetQualification?.[def.key] ?? ''"
      >
        <TextBox
          v-if="def.type === 'textbox'"
          :label="def.label"
          v-bind="field"
          :error-message="errorMessage"
          :required="def.required"
          wrapper-class="mt-4 w-full justify-center"
          label-class="w-40 ml-4 font-cursive"
          input-wrapper-class="w-1/2"
          @blur:event="field.onBlur"
          @input:value="() => commonStore.setHasUnsavedChange(true)"
        />
        <SelectBox
          v-if="def.type === 'select'"
          :label="def.label"
          :value="field.value"
          :options="def?.options?.map((e) => ({ label: e, value: e })) ?? []"
          :required="def.required"
          wrapper-class="mt-4 w-full justify-center"
          label-class="w-40 ml-4 font-cursive"
          select-wrapper-class="w-1/2"
          select-class="text-center"
          @change:value="
            (value) => {
              commonStore.setHasUnsavedChange(true);
              field?.['onUpdate:modelValue']?.(value);
            }
          "
        />
        <DatePicker
          v-if="def.type === 'datepicker'"
          :label="def.label"
          :date="field.value"
          :required="def.required"
          text-input
          wrapper-class="mt-4 w-full justify-center"
          label-class="w-40 ml-4 font-cursive"
          pickers-wrapper-class="w-1/2 px-12"
          @change:date="
            (value) => {
              commonStore.setHasUnsavedChange(true);
              field?.['onUpdate:modelValue']?.(value);
            }
          "
        />
        <div
          v-if="def.type === 'file'"
          class="mt-4 w-full flex justify-center items-center"
        >
          <Label
            :label="def.label"
            :required="def.required"
            label-class="w-40 ml-8 font-cursive"
          />
          <div class="w-1/2 ml-4">
            <FileUploader
              wrapper-class="h-24 flex-col"
              @upload="
                (file) => {
                  certificationPdfFile = file;
                  commonStore.setHasUnsavedChange(true);
                }
              "
            >
              <template v-if="certificationPdfFile">
                <div class="w-full flex justify-center items-center">
                  <Icon
                    name="tabler:file-type-pdf"
                    class="adjust-icon-2 text-red-500"
                  />
                  <span class="ml-2 font-cursive truncate max-w-48">{{
                    certificationPdfFile.name
                  }}</span>
                </div>
                <div class="w-full flex justify-center mt-2 text-gray-400">
                  <Icon name="tabler:hand-click" class="adjust-icon-2" />
                  <span class="ml-2 text-sm">Click To Change File</span>
                </div>
              </template>
              <template v-else>
                <div class="w-full flex justify-center">
                  <Icon name="tabler:drag-drop" class="adjust-icon-2" />
                  <span class="ml-2">Drag And Drop File Here</span>
                </div>
                <div class="w-full flex justify-center mt-2">
                  <Icon name="tabler:hand-click" class="adjust-icon-2" />
                  <span class="ml-2">Or Click Here To Select One</span>
                </div>
              </template>
            </FileUploader>
          </div>
        </div>
      </Field>
    </template>
    <div class="m-4">
      <Button
        :disabled="!meta.valid"
        type="action"
        wrapper-class="flex justify-center mt-8"
        @click:button="handleSubmit(onSubmit)"
      >
        <Icon name="tabler:database-share" class="adjust-icon-4" />
        <span class="ml-2">Execute</span>
      </Button>
    </div>
  </Form>
  <Dialog
    :show-dialog="showWarningDialog"
    type="warning"
    :message="warningDialogMessage"
    button-type="ok"
    @click:ok="onWarningOk"
    @close="onWarningOk"
  />
</template>

<script lang="ts" setup>
import axios from "axios";
import type { GenericObject, SubmissionHandler } from "vee-validate";
import { Form, Field } from "vee-validate";

import type { Qualification, QualificationBase } from "~/generated/api/client";
import { schemas } from "~/generated/api/client/schemas";
import Button from "~/components/common/Button.vue";
import TextBox from "~/components/common/TextBox.vue";
import SelectBox from "~/components/common/SelectBox.vue";
import DatePicker from "~/components/common/DatePicker.vue";
import IconButton from "~/components/common/IconButton.vue";
import FileUploader from "~/components/common/FileUploader.vue";
import Label from "~/components/common/Label.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useCommonStore } from "~/stores/common";
import { useFileCheck } from "~/composables/common/useFileCheck";
import { useApiClient } from "~/composables/common/useApiClient";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";
import { withErrorHandling } from "~/utils/api-call";

defineProps<{
  targetQualification: Qualification | undefined;
}>();
const emit = defineEmits<{
  (e: "closeModal"): void;
  (e: "submit", values: QualificationBase): void;
}>();

const { getFileApi } = useApiClient();
const fileApi = getFileApi();
const commonStore = useCommonStore();
const { checkFile, showWarningDialog, warningDialogMessage, onWarningOk } =
  useFileCheck({ maxSizeMB: 5 });

const certificationPdfFile = ref<File | null>(null);

const onSubmit: SubmissionHandler<GenericObject> = async (value) => {
  const valueTyped = value as QualificationBase;
  if (certificationPdfFile.value) {
    if (!(await checkFile(certificationPdfFile.value))) {
      return;
    }
    const result = await withErrorHandling(async () => {
      const res = await fileApi.getUploadUrl();
      const { fileId, uploadUrl } = res.data;
      if (!uploadUrl) {
        throw new TypeError(`uploadUrl is invalid: ${uploadUrl}`);
      }
      await axios.put(uploadUrl, certificationPdfFile.value, {
        headers: { "Content-Type": "application/pdf" },
      });
      valueTyped.certificationAssetId = fileId;
    }, commonStore);
    if (!result) return;
  }
  emit("submit", valueTyped);
};
const onCloseModal = () => {
  emit("closeModal");
};

const validationRules: {
  [K in keyof QualificationBase]?: GenericValidateFunction[];
} = {
  qualificationName: zodToVeeRules(
    schemas.QualificationBase.shape.qualificationName,
  ),
  abbreviation: zodToVeeRules(schemas.QualificationBase.shape.abbreviation),
  version: zodToVeeRules(schemas.QualificationBase.shape.version),
  status: zodToVeeRules(schemas.QualificationBase.shape.status),
  rank: zodToVeeRules(schemas.QualificationBase.shape.rank),
  organization: zodToVeeRules(schemas.QualificationBase.shape.organization),
  acquiredDate: zodToVeeRules(schemas.QualificationBase.shape.acquiredDate),
  expirationDate: zodToVeeRules(schemas.QualificationBase.shape.expirationDate),
  officialUrl: zodToVeeRules(schemas.QualificationBase.shape.officialUrl),
  certificationUrl: zodToVeeRules(
    schemas.QualificationBase.shape.certificationUrl,
  ),
  badgeUrl: zodToVeeRules(schemas.QualificationBase.shape.badgeUrl),
};
const editFieldDefs: {
  key: keyof QualificationBase;
  label: string;
  type: "textbox" | "select" | "datepicker" | "file";
  options?: string[];
  required: boolean;
}[] = [
  {
    key: "qualificationName",
    label: "Qualification Name",
    type: "textbox",
    required: true,
  },
  {
    key: "abbreviation",
    label: "Abbreviation",
    type: "textbox",
    required: false,
  },
  { key: "version", label: "Version", type: "textbox", required: false },
  {
    key: "status",
    label: "Status",
    type: "select",
    options: ["dream", "planning", "acquired"],
    required: true,
  },
  {
    key: "rank",
    label: "Rank",
    type: "select",
    options: ["A", "B", "C", "D"],
    required: true,
  },
  {
    key: "organization",
    label: "Organization",
    type: "textbox",
    required: true,
  },
  {
    key: "acquiredDate",
    label: "Acquired Date",
    type: "datepicker",
    required: false,
  },
  {
    key: "expirationDate",
    label: "Expiration Date",
    type: "datepicker",
    required: false,
  },
  {
    key: "officialUrl",
    label: "Official Url",
    type: "textbox",
    required: true,
  },
  {
    key: "certificationUrl",
    label: "Certification Url",
    type: "textbox",
    required: false,
  },
  { key: "badgeUrl", label: "Badge Url", type: "textbox", required: false },
  {
    key: "certificationAssetId",
    label: "Certification Pdf",
    type: "file",
    required: false,
  },
];
</script>
