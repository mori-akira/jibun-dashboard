import { defineNuxtPlugin, useRuntimeConfig } from "#app";
import axios from "axios";
import urlJoin from "url-join";

export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig();
  axios.defaults.baseURL = config.public.apiBaseUrl as string;

  const instance = axios.create();
  console.log(`apiMode: ${config.public.apiMode}`);
  if (config.public.apiMode === "mock") {
    console.log(`mock mode`);
    instance.interceptors.request.use((req) => {
      const url = req.url?.replace(/^\/?/, "");
      console.log(`mock url: ${urlJoin("/mock-api/", `${url}.json`)}`);
      req.url = urlJoin("/mock-api/", `${url}.json`);
      req.method = "get";
      return req;
    });
  }

  return {
    provide: {
      axios: instance,
    },
  };
});
