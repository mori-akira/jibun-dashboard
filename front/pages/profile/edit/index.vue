<template>
  <div>
    <Breadcrumb :items="[{ text: 'Edit Profile', iconName: 'tabler:edit' }]" />

    <Form v-slot="{ meta, handleSubmit }">
      <Panel class="w-2/3 flex-col items-center" centered>
        <Field
          v-slot="{ field, errorMessage }"
          name="userName"
          :rules="validationRules.userName"
          :value="userStore.user?.userName"
        >
          <TextBox
            label="Name"
            v-bind="field"
            :error-message="errorMessage"
            required
            wrapper-class="m-4 w-full justify-center"
            label-class="w-20 ml-4 font-cursive"
            input-wrapper-class="w-2/3"
            @blur:event="field.onBlur"
            @input:value="() => commonStore.setHasUnsavedChange(true)"
          />
        </Field>
        <Field
          v-slot="{ field, errorMessage }"
          name="emailAddress"
          :rules="validationRules.emailAddress"
          :value="userStore.user?.emailAddress"
        >
          <TextBox
            label="Email"
            v-bind="field"
            :error-message="errorMessage"
            required
            wrapper-class="m-4 w-full justify-center"
            label-class="w-20 ml-4 font-cursive"
            input-wrapper-class="w-2/3"
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

import type { User } from "~/api/client/api";
import { schemas } from "~/api/client/schemas";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import TextBox from "~/components/common/TextBox.vue";
import Button from "~/components/common/Button.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

const commonStore = useCommonStore();
const userStore = useUserStore();
const validationRules = {
  userName: zodToVeeRules(schemas.User.shape.userName),
  emailAddress: zodToVeeRules(schemas.User.shape.emailAddress),
};
const showDialog = ref(false);

const onSubmit: SubmissionHandler<GenericObject> = async (values) => {
  const id = commonStore.addLoadingQueue();
  try {
    await userStore.putUser({
      ...userStore.user,
      ...(values as User),
    });
    showDialog.value = true;
    commonStore.setHasUnsavedChange(false);
  } catch (error) {
    commonStore.addErrorMessage(getErrorMessage(error));
  } finally {
    commonStore.deleteLoadingQueue(id);
  }
};
const onCloseDialog = (): void => {
  showDialog.value = false;
  navigateTo("/");
};
</script>
