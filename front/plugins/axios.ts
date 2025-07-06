import { defineNuxtPlugin, useRuntimeConfig } from "#app";
import axios from "axios";
import urlJoin from "url-join";

import { sleep } from "~/util/sleep";

export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig();
  axios.defaults.baseURL = config.public.apiBaseUrl as string;

  if (config.public.apiMode === "mock") {
    axios.interceptors.request.use((req) => {
      const url = req.url?.replace(/^\/?/, "").replace(/\?.+$/, "");
      req.url = urlJoin("/mock-api/", `${url}.json`);
      req.method = "get";
      return req;
    });

    axios.interceptors.response.use(async (res) => {
      await sleep(500);
      return res;
    });
  }
});
