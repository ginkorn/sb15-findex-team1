import type {
  DatePickerProps as AriaDatePickerProps,
  DateValue,
} from "react-aria-components";
import {
  DatePicker as AriaDatePicker,
  Dialog as AriaDialog,
  Group as AriaGroup,
  Popover as AriaPopover,
} from "react-aria-components";
import { getLocalTimeZone, today } from "@internationalized/date";
import { useControlledState } from "@react-stately/utils";
import { Calendar as CalendarIcon, ChevronDown } from "@untitledui/icons";
import { Button } from "@/components/common/buttons/Button";
import { cx } from "@/utils/cx";
import { formatToKNDate } from "@/utils/date";
import { Calendar } from "./Calendar";

const highlightedDates = [today(getLocalTimeZone())];

interface DatePickerProps extends AriaDatePickerProps<DateValue> {
  /** The function to call when the apply button is clicked. */
  onApply?: () => void;
  /** The function to call when the cancel button is clicked. */
  onCancel?: () => void;
  placeholder?: string;
}

export const DatePicker = ({
  value: valueProp,
  defaultValue,
  onChange,
  onApply,
  onCancel,
  placeholder,
  ...props
}: DatePickerProps) => {
  const [value, setValue] = useControlledState(
    valueProp,
    defaultValue || null,
    onChange,
  );

  const formattedDate = formatToKNDate(value) || placeholder;

  return (
    <AriaDatePicker
      shouldCloseOnSelect={false}
      {...props}
      value={value}
      onChange={setValue}
    >
      <AriaGroup>
        <Button
          size="md"
          color="secondary"
          iconLeading={<CalendarIcon size={20} stroke="black" />}
          iconTrailing={<ChevronDown size={16} stroke="black" />}
          className={`${!value ? "text-text-placeholder" : ""} text-md disabled:bg-secondary flex w-full font-normal`}
        >
          {formattedDate}
        </Button>
      </AriaGroup>
      <AriaPopover
        offset={8}
        placement="bottom left"
        className={({ isEntering, isExiting }) =>
          cx(
            "origin-(--trigger-anchor-point) will-change-transform",
            isEntering &&
              "animate-in fade-in placement-right:slide-in-from-left-0.5 placement-top:slide-in-from-bottom-0.5 placement-bottom:slide-in-from-top-0.5 duration-150 ease-out",
            isExiting &&
              "animate-out fade-out placement-right:slide-out-to-left-0.5 placement-top:slide-out-to-bottom-0.5 placement-bottom:slide-out-to-top-0.5 duration-100 ease-in",
          )
        }
      >
        <AriaDialog className="bg-primary ring-secondary_alt rounded-2xl shadow-xl ring">
          {({ close }) => (
            <>
              <div className="flex px-6 py-5">
                <Calendar highlightedDates={highlightedDates} />
              </div>
              <div className="border-secondary grid grid-cols-2 gap-3 border-t p-4">
                <Button
                  size="md"
                  color="secondary"
                  onClick={() => {
                    onCancel?.();
                    close();
                  }}
                >
                  취소
                </Button>
                <Button
                  size="md"
                  color="primary"
                  onClick={() => {
                    onApply?.();
                    close();
                  }}
                >
                  확인
                </Button>
              </div>
            </>
          )}
        </AriaDialog>
      </AriaPopover>
    </AriaDatePicker>
  );
};
