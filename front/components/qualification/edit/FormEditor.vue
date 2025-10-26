<template>
  <Form v-slot="{ meta, handleSubmit }">
    <div class="flex justify-end">
      <IconButton type="cancel" icon-class="w-6 h-6" @click="onCloseModal" />
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
          @change="
            (value) => {
              commonStore.setHasUnsavedChange(true);
              field?.['onUpdate:modelValue']?.(value);
            }
          "
        />
      </Field>
    </template>
    <div class="m-4">
      <Button
        :disabled="!meta.valid"
        type="action"
        wrapper-class="flex justify-center mt-8"
        @click="handleSubmit(onSubmit)"
      >
        <Icon name="tabler:database-share" class="adjust-icon-4" />
        <span class="ml-2">Execute</span>
      </Button>
    </div>
  </Form>
</template>

<script lang="ts" setup>
import type { GenericObject, SubmissionHandler } from "vee-validate";
import { Form, Field } from "vee-validate";

import type { Qualification } from "~/generated/api/client";
import { schemas } from "~/generated/api/client/schemas";
import Button from "~/components/common/Button.vue";
import TextBox from "~/components/common/TextBox.vue";
import SelectBox from "~/components/common/SelectBox.vue";
import DatePicker from "~/components/common/DatePicker.vue";
import IconButton from "~/components/common/IconButton.vue";
import { useCommonStore } from "~/stores/common";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

defineProps<{
  targetQualification: Qualification | undefined;
}>();
const emit = defineEmits<{
  (e: "closeModal"): void;
  (e: "submit", values: Qualification): void;
}>();

const commonStore = useCommonStore();

const onSubmit: SubmissionHandler<GenericObject> = async (value) => {
  const valueTyped = value as Qualification;
  emit("submit", valueTyped);
};
const onCloseModal = () => {
  emit("closeModal");
};

const validationRules: {
  [K in keyof Qualification]?: GenericValidateFunction[];
} = {
  qualificationName: zodToVeeRules(
    schemas.Qualification.shape.qualificationName
  ),
  abbreviation: zodToVeeRules(schemas.Qualification.shape.abbreviation),
  version: zodToVeeRules(schemas.Qualification.shape.version),
  status: zodToVeeRules(schemas.Qualification.shape.status),
  rank: zodToVeeRules(schemas.Qualification.shape.rank),
  organization: zodToVeeRules(schemas.Qualification.shape.organization),
  acquiredDate: zodToVeeRules(schemas.Qualification.shape.acquiredDate),
  expirationDate: zodToVeeRules(schemas.Qualification.shape.expirationDate),
  officialUrl: zodToVeeRules(schemas.Qualification.shape.officialUrl),
  certificationUrl: zodToVeeRules(schemas.Qualification.shape.certificationUrl),
  badgeUrl: zodToVeeRules(schemas.Qualification.shape.badgeUrl),
};
const editFieldDefs: {
  key: keyof Qualification;
  label: string;
  type: "textbox" | "select" | "datepicker";
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
];
</script>
