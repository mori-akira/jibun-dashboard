import { test } from "@playwright/test";
import { Configuration } from "../../generated/api/client/configuration";
import {
  UserApi,
  SalaryApi,
  QualificationApi,
  VocabularyApi,
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
  const vocabularyApi = new VocabularyApi(config);

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
  await Promise.all(
    salaries.map((salary) => salaryApi.deleteSalariesById(salary.salaryId!))
  );

  // qualification
  const resQualification = await qualificationApi.getQualifications();
  const qualifications = resQualification.data;
  await Promise.all(
    qualifications.map((qualification) =>
      qualificationApi.deleteQualificationsById(qualification.qualificationId!)
    )
  );

  // vocabulary (delete vocabularies before tags due to tag references)
  const resVocabularies = await vocabularyApi.getVocabularies();
  const vocabularies = resVocabularies.data;
  await Promise.all(
    vocabularies.map((vocabulary) =>
      vocabularyApi.deleteVocabulariesById(vocabulary.vocabularyId!)
    )
  );

  // vocabulary tags
  const resVocabularyTags = await vocabularyApi.getVocabularyTags();
  const vocabularyTags = resVocabularyTags.data;
  await Promise.all(
    vocabularyTags.map((tag) =>
      vocabularyApi.deleteVocabularyTagsById(tag.vocabularyTagId!)
    )
  );
});
