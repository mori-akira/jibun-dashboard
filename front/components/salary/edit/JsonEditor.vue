<template>
  <div class="flex flex-col mt-4">
    <NuxtCodeMirror
      v-model="text"
      basic-setup
      editable
      theme="light"
      :class="['code-mirror', { invalid: !isValid }]"
      :extensions="[json(), lintGutter(), linter(jsonParseLinter())]"
      :linter="jsonParseLinter()"
      @on-change="onUpdate"
    />
    <div v-if="!isValidJson" class="text-red-500 mt-2">
      {{ t("message.validation.json.invalidFormat") }}
    </div>
    <div v-if="isValidJson && !isValidSchema" class="text-red-500 mt-2">
      {{ t("message.validation.json.invalidSchema") }}
    </div>
    <div class="mt-4 flex justify-center space-x-2">
      <Button type="action" :disabled="!isValid" @click="onExecute">
        <Icon name="tabler:database-share" class="adjust-icon-4" />
        <span class="ml-2">Execute</span>
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { linter, lintGutter } from "@codemirror/lint";
import { json, jsonParseLinter } from "@codemirror/lang-json";
import { useI18n } from "vue-i18n";

import type { Overview, PayslipData, Structure } from "~/generated/api/client";
import { schemas } from "~/generated/api/client/schemas";
import Button from "~/components/common/Button.vue";
import { useCommonStore } from "~/stores/common";
import { generateRandomString } from "~/utils/rand";

const props = defineProps<{
  targetSalary: {
    id: string;
    overview: Overview;
    structure: Structure;
    payslipData: PayslipData[];
  };
}>();
const emit = defineEmits<{
  (
    event: "update:targetSalary",
    value: {
      id: string;
      overview: Overview;
      structure: Structure;
      payslipData: PayslipData[];
    }
  ): void;
  (event: "execute"): void;
}>();

const { t } = useI18n();
const commonStore = useCommonStore();

const text = ref(
  JSON.stringify(
    {
      overview: { ...props.targetSalary.overview },
      structure: { ...props.targetSalary.structure },
      payslipData: structuredClone(
        toRaw(props.targetSalary.payslipData ?? []).map((e) =>
          toRaw({ ...e, data: e.data.map((e2) => toRaw(e2)) })
        )
      ),
    },
    null,
    2
  )
);
watch(
  () => props.targetSalary,
  () => {
    text.value = JSON.stringify(
      {
        overview: { ...props.targetSalary.overview },
        structure: { ...props.targetSalary.structure },
        payslipData: structuredClone(
          toRaw(props.targetSalary.payslipData ?? []).map((e) =>
            toRaw({ ...e, data: e.data.map((e2) => toRaw(e2)) })
          )
        ),
      },
      null,
      2
    );
  },
  { deep: true }
);

const validateJson = () => {
  try {
    JSON.parse(text.value);
    return true;
  } catch {
    return false;
  }
};
const isValidJson = computed(() => validateJson());
const validateSchema = () => {
  try {
    const parsed = JSON.parse(text.value);
    const checkOverview = schemas.Salary.shape.overview.safeParse(
      parsed.overview
    );
    const checkStructure = schemas.Salary.shape.structure.safeParse(
      parsed.structure
    );
    const checkPayslipData = schemas.Salary.shape.payslipData.safeParse(
      parsed.payslipData
    );
    return (
      checkOverview.success &&
      checkStructure.success &&
      checkPayslipData.success
    );
  } catch {
    return false;
  }
};
const isValidSchema = computed(() => validateSchema());
const validate = () => validateJson() && validateSchema();
const isValid = computed(() => isValidJson.value && isValidSchema.value);

const updateTargetSalary = (newVal: string) => {
  const parsed = JSON.parse(newVal);
  emit("update:targetSalary", {
    id: generateRandomString(),
    overview: parsed.overview,
    structure: parsed.structure,
    payslipData: parsed.payslipData,
  });
  commonStore.setHasUnsavedChange(true);
};
const onUpdate = (newVal: string) => {
  if (validate()) {
    updateTargetSalary(newVal);
  }
};
const onExecute = () => {
  emit("execute");
};
</script>

<style lang="css" scoped>
.code-mirror {
  flex: 1;
  max-height: calc(100vh - 27rem);
  box-shadow: 1px 1px 3px #000;
  overflow-y: auto;
}

.code-mirror.invalid {
  box-shadow: 1px 1px 3px #f00;
}
</style>
