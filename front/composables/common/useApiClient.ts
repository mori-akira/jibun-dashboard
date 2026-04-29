import { Configuration } from "~/generated/api/client/configuration";
import {
  UserApi,
  SalaryApi,
  QualificationApi,
  SettingApi,
  FileApi,
  VocabularyApi,
  SharedLinkApi,
  ShareApi,
} from "~/generated/api/client/api";
import { useAuth } from "~/composables/common/useAuth";

export const useApiClient = () => {
  const { getAccessToken } = useAuth();

  const getConfiguration = () => {
    return new Configuration({
      baseOptions: {
        headers: { Authorization: `Bearer ${getAccessToken() || ""}` },
      },
    });
  };

  const getPublicConfiguration = () => new Configuration();

  const getUserApi = () => new UserApi(getConfiguration());
  const getSalaryApi = () => new SalaryApi(getConfiguration());
  const getQualificationApi = () => new QualificationApi(getConfiguration());
  const getSettingApi = () => new SettingApi(getConfiguration());
  const getFileApi = () => new FileApi(getConfiguration());
  const getVocabularyApi = () => new VocabularyApi(getConfiguration());
  const getSharedLinkApi = () => new SharedLinkApi(getConfiguration());
  const getShareApi = () => new ShareApi(getPublicConfiguration());

  return {
    getConfiguration,
    getUserApi,
    getSalaryApi,
    getQualificationApi,
    getSettingApi,
    getFileApi,
    getVocabularyApi,
    getSharedLinkApi,
    getShareApi,
  };
};
