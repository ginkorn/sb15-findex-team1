import { useState } from "react";
import { useDateFormatter } from "react-aria";
import type {
  DateRangePickerProps as AriaDateRangePickerProps,
  DateValue,
} from "react-aria-components";
import {
  DateRangePicker as AriaDateRangePicker,
  Dialog as AriaDialog,
  Group as AriaGroup,
  Popover as AriaPopover,
} from "react-aria-components";
import { getLocalTimeZone, today } from "@internationalized/date";
import { useControlledState } from "@react-stately/utils";
import { Calendar as CalendarIcon, ChevronDown } from "@untitledui/icons";
import { Button } from "@/components/common/buttons/Button";
import { cx } from "@/utils/cx";
import { DateInput } from "./DateInput";
import { RangeCalendar } from "./RangeCalendar";

const highlightedDates = [today(getLocalTimeZone())];

interface DateRangePickerProps extends AriaDateRangePickerProps<DateValue> {
  /** The function to call when the apply button is clicked. */
  onApply?: () => void;
  /** The function to call when the cancel button is clicked. */
  onCancel?: () => void;
  placeholder: string;
}

export const DateRangePicker = ({
  value: valueProp,
  defaultValue,
  onChange,
  onApply,
  onCancel,
  placeholder,
  ...props
}: DateRangePickerProps) => {
  const formatter = useDateFormatter({
    month: "short",
    day: "numeric",
    year: "numeric",
  });
  const [value, setValue] = useControlledState(
    valueProp,
    defaultValue || null,
    onChange,
  );
  const [focusedValue, setFocusedValue] = useState<DateValue | null>(null);

  const formattedStartDate = value?.start
    ? formatter.format(value.start.toDate(getLocalTimeZone()))
    : placeholder;
  const formattedEndDate = value?.end
    ? formatter.format(value.end.toDate(getLocalTimeZone()))
    : placeholder;

  return (
    <AriaDateRangePicker
      aria-label="Date range picker"
      shouldCloseOnSelect={false}
      {...props}
      value={value}
      onChange={setValue}
    >
      <AriaGroup slot="trigger">
        <Button
          size="md"
          color="secondary"
          iconLeading={<CalendarIcon size={20} stroke="black" />}
          iconTrailing={<ChevronDown />}
          className="h-10 w-full justify-between"
        >
          {!value ? (
            <span className="text-placeholder">{placeholder}</span>
          ) : (
            `${formattedStartDate} – ${formattedEndDate}`
          )}
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
        <AriaDialog className="bg-primary ring-secondary_alt flex rounded-2xl shadow-xl ring focus:outline-hidden">
          {({ close }) => (
            <>
              <div className="flex flex-col">
                <RangeCalendar
                  focusedValue={focusedValue}
                  onFocusChange={setFocusedValue}
                  highlightedDates={highlightedDates}
                />
                <div className="border-secondary flex justify-between gap-3 border-t p-4">
                  <div className="hidden items-center gap-3">
                    <DateInput slot="start" className="w-36" />
                    <div className="text-md text-quaternary">–</div>
                    <DateInput slot="end" className="w-36" />
                  </div>
                  <div className="grid w-full grid-cols-2 gap-3">
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
                </div>
              </div>
            </>
          )}
        </AriaDialog>
      </AriaPopover>
    </AriaDateRangePicker>
  );
};
