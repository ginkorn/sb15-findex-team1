import {
  type ComponentPropsWithRef,
  type TdHTMLAttributes,
  type ThHTMLAttributes,
} from "react";
import type {
  CellProps as AriaCellProps,
  ColumnProps as AriaColumnProps,
  RowProps as AriaRowProps,
  TableHeaderProps as AriaTableHeaderProps,
  TableProps as AriaTableProps,
} from "react-aria-components";
import {
  Cell as AriaCell,
  Collection as AriaCollection,
  Column as AriaColumn,
  Group as AriaGroup,
  Row as AriaRow,
  Table as AriaTable,
  TableBody as AriaTableBody,
  TableHeader as AriaTableHeader,
} from "react-aria-components";
import { ArrowDown, ChevronSelectorVertical } from "@untitledui/icons";
import { cx } from "@/utils/cx";

interface TableRootProps
  extends
    AriaTableProps,
    Omit<ComponentPropsWithRef<"table">, "className" | "slot" | "style"> {}

const TableRoot = ({ className, ...props }: TableRootProps) => {
  return (
    <AriaTable
      {...props}
      className={(state) =>
        cx(
          "w-full",
          typeof className === "function" ? className(state) : className,
        )
      }
    />
  );
};
TableRoot.displayName = "Table";

interface TableHeaderProps<T extends object>
  extends
    AriaTableHeaderProps<T>,
    Omit<
      ComponentPropsWithRef<"thead">,
      "children" | "className" | "slot" | "style"
    > {
  bordered?: boolean;
}

const TableHeader = <T extends object>({
  columns,
  children,
  bordered = true,
  className,
  ...props
}: TableHeaderProps<T>) => {
  return (
    <AriaTableHeader
      {...props}
      className={(state) =>
        cx(
          "bg-secondary relative h-11",
          bordered &&
            "[&>tr>th]:after:bg-border-secondary [&>tr>th]:after:pointer-events-none [&>tr>th]:after:absolute [&>tr>th]:after:inset-x-0 [&>tr>th]:after:bottom-0 [&>tr>th]:after:h-px [&>tr>th]:focus-visible:after:bg-transparent",
          typeof className === "function" ? className(state) : className,
        )
      }
    >
      <AriaCollection items={columns}>{children}</AriaCollection>
    </AriaTableHeader>
  );
};
TableHeader.displayName = "TableHeader";

interface TableHeadProps
  extends
    AriaColumnProps,
    Omit<
      ThHTMLAttributes<HTMLTableCellElement>,
      "children" | "className" | "style" | "id"
    > {
  label?: string;
  isActive?: boolean;
}

const TableHead = ({
  className,
  label,
  children,
  isActive = false,
  ...props
}: TableHeadProps) => {
  return (
    <AriaColumn
      {...props}
      className={(state) =>
        cx(
          "focus-visible:ring-focus-ring focus-visible:ring-offset-bg-primary relative p-0 px-6 py-2 outline-hidden focus-visible:z-1 focus-visible:ring-2 focus-visible:ring-inset",
          state.allowsSorting && "cursor-pointer",
          typeof className === "function" ? className(state) : className,
        )
      }
    >
      {(state) => (
        <AriaGroup className="flex items-center gap-1">
          <div className="flex items-center gap-1">
            {label && (
              <span
                className={`${isActive ? "text-brand-tertiary" : "text-quaternary"} text-xs font-semibold whitespace-nowrap`}
              >
                {label}
              </span>
            )}
            {typeof children === "function" ? children(state) : children}
          </div>

          {state.allowsSorting &&
            (state.sortDirection ? (
              <ArrowDown
                className={cx(
                  "text-text-brand-tertiary size-3 stroke-[3px]",
                  state.sortDirection === "ascending" && "rotate-180",
                )}
              />
            ) : (
              <ChevronSelectorVertical
                size={12}
                strokeWidth={3}
                className="text-fg-quaternary"
              />
            ))}
        </AriaGroup>
      )}
    </AriaColumn>
  );
};
TableHead.displayName = "TableHead";

interface TableRowProps<T extends object>
  extends
    AriaRowProps<T>,
    Omit<
      ComponentPropsWithRef<"tr">,
      "children" | "className" | "onClick" | "slot" | "style" | "id"
    > {
  highlightSelectedRow?: boolean;
}

const TableRow = <T extends object>({
  columns,
  children,
  className,
  highlightSelectedRow = true,
  ...props
}: TableRowProps<T>) => {
  return (
    <AriaRow
      {...props}
      className={(state) =>
        cx(
          "outline-focus-ring hover:bg-secondary relative transition-colors after:pointer-events-none focus-visible:outline-2 focus-visible:-outline-offset-2",
          "h-16",
          highlightSelectedRow && "selected:bg-secondary",
          "[&>td]:after:bg-border-secondary [&>td]:after:absolute [&>td]:after:inset-x-0 [&>td]:after:bottom-0 [&>td]:after:h-px [&>td]:after:w-full last:[&>td]:after:hidden [&>td]:focus-visible:after:opacity-0",
          typeof className === "function" ? className(state) : className,
        )
      }
    >
      <AriaCollection items={columns}>{children}</AriaCollection>
    </AriaRow>
  );
};
TableRow.displayName = "TableRow";

interface TableCellProps
  extends
    AriaCellProps,
    Omit<
      TdHTMLAttributes<HTMLTableCellElement>,
      "children" | "className" | "style" | "id"
    > {}

const TableCell = ({ className, children, ...props }: TableCellProps) => {
  return (
    <AriaCell
      {...props}
      className={(state) =>
        cx(
          "text-tertiary outline-focus-ring relative text-sm focus-visible:z-1 focus-visible:outline-2 focus-visible:-outline-offset-2",
          "px-5",
          typeof className === "function" ? className(state) : className,
        )
      }
    >
      {children}
    </AriaCell>
  );
};
TableCell.displayName = "TableCell";

type TableComponent = typeof TableRoot & {
  Body: typeof AriaTableBody;
  Cell: typeof TableCell;
  Head: typeof TableHead;
  Header: typeof TableHeader;
  Row: typeof TableRow;
};

const Table = Object.assign(TableRoot, {
  Body: AriaTableBody,
  Cell: TableCell,
  Head: TableHead,
  Header: TableHeader,
  Row: TableRow,
}) as TableComponent;

export { Table };
