import { defineStore } from "pinia";
import { ref } from "vue";

import type { Salary } from "~/api/client/api";
import { SalaryApi } from "~/api/client/api";
import { useCommonStore } from "~/stores/common";
import { getErrorMessage } from "~/utils/error";

export const useSalaryStore = defineStore("salary", () => {
  const salaryApi = new SalaryApi();
  const commonStore = useCommonStore();
  const salaries = ref<Salary[] | null>(null);

  async function fetchSalary(
    targetDate?: string,
    targetDateFrom?: string,
    targetDateTo?: string
  ) {
    try {
      const res = await salaryApi.getSalary(
        targetDate,
        targetDateFrom,
        targetDateTo
      );
      salaries.value = res.data;
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    }
  }

  async function putSalary(salary: Salary) {
    const id = commonStore.addLoadingQueue();
    try {
      await salaryApi.putSalary(salary);
      await fetchSalary();
    } catch (error) {
      console.error("Failed to call api:", error);
      commonStore.addErrorMessage(getErrorMessage(error));
    } finally {
      commonStore.deleteLoadingQueue(id);
    }
  }

  return {
    salaries,
    fetchSalary,
    putSalary,
  };
});
