import { ref, onUnmounted } from "vue";

export type UsePollingContext<T> = {
  result: T;
  elapsedMs: number;
  startedAt: number;
};

export type UsePollingOptions<T> = {
  intervalMs: number;
  timeoutMs?: number;
  immediate?: boolean;
  onPoll: () => Promise<T> | T;
  onSetup?: () => Promise<void> | void;
  onTeardown?: () => Promise<void> | void;
  shouldStop?: (ctx: UsePollingContext<T>) => boolean | Promise<boolean>;
  onTimeout?: (ctx: UsePollingContext<T>) => void | Promise<void>;
};

export const usePolling = <T = unknown>(options: UsePollingOptions<T>) => {
  const isActive = ref(false);
  const isRunning = ref(false);
  const isTimedOut = ref(false);
  const lastResult = ref<T | null>(null);
  const error = ref<unknown | null>(null);

  let timer: ReturnType<typeof setInterval> | null = null;
  let startedAt = 0;

  const stop = async () => {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
    isActive.value = false;
    if (options.onTeardown) {
      try {
        await options.onTeardown();
      } catch (e) {
        console.error("[usePolling] onTeardown error", e);
      }
    }
  };

  // 一度だけ実行
  const pollOnce = async () => {
    if (!isActive.value || isRunning.value) {
      return;
    }

    isRunning.value = true;
    error.value = null;

    try {
      const res = await options.onPoll();
      lastResult.value = res;

      const elapsed = Date.now() - startedAt;
      const ctx: UsePollingContext<T> = {
        result: res,
        elapsedMs: elapsed,
        startedAt,
      };

      let shouldStopNow = false;
      const timedOut = options.timeoutMs != null && elapsed > options.timeoutMs;
      if (timedOut) {
        isTimedOut.value = true;
        if (options.onTimeout) {
          try {
            await options.onTimeout(ctx);
          } catch (e) {
            console.error("[usePolling] onTimeout error", e);
          }
        }
        shouldStopNow = true;
      } else if (options.shouldStop) {
        const stopByUser = await options.shouldStop(ctx);
        shouldStopNow = stopByUser;
      }

      if (shouldStopNow) {
        await stop();
      }
    } catch (e) {
      error.value = e;
      await stop();
    } finally {
      isRunning.value = false;
    }
  };

  // ポーリング開始
  const start = async () => {
    if (isActive.value) return;
    await stop();

    startedAt = Date.now();
    isActive.value = true;
    isTimedOut.value = false;
    error.value = null;
    lastResult.value = null;

    if (options.onSetup) {
      await options.onSetup();
    }

    const immediate = options.immediate ?? true;

    if (immediate) {
      await pollOnce();
    }

    if (!options.intervalMs || !isActive.value) {
      return;
    }

    timer = setInterval(() => {
      void pollOnce();
    }, options.intervalMs);
  };

  onUnmounted(() => {
    void stop();
  });

  return {
    isActive,
    isRunning,
    isTimedOut,
    lastResult,
    error,
    start,
    stop,
    pollOnce,
  };
};
