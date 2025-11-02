import { test } from "@playwright/test";
import { Configuration } from "../../generated/api/client/configuration";
import {
  UserApi,
  SalaryApi,
  QualificationApi,
} from "../../generated/api/client/api";

import { getTokenFromStorageState } from "../helpers/auth";

test("seed data", async ({ baseURL }) => {
  // get user name from env
  const userName = process.env.E2E_USERNAME;
  if (!userName) {
    throw new Error("E2E_USERNAME is not set");
  }

  // get token from storage state
  const token = getTokenFromStorageState(baseURL!);
  if (!token) {
    throw new Error("Access token did not find in storage state");
  }

  // setup API client
  const config = new Configuration({
    basePath: `${baseURL}/api/v1`,
    baseOptions: {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  });
  const userApi = new UserApi(config);
  const salaryApi = new SalaryApi(config);
  const qualificationApi = new QualificationApi(config);

  // user
  const resUser = await userApi.getUser();
  const user = resUser.data;
  if (user.userName !== userName || user.emailAddress !== userName) {
    await userApi.putUser({
      userName,
      emailAddress: userName,
    });
  }

  // salary
  const resSalary = await salaryApi.getSalary();
  const salaries = resSalary.data;
  for (const salary of salaries) {
    await salaryApi.deleteSalary(salary.salaryId);
  }

  // qualification
  const resQualification = await qualificationApi.getQualification();
  const qualifications = resQualification.data;
  for (const qualification of qualifications) {
    await qualificationApi.deleteQualification(qualification.qualificationId);
  }
});
