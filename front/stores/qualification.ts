import { defineStore } from "pinia";
import { ref } from "vue";

import { Configuration } from "~/generated/api/client/configuration";
import type {
  Qualification,
  GetQualificationStatusEnum,
  GetQualificationRankEnum,
} from "~/generated/api/client/api";
import { QualificationApi } from "~/generated/api/client/api";
import { useAuth } from "~/composables/common/useAuth";

export const useQualificationStore = defineStore("qualification", () => {
  const qualifications = ref<Qualification[] | null>(null);

  const { getAccessToken } = useAuth();
  const getQualificationApi = () => {
    const configuration = new Configuration({
      baseOptions: {
        headers: { Authorization: `Bearer ${getAccessToken() || ""}` },
      },
    });
    return new QualificationApi(configuration);
  };

  async function fetchQualification(
    qualificationName?: string,
    status?: Array<GetQualificationStatusEnum>,
    rank?: Array<GetQualificationRankEnum>,
    organization?: string,
    acquiredDateFrom?: string,
    acquiredDateTo?: string,
    expirationDateFrom?: string,
    expirationDateTo?: string
  ) {
    const res = await getQualificationApi().getQualification(
      qualificationName || undefined,
      status || undefined,
      rank || undefined,
      organization || undefined,
      acquiredDateFrom || undefined,
      acquiredDateTo || undefined,
      expirationDateFrom || undefined,
      expirationDateTo || undefined
    );
    qualifications.value = res.data;
  }

  async function putQualification(qualification: Qualification) {
    await getQualificationApi().putQualification(qualification);
  }

  async function deleteQualification(qualificationId: string) {
    await getQualificationApi().deleteQualification(qualificationId);
  }

  return {
    qualifications,
    fetchQualification,
    putQualification,
    deleteQualification,
  };
});
