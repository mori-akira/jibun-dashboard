import { defineNuxtPlugin, useRuntimeConfig } from "#app";
import axios from "axios";
import urlJoin from "url-join";

export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig();
  axios.defaults.baseURL = config.public.apiBaseUrl as string;

  const instance = axios.create();
  if (config.public.apiMode === "mock") {
    instance.interceptors.request.use((req) => {
      const url = req.url?.replace(/^\/?/, "");
      req.url = urlJoin("mock-api", `${url}.json`);
      req.method = "get";
      req.baseURL = config.public.baseUrl as string;
      console.log("Mock BASE URL:", req.baseURL);
      console.log("Mock API URL:", req.url);
      return req;
    });
  }

  return {
    provide: {
      axios: instance,
    },
  };
});
