<template>
  <div>
    <Breadcrumb :items="[{ text: 'Setting', iconName: 'tabler:settings' }]" />

    <Form v-slot="{ meta, handleSubmit }">
      <Panel class="w-2/3 flex-col items-center" centered>
        <Accordion
          title="Salary"
          title-class="font-cursive"
          wrapper-class="m-4"
        >
          <Field
            :key="`financialYearStartMonth-${setting?.salary?.financialYearStartMonth}`"
            v-slot="{ field, errorMessage }"
            name="salary.financialYearStartMonth"
            :rules="validationRules.salary.financialYearStartMonth"
            :value="setting?.salary?.financialYearStartMonth"
          >
            <TextBox
              label="Financial Year Start Month"
              v-bind="field"
              :error-message="errorMessage"
              required
              type="number"
              wrapper-class="m-4 w-full justify-center"
              label-class="w-56 ml-4 font-cursive"
              input-wrapper-class="w-48"
              input-class="text-center"
              @blur:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
          <Field
            :key="`transitionItemCount-${setting?.salary?.transitionItemCount}`"
            v-slot="{ field, errorMessage }"
            name="salary.transitionItemCount"
            :rules="validationRules.salary.transitionItemCount"
            :value="setting?.salary?.transitionItemCount"
          >
            <TextBox
              label="Transition Item Count"
              v-bind="field"
              :error-message="errorMessage"
              required
              type="number"
              wrapper-class="m-4 w-full justify-center"
              label-class="w-56 ml-4 font-cursive"
              input-wrapper-class="w-48"
              input-class="text-center"
              @blur:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
          <Field
            :key="`compareDataColor1-${setting?.salary?.compareDataColors?.[0]}`"
            v-slot="{ field, errorMessage }"
            name="salary.compareDataColor1"
            :rules="validationRules.salary.compareDataColor1"
            :value="setting?.salary?.compareDataColors?.[0]"
          >
            <ColorPicker
              label="Compare Data Color 1"
              v-bind="field"
              format="hex"
              :error-message="errorMessage"
              required
              wrapper-class="m-4 w-full justify-center"
              label-class="w-56 ml-4 font-cursive"
              input-wrapper-class="w-48"
              input-class="text-center"
              @input:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
          <Field
            :key="`compareDataColor2-${setting?.salary?.compareDataColors?.[1]}`"
            v-slot="{ field, errorMessage }"
            name="salary.compareDataColor2"
            :rules="validationRules.salary.compareDataColor2"
            :value="setting?.salary?.compareDataColors?.[1]"
          >
            <ColorPicker
              label="Compare Data Color 2"
              v-bind="field"
              format="hex"
              :error-message="errorMessage"
              required
              wrapper-class="m-4 w-full justify-center"
              label-class="w-56 ml-4 font-cursive"
              input-wrapper-class="w-48"
              input-class="text-center"
              @input:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
          <Field
            :key="`compareDataColor3-${setting?.salary?.compareDataColors?.[2]}`"
            v-slot="{ field, errorMessage }"
            name="salary.compareDataColor3"
            :rules="validationRules.salary.compareDataColor3"
            :value="setting?.salary?.compareDataColors?.[2]"
          >
            <ColorPicker
              label="Compare Data Color 3"
              v-bind="field"
              format="hex"
              :error-message="errorMessage"
              required
              wrapper-class="m-4 w-full justify-center"
              label-class="w-56 ml-4 font-cursive"
              input-wrapper-class="w-48"
              input-class="text-center"
              @input:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
        </Accordion>

        <Accordion
          title="Qualification"
          title-class="font-cursive"
          wrapper-class="m-4"
        >
          <Field
            :key="`rankAColor-${setting?.qualification?.rankAColor}`"
            v-slot="{ field, errorMessage }"
            name="qualification.rankAColor"
            :rules="validationRules.qualification.rankAColor"
            :value="setting?.qualification?.rankAColor"
          >
            <ColorPicker
              label="Rank A Color"
              v-bind="field"
              format="hex"
              :error-message="errorMessage"
              required
              wrapper-class="m-4 w-full justify-center"
              label-class="w-56 ml-4 font-cursive"
              input-wrapper-class="w-48"
              input-class="text-center"
              @input:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
          <Field
            :key="`rankBColor-${setting?.qualification?.rankBColor}`"
            v-slot="{ field, errorMessage }"
            name="qualification.rankBColor"
            :rules="validationRules.qualification.rankBColor"
            :value="setting?.qualification?.rankBColor"
          >
            <ColorPicker
              label="Rank B Color"
              v-bind="field"
              format="hex"
              :error-message="errorMessage"
              required
              wrapper-class="m-4 w-full justify-center"
              label-class="w-56 ml-4 font-cursive"
              input-wrapper-class="w-48"
              input-class="text-center"
              @input:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
          <Field
            :key="`rankCColor-${setting?.qualification?.rankCColor}`"
            v-slot="{ field, errorMessage }"
            name="qualification.rankCColor"
            :rules="validationRules.qualification.rankCColor"
            :value="setting?.qualification?.rankCColor"
          >
            <ColorPicker
              label="Rank C Color"
              v-bind="field"
              format="hex"
              :error-message="errorMessage"
              required
              wrapper-class="m-4 w-full justify-center"
              label-class="w-56 ml-4 font-cursive"
              input-wrapper-class="w-48"
              input-class="text-center"
              @input:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
          <Field
            :key="`rankDColor-${setting?.qualification?.rankDColor}`"
            v-slot="{ field, errorMessage }"
            name="qualification.rankDColor"
            :rules="validationRules.qualification.rankDColor"
            :value="setting?.qualification?.rankDColor"
          >
            <ColorPicker
              label="Rank D Color"
              v-bind="field"
              format="hex"
              :error-message="errorMessage"
              required
              wrapper-class="m-4 w-full justify-center"
              label-class="w-56 ml-4 font-cursive"
              input-wrapper-class="w-48"
              input-class="text-center"
              @input:event="field.onBlur"
              @input:value="() => commonStore.setHasUnsavedChange(true)"
            />
          </Field>
        </Accordion>

        <div class="m-4">
          <Button
            :disabled="!meta.valid"
            type="action"
            wrapper-class="flex justify-center mt-4"
            @click="handleSubmit(onSubmit)"
          >
            <Icon name="tabler:database-share" class="adjust-icon-4" />
            <span class="ml-2">Execute</span>
          </Button>
        </div>
      </Panel>
    </Form>

    <Dialog
      :show-dialog="showDialog"
      type="info"
      message="Process Completed Successfully"
      button-type="ok"
      @click:ok="onCloseDialog"
      @close="onCloseDialog"
    />
  </div>
</template>

<script setup lang="ts">
import type { GenericObject, SubmissionHandler } from "vee-validate";
import { Form, Field } from "vee-validate";

import type { Setting } from "~/api/client/api";
import { schemas } from "~/api/client/schemas";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Accordion from "~/components/common/Accordion.vue";
import TextBox from "~/components/common/TextBox.vue";
import ColorPicker from "~/components/common/ColorPicker.vue";
import Button from "~/components/common/Button.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { withErrorHandling } from "~/utils/api-call";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

definePageMeta({
  prerender: false,
});

const commonStore = useCommonStore();
const settingStore = useSettingStore();
const setting = computed<Setting>(
  () => ({ ...settingStore.setting } as Setting)
);
const validationRules = {
  salary: {
    financialYearStartMonth: zodToVeeRules(
      schemas.Setting.shape.salary.shape.financialYearStartMonth
    ),
    transitionItemCount: zodToVeeRules(
      schemas.Setting.shape.salary.shape.transitionItemCount
    ),
    compareDataColor1: zodToVeeRules(
      schemas.Setting.shape.salary.shape.compareDataColors._def.type
    ),
    compareDataColor2: zodToVeeRules(
      schemas.Setting.shape.salary.shape.compareDataColors._def.type
    ),
    compareDataColor3: zodToVeeRules(
      schemas.Setting.shape.salary.shape.compareDataColors._def.type
    ),
  },
  qualification: {
    rankAColor: zodToVeeRules(
      schemas.Setting.shape.qualification.shape.rankAColor
    ),
    rankBColor: zodToVeeRules(
      schemas.Setting.shape.qualification.shape.rankBColor
    ),
    rankCColor: zodToVeeRules(
      schemas.Setting.shape.qualification.shape.rankCColor
    ),
    rankDColor: zodToVeeRules(
      schemas.Setting.shape.qualification.shape.rankDColor
    ),
  },
};
const showDialog = ref(false);

const onSubmit: SubmissionHandler<GenericObject> = async (values) => {
  withErrorHandling(
    () =>
      settingStore.putSetting({
        ...setting.value,
        salary: {
          ...setting.value.salary,
          financialYearStartMonth: values.salary.financialYearStartMonth,
          transitionItemCount: values.salary.transitionItemCount,
          compareDataColors: [
            values.salary.compareDataColor1,
            values.salary.compareDataColor2,
            values.salary.compareDataColor3,
          ],
        },
        qualification: {
          ...setting.value.qualification,
          rankAColor: values.qualification.rankAColor,
          rankBColor: values.qualification.rankBColor,
          rankCColor: values.qualification.rankCColor,
          rankDColor: values.qualification.rankDColor,
        },
      }),
    commonStore
  );
  showDialog.value = true;
  commonStore.setHasUnsavedChange(false);
};
const onCloseDialog = (): void => {
  showDialog.value = false;
  navigateTo("/");
};
</script>
