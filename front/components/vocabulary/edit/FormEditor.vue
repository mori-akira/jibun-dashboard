<template>
  <Form v-slot="{ meta, handleSubmit }">
    <div class="flex justify-end">
      <IconButton
        type="cancel"
        icon-class="w-6 h-6"
        @click:button="onCloseModal"
      />
    </div>

    <Field
      v-slot="{ field, errorMessage }"
      name="name"
      :rules="validationRules.name"
      :value="targetVocabulary?.name ?? ''"
    >
      <TextBox
        label="Name"
        v-bind="field"
        :error-message="errorMessage"
        required
        wrapper-class="mt-4 w-full justify-center"
        label-class="w-40 ml-4 font-cursive"
        input-wrapper-class="w-1/2"
        @blur:event="field.onBlur"
        @input:value="() => commonStore.setHasUnsavedChange(true)"
      />
    </Field>

    <Field
      v-slot="{ field, errorMessage }"
      name="description"
      :rules="validationRules.description"
      :value="targetVocabulary?.description ?? ''"
    >
      <TextArea
        label="Description"
        v-bind="field"
        :error-message="errorMessage"
        :rows="4"
        wrapper-class="mt-4 w-full justify-center items-start"
        label-class="w-40 ml-4 font-cursive mt-1"
        input-wrapper-class="w-1/2"
        @blur:event="field.onBlur"
        @input:value="() => commonStore.setHasUnsavedChange(true)"
      />
    </Field>

    <div class="mt-4 w-full flex justify-center items-start">
      <Label label="Tags" label-class="w-40 ml-4 font-cursive mt-1" />
      <div class="w-1/2 flex flex-wrap gap-2">
        <div
          v-for="tag in vocabularyTags"
          :key="tag.vocabularyTagId"
          :class="[
            'py-[0.2rem] px-4 text-[0.8rem] rounded-lg cursor-pointer',
            selectedTagIds.includes(tag.vocabularyTagId ?? '')
              ? 'bg-[#bb88ff] text-white hover:bg-[#aa77ee]'
              : 'bg-[#888] text-white hover:bg-[#777]',
          ]"
          @click="onToggleTag(tag.vocabularyTagId ?? '')"
        >
          {{ tag.vocabularyTag }}
        </div>
        <IconButton
          type="plus"
          icon-class="w-3 h-3"
          wrapper-class="self-stretch flex items-center px-2 bg-gray-200 rounded-lg hover:bg-gray-300 h-6"
          @click:button="openAddTagDialog"
        />
      </div>
    </div>

    <Dialog
      :show-dialog="showInputDialog"
      type="input"
      message="New tag name"
      button-type="okCancel"
      @click:ok="onAddTagOk"
      @click:cancel="onInputCancel"
      @close="onInputCancel"
    />

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
</template>

<script lang="ts" setup>
import type { GenericObject, SubmissionHandler } from "vee-validate";
import { Form, Field } from "vee-validate";

import type {
  Vocabulary,
  VocabularyRequest,
  VocabularyTag,
} from "~/generated/api/client";
import { schemas } from "~/generated/api/client/schemas";
import Button from "~/components/common/Button.vue";
import TextBox from "~/components/common/TextBox.vue";
import TextArea from "~/components/common/TextArea.vue";
import IconButton from "~/components/common/IconButton.vue";
import Label from "~/components/common/Label.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useCommonStore } from "~/stores/common";
import { useVocabularyStore } from "~/stores/vocabulary";
import { useInputDialog } from "~/composables/common/useDialog";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

const props = defineProps<{
  targetVocabulary: Vocabulary | undefined;
  vocabularyTags: VocabularyTag[];
}>();

const emit = defineEmits<{
  (e: "closeModal" | "addedTag"): void;
  (e: "submit", values: VocabularyRequest): void;
}>();

const commonStore = useCommonStore();
const vocabularyStore = useVocabularyStore();

const { showInputDialog, openInputDialog, onInputOk, onInputCancel } =
  useInputDialog();

const openAddTagDialog = () => openInputDialog("New tag name");

const onAddTagOk = async (inputValue?: string) => {
  onInputOk(inputValue);
  const name = inputValue?.trim();
  if (!name) return;
  const newId = await vocabularyStore.postVocabularyTag({
    vocabularyTag: name,
    order: props.vocabularyTags.length + 1,
  });
  emit("addedTag");
  if (newId) {
    selectedTagIds.value.push(newId);
    commonStore.setHasUnsavedChange(true);
  }
};

const selectedTagIds = ref<string[]>(
  Array.from(props.targetVocabulary?.tags ?? [])
    .map((t) => t.vocabularyTagId ?? "")
    .filter(Boolean),
);

watch(
  () => props.targetVocabulary,
  (v) => {
    selectedTagIds.value = Array.from(v?.tags ?? [])
      .map((t) => t.vocabularyTagId ?? "")
      .filter(Boolean);
  },
);

const onToggleTag = (tagId: string) => {
  const idx = selectedTagIds.value.indexOf(tagId);
  if (idx >= 0) {
    selectedTagIds.value.splice(idx, 1);
  } else {
    selectedTagIds.value.push(tagId);
  }
  commonStore.setHasUnsavedChange(true);
};

const validationRules: {
  [K in keyof Pick<VocabularyRequest, "name" | "description">]?: ReturnType<
    typeof zodToVeeRules
  >;
} = {
  name: zodToVeeRules(schemas.VocabularyRequest.shape.name),
  description: zodToVeeRules(schemas.VocabularyRequest.shape.description),
};

const onSubmit: SubmissionHandler<GenericObject> = async (values) => {
  emit("submit", {
    name: values.name as string,
    description: (values.description as string) || undefined,
    tagIds:
      selectedTagIds.value.length > 0
        ? (selectedTagIds.value as unknown as Set<string>)
        : undefined,
  });
};

const onCloseModal = () => emit("closeModal");
</script>
