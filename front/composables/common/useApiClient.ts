import { Configuration } from "~/generated/api/client/configuration";
import {
  CardbookApi,
  FileApi,
  QualificationApi,
  SalaryApi,
  SettingApi,
  ShareApi,
  SharedLinkApi,
  UserApi,
  VocabularyApi,
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

  const getCardbookApi = () => new CardbookApi(getConfiguration());
  const getFileApi = () => new FileApi(getConfiguration());
  const getQualificationApi = () => new QualificationApi(getConfiguration());
  const getSalaryApi = () => new SalaryApi(getConfiguration());
  const getSettingApi = () => new SettingApi(getConfiguration());
  const getShareApi = () => new ShareApi(getPublicConfiguration());
  const getSharedLinkApi = () => new SharedLinkApi(getConfiguration());
  const getUserApi = () => new UserApi(getConfiguration());
  const getVocabularyApi = () => new VocabularyApi(getConfiguration());

  return {
    getConfiguration,
    getCardbookApi,
    getFileApi,
    getQualificationApi,
    getSalaryApi,
    getSettingApi,
    getShareApi,
    getSharedLinkApi,
    getUserApi,
    getVocabularyApi,
  };
};
