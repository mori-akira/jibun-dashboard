<template>
  <div>
    <h2>
      <Icon name="tabler:settings" class="adjust-icon" />
      <span class="font-cursive font-bold ml-2">Setting</span>
    </h2>

    <Form v-slot="{ meta, handleSubmit }">
      <Panel class="w-2/3 flex-col items-center" centered>
        <Accordion
          title="Salary"
          title-class="font-cursive"
          wrapper-class="m-4"
        >
          <Field
            v-slot="{ field, errorMessage }"
            name="userName"
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
            />
          </Field>
        </Accordion>

        <div class="m-4">
          <Button
            :disabled="!meta.valid"
            type="action"
            wrapper-class="flex justify-center mt-8"
            @click="handleSubmit(onSubmit)"
          >
            <Icon name="tabler:database-share" class="adjust-icon" />
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
import Panel from "~/components/common/Panel.vue";
import Accordion from "~/components/common/Accordion.vue";
import TextBox from "~/components/common/TextBox.vue";
import Button from "~/components/common/Button.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useSettingStore } from "~/stores/setting";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

definePageMeta({
  prerender: false,
});

const settingStore = useSettingStore();
const setting = ref<Setting>({ ...settingStore.setting } as Setting);
const validationRules = {
  salary: {
    financialYearStartMonth: zodToVeeRules(
      schemas.Setting.shape.salary.shape.financialYearStartMonth
    ),
  },
};
const showDialog = ref(false);

const onSubmit: SubmissionHandler<GenericObject> = async (values) => {
  setting.value = {
    ...setting.value,
    ...(values as Setting),
  };
  await settingStore.putSetting(setting.value as Setting);
  showDialog.value = true;
};
const onCloseDialog = (): void => {
  showDialog.value = false;
  navigateTo("/");
};
</script>
