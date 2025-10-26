<template>
  <div>
    <MultiOptionSelector
      label="Status"
      :options="['acquired', 'planning', 'dream']"
      :values="['acquired', 'planning', 'dream']"
      :selected-options="selectedStatus"
      wrapper-class="m-4 flex justify-start items-center"
      label-class="w-25 font-cursive"
      @click:value="onClickStatusOption"
    ></MultiOptionSelector>
    <MultiOptionSelector
      label="Rank"
      :options="['A', 'B', 'C', 'D']"
      :values="['A', 'B', 'C', 'D']"
      :selected-options="selectedRank"
      wrapper-class="m-4 flex justify-start items-center"
      label-class="w-25 font-cursive"
      @click:value="onClickRankOption"
    ></MultiOptionSelector>

    <Accordion title="More Detail" title-class="font-cursive">
      <TextBox
        label="Qualification Name"
        :value="qualificationName"
        wrapper-class="m-4"
        label-class="w-40 font-cursive"
        input-wrapper-class="w-1/2"
        @blur:value="onBlurQualificationName"
      />
      <TextBox
        label="Organization"
        :value="organization"
        wrapper-class="m-4"
        label-class="w-40 font-cursive"
        input-wrapper-class="w-1/2"
        @blur:value="onBlurOrganization"
      />
      <DatePickerFromTo
        label="Acquired Date"
        text-input
        :date-from="acquiredDateFrom"
        :date-to="acquiredDateTo"
        wrapper-class="m-4"
        label-class="w-40 font-cursive"
        pickers-wrapper-class="min-w-96 w-1/2"
        @change:from="onChangeAcquiredDateFrom"
        @change:to="onChangeAcquiredDateTo"
      />
      <DatePickerFromTo
        label="Expiration Date"
        text-input
        :date-from="expirationDateFrom"
        :date-to="expirationDateTo"
        wrapper-class="m-4"
        label-class="w-40 font-cursive"
        pickers-wrapper-class="min-w-96 w-1/2"
        @change:from="onChangeExpirationDateFrom"
        @change:to="onChangeExpirationDateTo"
      />
    </Accordion>
  </div>
</template>

<script setup lang="ts">
import type {
  GetQualificationStatusEnum,
  GetQualificationRankEnum,
} from "~/generated/api/client/api";
import Accordion from "~/components/common/Accordion.vue";
import MultiOptionSelector from "~/components/common/MultiOptionSelector.vue";
import TextBox from "~/components/common/TextBox.vue";
import DatePickerFromTo from "~/components/common/DatePickerFromTo.vue";

const props = defineProps<{
  selectedStatus: string[];
  selectedRank: string[];
  qualificationName: string;
  organization: string;
  acquiredDateFrom: string;
  acquiredDateTo: string;
  expirationDateFrom: string;
  expirationDateTo: string;
}>();
const emits = defineEmits<{
  (
    event: "update:selectedStatus" | "update:selectedRank",
    value: string[]
  ): void;
  (
    event:
      | "update:qualificationName"
      | "update:organization"
      | "update:acquiredDateFrom"
      | "update:acquiredDateTo"
      | "update:expirationDateFrom"
      | "update:expirationDateTo",
    value: string
  ): void;
}>();

const onClickStatusOption = async (value: string) => {
  let selectedStatus = [...props.selectedStatus];
  if (props.selectedStatus.includes(value as GetQualificationStatusEnum)) {
    selectedStatus = props.selectedStatus.filter((e) => e !== value);
  } else {
    selectedStatus.push(value as GetQualificationStatusEnum);
  }
  emits("update:selectedStatus", selectedStatus);
};
const onClickRankOption = async (value: string) => {
  let selectedRank = [...props.selectedRank];
  if (props.selectedRank.includes(value as GetQualificationRankEnum)) {
    selectedRank = props.selectedRank.filter((e) => e !== value);
  } else {
    selectedRank.push(value as GetQualificationRankEnum);
  }
  emits("update:selectedRank", selectedRank);
};
const onBlurQualificationName = async (value: string) => {
  if (props.qualificationName === value) {
    return;
  }
  emits("update:qualificationName", value);
};
const onBlurOrganization = async (value: string) => {
  if (props.organization === value) {
    return;
  }
  emits("update:organization", value);
};
const onChangeAcquiredDateFrom = async (value: string | undefined) => {
  if (props.acquiredDateFrom === value) {
    return;
  }
  emits("update:acquiredDateFrom", value ?? "");
};
const onChangeAcquiredDateTo = async (value: string | undefined) => {
  if (props.acquiredDateTo === value) {
    return;
  }
  emits("update:acquiredDateTo", value ?? "");
};
const onChangeExpirationDateFrom = async (value: string | undefined) => {
  if (props.expirationDateFrom === value) {
    return;
  }
  emits("update:expirationDateFrom", value ?? "");
};
const onChangeExpirationDateTo = async (value: string | undefined) => {
  if (props.expirationDateTo === value) {
    return;
  }
  emits("update:expirationDateTo", value ?? "");
};
</script>
