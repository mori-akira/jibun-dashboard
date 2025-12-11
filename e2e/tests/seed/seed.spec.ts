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
  const resUser = await userApi.getUsers();
  const user = resUser.data;
  if (user.userName !== userName || user.emailAddress !== userName) {
    await userApi.putUsers({
      userName,
      emailAddress: userName,
    });
  }

  // salary
  const resSalary = await salaryApi.getSalaries();
  const salaries = resSalary.data;
  for (let salary of salaries) {
    await salaryApi.deleteSalaries(salary.salaryId!);
  }

  // qualification
  const resQualification = await qualificationApi.getQualifications();
  const qualifications = resQualification.data;
  for (let qualification of qualifications) {
    await qualificationApi.deleteQualifications(qualification.qualificationId!);
  }
});
