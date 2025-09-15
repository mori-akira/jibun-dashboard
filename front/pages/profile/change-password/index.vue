<template>
  <div>
    <Breadcrumb
      :items="[{ text: 'Change Password', iconName: 'tabler:edit' }]"
    />

    <Form v-slot="{ meta, handleSubmit }">
      <Panel class="w-2/3 flex-col items-center" centered>
        <Field
          v-slot="{ field, errorMessage }"
          name="oldPassword"
          :rules="validationRules.oldPassword"
        >
          <TextBox
            label="Old Password"
            v-bind="field"
            :error-message="errorMessage"
            required
            type="password"
            wrapper-class="mt-4 w-full justify-center"
            label-class="w-50 ml-4 font-cursive"
            input-wrapper-class="w-1/3"
            @blur:event="field.onBlur"
            @input:value="() => commonStore.setHasUnsavedChange(true)"
          />
        </Field>
        <Field
          v-slot="{ field, errorMessage }"
          name="newPassword"
          :rules="validationRules.newPassword"
        >
          <TextBox
            label="New Password"
            v-bind="field"
            :error-message="errorMessage"
            required
            type="password"
            wrapper-class="mt-4 w-full justify-center"
            label-class="w-50 ml-4 font-cursive"
            input-wrapper-class="w-1/3"
            @blur:event="field.onBlur"
            @input:value="() => commonStore.setHasUnsavedChange(true)"
          />
        </Field>
        <Field
          v-slot="{ field, errorMessage }"
          name="newPasswordConfirm"
          :rules="validationRules.newPasswordConfirm"
        >
          <TextBox
            label="New Password (Confirm)"
            v-bind="field"
            :error-message="errorMessage"
            required
            type="password"
            wrapper-class="mt-4 w-full justify-center"
            label-class="w-50 ml-4 font-cursive"
            input-wrapper-class="w-1/3"
            @blur:event="field.onBlur"
            @input:value="() => commonStore.setHasUnsavedChange(true)"
          />
        </Field>
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
      </Panel>
    </Form>

    <Dialog
      :show-dialog="showInfoDialog"
      type="info"
      :message="infoDialogMessage"
      button-type="ok"
      @click:ok="onInfoOk"
      @close="onInfoOk"
    />
  </div>
</template>

<script setup lang="ts">
import { navigateTo } from "nuxt/app";
import type { GenericObject, SubmissionHandler } from "vee-validate";
import { Form, Field } from "vee-validate";
import { useI18n } from "vue-i18n";

import { schemas } from "~/api/client/schemas";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import TextBox from "~/components/common/TextBox.vue";
import Button from "~/components/common/Button.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useInfoDialog } from "~/composables/common/useDialog";
import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";
import { withErrorHandling } from "~/utils/api-call";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";
import type {
  FieldValidationMetaInfo,
  GenericValidateFunction,
} from "~/utils/zod-to-vee-rules";
import { matchCharacterTypeRule } from "~/utils/password";

const { t } = useI18n();
const commonStore = useCommonStore();
const userStore = useUserStore();
const { showInfoDialog, infoDialogMessage, openInfoDialog, onInfoOk } =
  useInfoDialog();
const validationRules = {
  oldPassword: zodToVeeRules(schemas.Password.shape.oldPassword),
  newPassword: [
    ...zodToVeeRules(schemas.Password.shape.newPassword),
    ((value: string): true | string => {
      return matchCharacterTypeRule(value)
        ? true
        : t("message.validation.password.complexity");
    }) as GenericValidateFunction,
  ],
  newPasswordConfirm: [
    ...zodToVeeRules(schemas.Password.shape.newPassword),
    ((value: string): true | string => {
      return matchCharacterTypeRule(value)
        ? true
        : t("message.validation.password.complexity");
    }) as GenericValidateFunction,
    ((value: string, ctx: FieldValidationMetaInfo): true | string => {
      return value === ctx?.form?.newPassword
        ? true
        : t("message.validation.password.mismatch");
    }) as GenericValidateFunction,
  ],
};

type PasswordForm = {
  oldPassword: string;
  newPassword: string;
  newPasswordConfirm: string;
};

const onSubmit: SubmissionHandler<GenericObject> = async (values) => {
  const valuesTyped = values as PasswordForm;
  const result = await withErrorHandling(async () => {
    await userStore.postPassword({
      newPassword: valuesTyped.newPassword,
      oldPassword: valuesTyped.oldPassword,
    });
  }, commonStore);
  if (result) {
    commonStore.setHasUnsavedChange(false);
    await openInfoDialog(t("message.info.completeSuccessfully"));
    navigateTo("/");
  }
};
</script>
