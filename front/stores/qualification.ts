import { defineStore } from "pinia";
import { ref } from "vue";

import type {
  Qualification,
  GetQualificationStatusEnum,
  GetQualificationRankEnum,
  GetQualificationSortKeyEnum,
} from "~/api/client/api";
import { QualificationApi } from "~/api/client/api";

export const useQualificationStore = defineStore("qualification", () => {
  const qualificationApi = new QualificationApi();
  const qualifications = ref<Qualification[] | null>(null);

  async function fetchQualification(
    qualificationName?: string,
    status?: Array<GetQualificationStatusEnum>,
    rank?: Array<GetQualificationRankEnum>,
    organization?: string,
    acquiredDateFrom?: string,
    acquiredDateTo?: string,
    expirationDateFrom?: string,
    expirationDateTo?: string,
    sortKey?: GetQualificationSortKeyEnum
  ) {
    const res = await qualificationApi.getQualification(
      qualificationName || undefined,
      status || undefined,
      rank || undefined,
      organization || undefined,
      acquiredDateFrom || undefined,
      acquiredDateTo || undefined,
      expirationDateFrom || undefined,
      expirationDateTo || undefined,
      sortKey || undefined
    );
    qualifications.value = res.data;
  }

  async function putQualification(qualification: Qualification) {
    await qualificationApi.putQualification(qualification);
    await fetchQualification();
  }

  async function deleteQualification(qualificationId: string) {
    await qualificationApi.deleteQualification(qualificationId);
  }

  return {
    qualifications,
    fetchQualification,
    putQualification,
    deleteQualification,
  };
});
