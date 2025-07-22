import { getErrorMessage } from "~/utils/error";

type CommonStoreLike = {
  addLoadingQueue: (id?: string) => string;
  deleteLoadingQueue: (id: string) => void;
  addErrorMessage: (msg: string) => void;
};

export async function withErrorHandling(
  caller: () => Promise<unknown>,
  commonStore: CommonStoreLike,
  originalId?: string
): Promise<boolean> {
  const id = commonStore.addLoadingQueue(originalId);
  try {
    await caller();
    return true;
  } catch (err) {
    console.error(err);
    commonStore.addErrorMessage(getErrorMessage(err));
    return false;
  } finally {
    commonStore.deleteLoadingQueue(id);
  }
}
