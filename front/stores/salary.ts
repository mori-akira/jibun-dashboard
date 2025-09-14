import { defineStore } from "pinia";
import { ref } from "vue";

import { Configuration } from "~/api/client/configuration";
import type { Salary } from "~/api/client/api";
import { SalaryApi } from "~/api/client/api";
import { useAuth } from "~/composables/common/useAuth";

export const useSalaryStore = defineStore("salary", () => {
  const salaries = ref<Salary[] | null>(null);

  const { getAccessToken } = useAuth();
  const getSalaryApi = () => {
    const configuration = new Configuration({
      baseOptions: {
        headers: { Authorization: `Bearer ${getAccessToken() || ""}` },
      },
    });
    return new SalaryApi(configuration);
  };

  async function fetchSalary(
    targetDate?: string,
    targetDateFrom?: string,
    targetDateTo?: string
  ) {
    const res = await getSalaryApi().getSalary(
      targetDate,
      targetDateFrom,
      targetDateTo
    );
    salaries.value = res.data;
  }

  async function putSalary(salary: Salary) {
    await getSalaryApi().putSalary(salary);
  }

  return {
    salaries,
    fetchSalary,
    putSalary,
  };
});
