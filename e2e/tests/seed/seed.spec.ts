import { test } from "@playwright/test";
import { Configuration } from "../../generated/api/client/configuration";
import {
  UserApi,
  SalaryApi,
  QualificationApi,
  VocabularyApi,
  SharedLinkApi,
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
  const sharedLinkApi = new SharedLinkApi(config);

  // user
  const resUser = await userApi.getUsers();
  const user = resUser.data;
  if (user.userName !== userName || user.emailAddress !== userName) {
    await userApi.putUsers({
      userName,
      emailAddress: userName,
    });
  }

  // salaries
  const resSalary = await salaryApi.getSalaries();
  const salaries = resSalary.data;
  await Promise.all(
    salaries.map((salary) => salaryApi.deleteSalariesById(salary.salaryId!))
  );

  // qualifications
  const resQualification = await qualificationApi.getQualifications();
  const qualifications = resQualification.data;
  await Promise.all(
    qualifications.map((qualification) =>
      qualificationApi.deleteQualificationsById(qualification.qualificationId!)
    )
  );

  // vocabularies
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

  // vocabulary quiz histories
  const resQuizHistories = await vocabularyApi.getVocabularyQuizHistories();
  await Promise.all(
    resQuizHistories.data.map((history) =>
      vocabularyApi.deleteVocabularyQuizHistoriesById(history.quizHistoryId!)
    )
  );

  // shared links
  const resSharedLinks = await sharedLinkApi.getSharedLinks();
  await Promise.all(
    resSharedLinks.data.map((link) =>
      sharedLinkApi.deleteSharedLinksById(link.token!)
    )
  );
});
