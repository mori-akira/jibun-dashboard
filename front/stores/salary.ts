import { defineStore } from "pinia";
import { ref } from "vue";

import type {
  Salary,
  SalaryBase,
  SalaryOcrTask,
} from "~/generated/api/client/api";
import { useApiClient } from "~/composables/common/useApiClient";

export const useSalaryStore = defineStore("salary", () => {
  const salaries = ref<Salary[] | null>(null);
  const salaryOcrTasks = ref<SalaryOcrTask[] | null>(null);
  const { getSalaryApi } = useApiClient();

  async function fetchSalary(
    targetDate?: string,
    targetDateFrom?: string,
    targetDateTo?: string
  ) {
    const res = await getSalaryApi().getSalaries(
      targetDate,
      targetDateFrom,
      targetDateTo
    );
    salaries.value = res.data;
  }

  async function postSalary(salary: SalaryBase) {
    await getSalaryApi().postSalaries(salary);
  }

  async function putSalary(salaryId: string | undefined, salary: SalaryBase) {
    if (salaryId) {
      await getSalaryApi().putSalaries(salaryId, salary);
    } else {
      await postSalary(salary);
    }
  }

  async function deleteSalary(salaryId: string) {
    await getSalaryApi().deleteSalaries(salaryId);
  }

  async function fetchSalaryOcrTasks(targetDate: string) {
    const res = await getSalaryApi().getSalaryOcrTasks(targetDate);
    salaryOcrTasks.value = res.data;
  }

  async function isRunningSalaryOcrTask(targetDate: string) {
    await fetchSalaryOcrTasks(targetDate);
    return (
      salaryOcrTasks.value?.some(
        (task) => task.status === "PENDING" || task.status === "RUNNING"
      ) ?? false
    );
  }

  return {
    salaries,
    salaryOcrTasks,
    fetchSalary,
    postSalary,
    putSalary,
    deleteSalary,
    fetchSalaryOcrTasks,
    isRunningSalaryOcrTask,
  };
});
