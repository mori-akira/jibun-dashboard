import moment from "moment";
import { computed } from "vue";

type DatePickerConfigOptions = {
  autoPosition?: "top" | "bottom";
  textInput?: boolean;
};

export const useDatePickerConfig = (options: DatePickerConfigOptions) => {
  const format = (date: Date) => moment(date).format("YYYY-MM-DD");

  const config = computed(() => ({
    enableTimePicker: false,
    teleport: true,
    position: "center" as const,
    autoPosition: options.autoPosition,
    textInput: options.textInput
      ? {
          format: "yyyy-MM-dd",
          enterSubmit: true,
          selectOnFocus: true,
          openMenu: "toggle" as const,
          escClose: true,
        }
      : false,
  }));

  return { format, config };
};
