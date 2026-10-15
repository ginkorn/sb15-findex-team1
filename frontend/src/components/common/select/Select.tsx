import type { FC, ReactNode, Ref, RefAttributes } from "react";
import { createContext, isValidElement, useMemo, useState } from "react";
import type { SelectProps as AriaSelectProps } from "react-aria-components";
import {
  Button as AriaButton,
  ListBox as AriaListBox,
  Select as AriaSelect,
  SelectValue as AriaSelectValue,
} from "react-aria-components";
import { ChevronDown, SearchMd } from "@untitledui/icons";
import { cx } from "@/utils/cx";
import { isReactComponent } from "@/utils/is-react-component";
import { HintText } from "../input/HintText";
import { Input } from "../input/Input";
import { Label } from "../input/Label";
import { Popover } from "./Popover";
import { SelectItem } from "./SelectItem";

export type SelectItemType = {
  id: number;
  label?: string;
  value?: string | boolean;
  avatarUrl?: string;
  isDisabled?: boolean;
  supportingText?: string;
  icon?: FC | ReactNode;
};

export interface CommonProps {
  hint?: string;
  label?: string;
  tooltip?: string;
  size?: "sm" | "md";
  placeholder?: string;
}

interface SelectProps
  extends
    Omit<AriaSelectProps<SelectItemType>, "children" | "items">,
    RefAttributes<HTMLDivElement>,
    CommonProps {
  items?: SelectItemType[];
  popoverClassName?: string;
  placeholderIcon?: FC | ReactNode;
  searchable?: boolean;
  searchPlaceholder?: string;
  children: ReactNode | ((item: SelectItemType) => ReactNode);
}

interface SelectValueProps {
  isOpen: boolean;
  size: "sm" | "md";
  isFocused: boolean;
  isDisabled: boolean;
  placeholder?: string;
  ref?: Ref<HTMLButtonElement>;
  placeholderIcon?: FC | ReactNode;
}

interface SearchInputProps {
  size: "sm" | "md";
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

const sizes = {
  sm: { root: "py-2 px-3", shortcut: "pr-2.5" },
  md: { root: "py-2.5 px-3.5", shortcut: "pr-3" },
};

const SearchInput = ({
  size,
  value,
  onChange,
  placeholder = "Search...",
}: SearchInputProps) => {
  return (
    <div className="p-2">
      <Input
        size={size}
        icon={SearchMd}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        wrapperClassName="shadow-none"
        autoFocus
      />
    </div>
  );
};

const SelectValue = ({
  isOpen,
  isFocused,
  isDisabled,
  size,
  placeholder,
  placeholderIcon,
  ref,
}: SelectValueProps) => {
  return (
    <AriaButton
      ref={ref}
      className={cx(
        "bg-primary ring-primary relative flex w-full cursor-pointer items-center rounded-lg shadow-xs ring-1 outline-hidden transition duration-100 ease-linear ring-inset",
        (isFocused || isOpen) && "ring-brand ring-2",
        isDisabled && "bg-disabled_subtle text-disabled cursor-not-allowed",
      )}
    >
      <AriaSelectValue<SelectItemType>
        className={cx(
          "flex h-max w-full items-center justify-start gap-2 truncate text-left align-middle",
          "*:data-icon:text-fg-quaternary in-disabled:*:data-icon:text-fg-disabled *:data-icon:size-5 *:data-icon:shrink-0",
          sizes[size].root,
        )}
      >
        {(state) => {
          const Icon = state.selectedItem?.icon || placeholderIcon;
          return (
            <>
              {isReactComponent(Icon) ? (
                <Icon data-icon aria-hidden="true" />
              ) : isValidElement(Icon) ? (
                Icon
              ) : null}

              {state.selectedItem ? (
                <section className="flex w-full gap-2 truncate">
                  <p className="text-md text-primary truncate font-medium">
                    {state.selectedItem?.label}
                  </p>
                  {state.selectedItem?.supportingText && (
                    <p className="text-md text-tertiary">
                      {state.selectedItem?.supportingText}
                    </p>
                  )}
                </section>
              ) : (
                <p
                  className={cx(
                    "text-md text-placeholder",
                    isDisabled && "text-disabled",
                  )}
                >
                  {placeholder}
                </p>
              )}

              <ChevronDown
                aria-hidden="true"
                className={cx(
                  "ml-auto shrink-0",
                  size === "sm" ? "size-4 stroke-[2.5px]" : "size-5",
                )}
              />
            </>
          );
        }}
      </AriaSelectValue>
    </AriaButton>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export const SelectContext = createContext<{ size: "sm" | "md" }>({
  size: "sm",
});

const Select = ({
  placeholder = "Select",
  placeholderIcon,
  size = "sm",
  children,
  items,
  label,
  hint,
  tooltip,
  className,
  searchable = false,
  searchPlaceholder = "검색",
  ...rest
}: SelectProps) => {
  const [searchQuery, setSearchQuery] = useState("");

  // 검색어로 items 필터링
  const filteredItems = useMemo(() => {
    // items가 없으면 즉시 빈 배열 반환
    if (!items || !Array.isArray(items)) {
      return [];
    }

    // 검색어가 없으면 원본 items 반환
    if (!searchable || !searchQuery.trim()) {
      return items;
    }

    const query = searchQuery.toLowerCase();
    return items.filter((item) => {
      const labelMatch = item.label?.toLowerCase().includes(query);
      const supportingTextMatch = item.supportingText
        ?.toLowerCase()
        .includes(query);
      return labelMatch || supportingTextMatch;
    });
  }, [items, searchQuery, searchable]);

  // Popover가 닫힐 때 검색어 리셋
  const handleOpenChange = (isOpen: boolean) => {
    if (!isOpen) {
      setSearchQuery("");
    }
    rest.onOpenChange?.(isOpen);
  };

  return (
    <SelectContext.Provider value={{ size }}>
      <AriaSelect
        {...rest}
        onOpenChange={handleOpenChange}
        className={(state) =>
          cx(
            "flex flex-col gap-1.5",
            typeof className === "function" ? className(state) : className,
          )
        }
      >
        {(state) => (
          <>
            {label && (
              <Label isRequired={state.isRequired} tooltip={tooltip}>
                {label}
              </Label>
            )}

            <SelectValue
              {...state}
              {...{ size, placeholder }}
              placeholderIcon={placeholderIcon}
            />

            <Popover size={size} className={rest.popoverClassName}>
              {searchable && (
                <SearchInput
                  size={size}
                  value={searchQuery}
                  onChange={setSearchQuery}
                  placeholder={searchPlaceholder}
                />
              )}
              <AriaListBox
                items={filteredItems}
                className="size-full outline-hidden"
              >
                {children}
              </AriaListBox>
            </Popover>

            {hint && <HintText isInvalid={state.isInvalid}>{hint}</HintText>}
          </>
        )}
      </AriaSelect>
    </SelectContext.Provider>
  );
};

const _Select = Select as typeof Select & {
  Item: typeof SelectItem;
};
_Select.Item = SelectItem;

export { _Select as Select };
