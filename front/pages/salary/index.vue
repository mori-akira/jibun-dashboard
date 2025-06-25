<script setup lang="ts">
import { ref } from "vue";
import Panel from "~/components/common/Panel.vue";
import TextBox from "~/components/common/TextBox.vue";

const inputText = ref("未入力");
const onChangeText = (text: string): void => {
  inputText.value = text || "未入力";
};
</script>

<template>
  <div>
    <Panel title="タイトル" />
    <Panel>
      <p>中身のコンテンツ</p>
    </Panel>
    <Panel>
      <TextBox
        label="テキスト"
        @change:value="onChangeText"
        @blur:value="onChangeText"
      />
      <p>入力されたテキスト: {{ inputText }}</p>
    </Panel>
  </div>
</template>
