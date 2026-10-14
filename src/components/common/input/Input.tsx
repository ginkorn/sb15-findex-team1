import {
  createContext,
  useContext,
  type ComponentType,
  type HTMLAttributes,
  type ReactNode,
  type Ref,
} from "react";
import type {
  InputProps as AriaInputProps,
  TextFieldProps as AriaTextFieldProps,
} from "react-aria-components";
import {
  Group as AriaGroup,
  Input as AriaInput,
  TextField as AriaTextField,
} from "react-aria-components";
import { InfoCircle } from "@untitledui/icons";
import { HintText } from "@/components/common/input/HintText";
import { Label } from "@/components/common/input/Label";
import { cx, sortCx } from "@/utils/cx";

export interface InputBaseProps extends TextFieldProps {
  /**
   * Input size.
   * @default "sm"
   */
  size?: "sm" | "md";
  /** Placeholder text. */
  placeholder?: string;
  /** Class name for the icon. */
  iconClassName?: string;
  /** Class name for the input. */
  inputClassName?: string;
  /** Class name for the input wrapper. */
  wrapperClassName?: string;
  /** Keyboard shortcut to display. */
  shortcut?: string | boolean;
  ref?: Ref<HTMLInputElement>;
  groupRef?: Ref<HTMLDivElement>;
  /** Icon component to display on the left side of the input. */
  icon?: ComponentType<HTMLAttributes<HTMLOrSVGElement>>;
}

export const InputBase = ({
  ref,
  shortcut,
  groupRef,
  size = "sm",
  isInvalid,
  isDisabled,
  icon: Icon,
  placeholder,
  wrapperClassName,
  inputClassName,
  iconClassName,
  // Omit this prop to avoid invalid HTML attribute warning
  // isRequired: _isRequired,
  ...inputProps
}: Omit<InputBaseProps, "label" | "hint">) => {
  // Check if the input has a leading icon or tooltip
  const hasLeadingIcon = Icon;

  // If the input is inside a `TextFieldContext`, use its context to simplify applying styles
  const context = useContext(TextFieldContext);

  const inputSize = context?.size || size;

  const sizes = sortCx({
    sm: {
      root: cx("px-3 py-2", hasLeadingIcon && "pl-10"),
      iconLeading: "left-3",
      iconTrailing: "right-3",
      shortcut: "pr-2.5",
    },
    md: {
      root: cx("px-3.5 py-2.5", hasLeadingIcon && "pl-10.5"),
      iconLeading: "left-3.5",
      iconTrailing: "right-3.5",
      shortcut: "pr-3",
    },
  });

  return (
    <AriaGroup
      {...{ isDisabled, isInvalid }}
      ref={groupRef}
      className={({ isFocusWithin, isDisabled, isInvalid }) =>
        cx(
          "bg-primary ring-primary relative flex w-full flex-row place-content-center place-items-center rounded-lg shadow-xs ring-1 transition-shadow duration-100 ease-linear ring-inset",

          isFocusWithin && !isDisabled && "ring-brand ring-2",

          // Disabled state styles
          isDisabled && "bg-disabled_subtle ring-disabled cursor-not-allowed",
          "group-disabled:bg-disabled_subtle group-disabled:ring-disabled group-disabled:cursor-not-allowed",

          // Invalid state styles
          isInvalid && "ring-error_subtle",
          "group-invalid:ring-error_subtle",

          // Invalid state with focus-within styles
          isInvalid && isFocusWithin && "ring-error ring-2",
          isFocusWithin && "group-invalid:ring-error group-invalid:ring-2",

          context?.wrapperClassName,
          wrapperClassName,
        )
      }
    >
      {/* Leading icon and Payment icon */}
      {Icon && (
        <Icon
          className={cx(
            "pointer-events-none absolute size-5 text-black",
            isDisabled && "text-fg-disabled",
            sizes[inputSize].iconLeading,
            context?.iconClassName,
            iconClassName,
          )}
        />
      )}

      {/* Input field */}
      <AriaInput
        {...(inputProps as AriaInputProps)}
        ref={ref}
        placeholder={placeholder}
        className={cx(
          "text-md text-primary placeholder:text-placeholder autofill:text-primary m-0 w-full bg-transparent ring-0 outline-hidden placeholder:font-normal autofill:rounded-lg",
          isDisabled && "text-disabled cursor-not-allowed",
          sizes[inputSize].root,
          context?.inputClassName,
          inputClassName,
        )}
      />

      {/* Invalid icon */}
      {isInvalid && (
        <InfoCircle
          className={cx(
            "text-fg-error-secondary pointer-events-none absolute size-4",
            sizes[inputSize].iconTrailing,
          )}
        />
      )}

      {/* Shortcut */}
      {shortcut && (
        <div
          className={cx(
            "to-bg-primary pointer-events-none absolute inset-y-0.5 right-0.5 z-10 flex items-center rounded-r-[inherit] bg-linear-to-r from-transparent to-40% pl-8",
            sizes[inputSize].shortcut,
          )}
        >
          <span
            className={cx(
              "text-quaternary ring-secondary pointer-events-none rounded px-1 py-px text-xs font-medium ring-1 select-none ring-inset",
              isDisabled && "text-disabled bg-transparent",
            )}
            aria-hidden="true"
          >
            {typeof shortcut === "string" ? shortcut : "⌘K"}
          </span>
        </div>
      )}
    </AriaGroup>
  );
};

InputBase.displayName = "InputBase";

interface BaseProps {
  /** Label text for the input */
  label?: string;
  /** Helper text displayed below the input */
  hint?: ReactNode;
}

interface TextFieldProps
  extends
    BaseProps,
    AriaTextFieldProps,
    Pick<
      InputBaseProps,
      "size" | "wrapperClassName" | "inputClassName" | "iconClassName"
    > {
  ref?: Ref<HTMLDivElement>;
}

const TextFieldContext = createContext<TextFieldProps>({});

export const TextField = ({ className, ...props }: TextFieldProps) => {
  return (
    <TextFieldContext.Provider value={props}>
      <AriaTextField
        {...props}
        data-input-wrapper
        className={(state) =>
          cx(
            "group flex h-max w-full flex-col items-start justify-start gap-1.5",
            typeof className === "function" ? className(state) : className,
          )
        }
      />
    </TextFieldContext.Provider>
  );
};

TextField.displayName = "TextField";

interface InputProps extends InputBaseProps, BaseProps {
  /** Whether to hide required indicator from label */
  hideRequiredIndicator?: boolean;
}

export const Input = ({
  size = "sm",
  placeholder,
  icon: Icon,
  label,
  hint,
  shortcut,
  hideRequiredIndicator,
  className,
  ref,
  groupRef,
  iconClassName,
  inputClassName,
  wrapperClassName,
  ...props
}: InputProps) => {
  return (
    <TextField
      aria-label={!label ? placeholder : undefined}
      {...props}
      className={className}
    >
      {({ isRequired, isInvalid }) => (
        <>
          {label && (
            <Label
              isRequired={
                hideRequiredIndicator ? !hideRequiredIndicator : isRequired
              }
            >
              {label}
            </Label>
          )}

          <InputBase
            {...{
              ref,
              groupRef,
              size,
              placeholder,
              icon: Icon,
              shortcut,
              iconClassName,
              inputClassName,
              wrapperClassName,
            }}
          />

          {hint && <HintText isInvalid={isInvalid}>{hint}</HintText>}
        </>
      )}
    </TextField>
  );
};

Input.displayName = "Input";
