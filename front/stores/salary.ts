import { defineStore } from "pinia";
import { ref } from "vue";

import { Configuration } from "~/generated/api/client/configuration";
import type { Salary } from "~/generated/api/client/api";
import { SalaryApi } from "~/generated/api/client/api";
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

  async function deleteSalary(salaryId: string) {
    await getSalaryApi().deleteSalary(salaryId);
  }

  return {
    salaries,
    fetchSalary,
    putSalary,
    deleteSalary,
  };
});
