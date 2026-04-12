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

    <div v-if="vocabularyTags.length > 0" class="mt-4 w-full flex justify-center items-start">
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
      </div>
    </div>

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
  VocabularyBase,
  VocabularyTag,
} from "~/generated/api/client";
import { schemas } from "~/generated/api/client/schemas";
import Button from "~/components/common/Button.vue";
import TextBox from "~/components/common/TextBox.vue";
import TextArea from "~/components/common/TextArea.vue";
import IconButton from "~/components/common/IconButton.vue";
import Label from "~/components/common/Label.vue";
import { useCommonStore } from "~/stores/common";
import { zodToVeeRules } from "~/utils/zod-to-vee-rules";

const props = defineProps<{
  targetVocabulary: Vocabulary | undefined;
  vocabularyTags: VocabularyTag[];
}>();

const emit = defineEmits<{
  (e: "closeModal"): void;
  (e: "submit", values: VocabularyBase): void;
}>();

const commonStore = useCommonStore();

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

const validationRules: { [K in keyof Pick<VocabularyBase, "name" | "description">]?: ReturnType<typeof zodToVeeRules> } = {
  name: zodToVeeRules(schemas.VocabularyBase.shape.name),
  description: zodToVeeRules(schemas.VocabularyBase.shape.description),
};

const onSubmit: SubmissionHandler<GenericObject> = async (values) => {
  const selectedTags = props.vocabularyTags.filter((t) =>
    selectedTagIds.value.includes(t.vocabularyTagId ?? ""),
  );
  emit("submit", {
    name: values.name as string,
    description: (values.description as string) || undefined,
    tags: selectedTags.length > 0 ? selectedTags as unknown as Set<VocabularyTag> : undefined,
  });
};

const onCloseModal = () => emit("closeModal");
</script>
