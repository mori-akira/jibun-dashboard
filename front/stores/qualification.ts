import { defineStore } from "pinia";
import { ref } from "vue";

import type {
  Qualification,
  GetQualificationStatusEnum,
  GetQualificationRankEnum,
  GetQualificationSortKeyEnum,
} from "~/api/client/api";
import { QualificationApi } from "~/api/client/api";
import { useCommonStore } from "~/stores/common";
import { getErrorMessage } from "~/utils/error";

export const useQualificationStore = defineStore("qualification", () => {
  const qualificationApi = new QualificationApi();
  const commonStore = useCommonStore();
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
    try {
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
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    }
  }

  return {
    qualifications,
    fetchQualification,
  };
});
