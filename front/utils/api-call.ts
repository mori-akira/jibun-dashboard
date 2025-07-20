import { getErrorMessage } from "~/utils/error";

type CommonStoreLike = {
  addLoadingQueue: (id?: string) => string;
  deleteLoadingQueue: (id: string) => void;
  addErrorMessage: (msg: string) => void;
};

export async function withErrorHandling<T>(
  caller: () => Promise<T>,
  commonStore: CommonStoreLike,
  originalId?: string
): Promise<T | undefined> {
  const id = commonStore.addLoadingQueue(originalId);
  try {
    return await caller();
  } catch (err) {
    console.error(err);
    commonStore.addErrorMessage(getErrorMessage(err));
  } finally {
    commonStore.deleteLoadingQueue(id);
  }
}
