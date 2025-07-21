import { defineStore } from "pinia";
import { ref } from "vue";

import type { Salary } from "~/api/client/api";
import { SalaryApi } from "~/api/client/api";

export const useSalaryStore = defineStore("salary", () => {
  const salaryApi = new SalaryApi();
  const salaries = ref<Salary[] | null>(null);

  async function fetchSalary(
    targetDate?: string,
    targetDateFrom?: string,
    targetDateTo?: string
  ) {
    const res = await salaryApi.getSalary(
      targetDate,
      targetDateFrom,
      targetDateTo
    );
    salaries.value = res.data;
  }

  async function putSalary(salary: Salary) {
    await salaryApi.putSalary(salary);
    await fetchSalary();
  }

  return {
    salaries,
    fetchSalary,
    putSalary,
  };
});
