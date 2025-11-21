import { defineStore } from "pinia";
import { ref } from "vue";

import { Configuration } from "~/generated/api/client/configuration";
import type {
  Salary,
  SalaryBase,
  SalaryOcrTask,
} from "~/generated/api/client/api";
import { SalaryApi } from "~/generated/api/client/api";
import { useAuth } from "~/composables/common/useAuth";

export const useSalaryStore = defineStore("salary", () => {
  const salaries = ref<Salary[] | null>(null);
  const salaryOcrTasks = ref<SalaryOcrTask[] | null>(null);

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
    const res = await getSalaryApi().getSalaries(
      targetDate,
      targetDateFrom,
      targetDateTo
    );
    salaries.value = res.data;
  }

  async function postSalary(salary: SalaryBase) {
    await getSalaryApi().postSalary(salary);
  }

  async function putSalary(salaryId: string | undefined, salary: SalaryBase) {
    if (salaryId) {
      await getSalaryApi().putSalary(salaryId, salary);
    } else {
      await postSalary(salary);
    }
  }

  async function deleteSalary(salaryId: string) {
    await getSalaryApi().deleteSalary(salaryId);
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
