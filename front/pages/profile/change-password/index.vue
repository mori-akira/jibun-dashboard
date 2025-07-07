<template>
  <div>
    <h2>
      <Icon name="tabler:edit" class="adjust-icon" />
      <span class="font-cursive font-bold ml-2">Change Password</span>
    </h2>

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
          />
        </Field>
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
import { navigateTo } from "nuxt/app";
import type { GenericObject, SubmissionHandler } from "vee-validate";
import { Form, Field } from "vee-validate";

import { schemas } from "~/api/client/schemas";
import Panel from "~/components/common/Panel.vue";
import TextBox from "~/components/common/TextBox.vue";
import Button from "~/components/common/Button.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useUserStore } from "~/stores/user";
import { zodToVeeRules } from "~/util/zod-to-vee-rules";
import type {
  FieldValidationMetaInfo,
  GenericValidateFunction,
} from "~/util/zod-to-vee-rules";
import { matchCharacterTypeRule } from "~/util/password";

const userStore = useUserStore();
const validationRules = {
  oldPassword: zodToVeeRules(schemas.Password.shape.oldPassword),
  newPassword: [
    ...zodToVeeRules(schemas.Password.shape.newPassword),
    ((value: string): true | string => {
      return matchCharacterTypeRule(value)
        ? true
        : "大文字・小文字・数字・記号のうち3種類以上を含めてください";
    }) as GenericValidateFunction,
  ],
  newPasswordConfirm: [
    ...zodToVeeRules(schemas.Password.shape.newPassword),
    ((value: string): true | string => {
      return matchCharacterTypeRule(value)
        ? true
        : "大文字・小文字・数字・記号のうち3種類以上を含めてください";
    }) as GenericValidateFunction,
    ((value: string, ctx: FieldValidationMetaInfo): true | string => {
      return value === ctx?.form?.newPassword
        ? true
        : "パスワードが一致しません";
    }) as GenericValidateFunction,
  ],
};
const showDialog = ref(false);

type PasswordForm = {
  oldPassword: string;
  newPassword: string;
  newPasswordConfirm: string;
};

const onSubmit: SubmissionHandler<GenericObject> = async (values) => {
  const valuesTyped = values as PasswordForm;
  await userStore.postPassword({
    newPassword: valuesTyped.newPassword,
    oldPassword: valuesTyped.oldPassword,
  });
  showDialog.value = true;
};
const onCloseDialog = (): void => {
  showDialog.value = false;
  navigateTo("/");
};
</script>
