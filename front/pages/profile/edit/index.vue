<template>
  <div>
    <h2>
      <Icon name="tabler:edit" class="adjust-icon" />
      <span class="font-cursive font-bold ml-2">Edit Profile</span>
    </h2>

    <Form v-slot="{ meta, handleSubmit }">
      <Panel class="w-2/3 flex-col items-center" centered>
        <Field
          v-slot="{ field, errorMessage }"
          name="userName"
          :rules="validationRules.userName"
          :value="user?.userName"
        >
          <TextBox
            label="Name"
            v-bind="field"
            :error-message="errorMessage"
            required
            wrapper-class="w-full"
            label-class="w-20 ml-4 font-cursive"
            input-wrapper-class="w-2/3"
            @blur:event="field.onBlur"
          />
        </Field>
        <Field
          v-slot="{ field, errorMessage }"
          name="emailAddress"
          :rules="validationRules.emailAddress"
          :value="user?.emailAddress"
        >
          <TextBox
            label="Email"
            v-bind="field"
            :error-message="errorMessage"
            required
            wrapper-class="w-full"
            label-class="w-20 ml-4 font-cursive"
            input-wrapper-class="w-2/3"
            @blur:event="field.onBlur"
          />
        </Field>
        <div class="m-4">
          <Button
            :disabled="!meta.valid"
            type="action"
            @click="handleSubmit(onSubmit)"
          >
            <Icon name="tabler:database-share" class="adjust-icon" />
            <span class="font-cursive ml-2">Execute</span>
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
import Panel from "~/components/common/Panel.vue";
import TextBox from "~/components/common/TextBox.vue";
import Button from "~/components/common/Button.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useUserStore } from "~/stores/user";
import { zodToVeeRules } from "~/util/zod-to-vee-rules";

const userStore = useUserStore();
const user = ref<User | null>({ ...userStore.user } as User);
const validationRules = {
  userName: zodToVeeRules(schemas.User.shape.userName),
  emailAddress: zodToVeeRules(schemas.User.shape.emailAddress),
};
const showDialog = ref(false);

const onSubmit: SubmissionHandler<GenericObject> = async (values) => {
  user.value = {
    ...user.value,
    ...(values as User),
  };
  await userStore.putUser(user.value as User);
  showDialog.value = true;
};
const onCloseDialog = (): void => {
  console.log("Dialog closed");
  showDialog.value = false;
  navigateTo("/");
};
</script>
