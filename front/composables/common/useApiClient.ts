import { Configuration } from "~/generated/api/client/configuration";
import {
  UserApi,
  SalaryApi,
  QualificationApi,
  SettingApi,
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

  const getUserApi = () => new UserApi(getConfiguration());
  const getSalaryApi = () => new SalaryApi(getConfiguration());
  const getQualificationApi = () => new QualificationApi(getConfiguration());
  const getSettingApi = () => new SettingApi(getConfiguration());

  return {
    getConfiguration,
    getUserApi,
    getSalaryApi,
    getQualificationApi,
    getSettingApi,
  };
};
