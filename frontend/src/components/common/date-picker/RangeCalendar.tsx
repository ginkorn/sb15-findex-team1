import type { PropsWithChildren } from "react";
import { Fragment, useContext, useState } from "react";
import { ChevronLeft, ChevronRight } from "@untitledui/icons";
import type {
  RangeCalendarProps as AriaRangeCalendarProps,
  DateValue,
} from "react-aria-components";
import {
  CalendarGrid as AriaCalendarGrid,
  CalendarGridBody as AriaCalendarGridBody,
  CalendarGridHeader as AriaCalendarGridHeader,
  CalendarHeaderCell as AriaCalendarHeaderCell,
  RangeCalendar as AriaRangeCalendar,
  RangeCalendarContext,
  RangeCalendarStateContext,
  useSlottedContext,
} from "react-aria-components";
import { Button } from "@/components/common/buttons/Button";
import { CalendarCell } from "./Cell";
import { DateInput } from "./DateInput";

export const RangeCalendarContextProvider = ({
  children,
}: PropsWithChildren) => {
  const [value, onChange] = useState<{
    start: DateValue;
    end: DateValue;
  } | null>(null);
  const [focusedValue, onFocusChange] = useState<DateValue | undefined>();

  return (
    <RangeCalendarContext.Provider
      value={{ value, onChange, focusedValue, onFocusChange }}
    >
      {children}
    </RangeCalendarContext.Provider>
  );
};

const RangeCalendarTitle = ({ part }: { part: "start" | "end" }) => {
  const context = useContext(RangeCalendarStateContext);

  if (!context) {
    throw new Error(
      "<RangeCalendarTitle /> must be used within a <RangeCalendar /> component."
    );
  }

  const target =
    part === "start" ? context.visibleRange.start : context.visibleRange.end;

  const year = target.year;
  const month = String(target.month).padStart(2, "0");

  return (
    <>
      {year}. {month}
    </>
  );
};
interface RangeCalendarProps extends AriaRangeCalendarProps<DateValue> {
  /** The dates to highlight. */
  highlightedDates?: DateValue[];
}

export const RangeCalendar = ({ ...props }: RangeCalendarProps) => {
  const context = useSlottedContext(RangeCalendarContext);

  const ContextWrapper = context ? Fragment : RangeCalendarContextProvider;

  return (
    <ContextWrapper>
      <AriaRangeCalendar
        className="flex items-start"
        visibleDuration={{
          months: 1,
        }}
        {...props}
      >
        <div className="flex flex-col gap-3 px-6 py-5">
          <header className="relative flex items-center justify-between">
            <Button
              slot="previous"
              iconLeading={ChevronLeft}
              size="sm"
              color="tertiary"
              className="size-8"
            />

            <h2 className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 text-sm font-semibold text-fg-secondary">
              <RangeCalendarTitle part="start" />
            </h2>

            <Button
              slot="next"
              iconLeading={ChevronRight}
              size="sm"
              color="tertiary"
              className="size-8"
            />
          </header>

          <div className="flex items-center gap-2">
            <DateInput slot="start" className="flex-1" />
            <div className="text-md text-quaternary">–</div>
            <DateInput slot="end" className="flex-1" />
          </div>

          <AriaCalendarGrid weekdayStyle="short" className="w-max">
            <AriaCalendarGridHeader>
              {(day) => (
                <AriaCalendarHeaderCell className="border-b-4 border-transparent p-0">
                  <div className="flex size-10 items-center justify-center text-sm font-medium text-secondary">
                    {day.slice(0, 2)}
                  </div>
                </AriaCalendarHeaderCell>
              )}
            </AriaCalendarGridHeader>
            <AriaCalendarGridBody className="[&_td]:p-0 [&_tr]:border-b-4 [&_tr]:border-transparent [&_tr:last-of-type]:border-none">
              {(date) => <CalendarCell date={date} />}
            </AriaCalendarGridBody>
          </AriaCalendarGrid>
        </div>
      </AriaRangeCalendar>
    </ContextWrapper>
  );
};
