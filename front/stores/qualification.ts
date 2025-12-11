import { defineStore } from "pinia";
import { ref } from "vue";

import { Configuration } from "~/generated/api/client/configuration";
import type {
  Qualification,
  GetQualificationsStatusEnum,
  GetQualificationsRankEnum,
  QualificationBase,
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
    status?: Array<GetQualificationsStatusEnum>,
    rank?: Array<GetQualificationsRankEnum>,
    organization?: string,
    acquiredDateFrom?: string,
    acquiredDateTo?: string,
    expirationDateFrom?: string,
    expirationDateTo?: string
  ) {
    const res = await getQualificationApi().getQualifications(
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

  async function postQualification(qualification: QualificationBase) {
    await getQualificationApi().postQualifications(qualification);
  }

  async function putQualification(
    qualificationId: string | undefined,
    qualification: QualificationBase
  ) {
    if (qualificationId) {
      await getQualificationApi().putQualifications(
        qualificationId,
        qualification
      );
    } else {
      await postQualification(qualification);
    }
  }

  async function deleteQualification(qualificationId: string) {
    await getQualificationApi().deleteQualifications(qualificationId);
  }

  return {
    qualifications,
    fetchQualification,
    postQualification,
    putQualification,
    deleteQualification,
  };
});
