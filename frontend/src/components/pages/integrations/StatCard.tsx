import { Check, Clock, X } from "@untitledui/icons";

interface StatCardProps {
  title: string;
  value: number | string;
  unit: string;
  subtitle: string;
  type: "success" | "error" | "pending";
}

export const StatCard = ({
  title,
  value,
  unit,
  subtitle,
  type,
}: StatCardProps) => {
  const getStyles = () => {
    switch (type) {
      case "success":
        return {
          bg: "bg-utility-brand-50_alt",
          border: "border-utility-brand-300",
          text: "text-[#2D3282]",
          icon: <Check className="h-6 w-6 text-[#444CE7]" />,
          iconBg: "bg-utility-brand-100",
        };
      case "error":
        return {
          bg: "bg-error-primary",
          border: "border-utility-error-300",
          text: "text-[#D92D20]",
          icon: <X className="h-6 w-6 text-[#D92D20]" />,
          iconBg: "bg-error-100",
        };
      case "pending":
        return {
          bg: "bg-tertiary",
          border: "border-utility-gray-400",
          text: "text-gray-700",
          icon: <Clock className="h-6 w-6 text-gray-600" />,
          iconBg: "bg-utility-gray-200",
        };
    }
  };

  const styles = getStyles();

  return (
    <div
      className={`${styles.bg} ${styles.border} relative flex w-full min-w-60 flex-col gap-3 rounded-2xl border p-6`}
    >
      <div className="flex items-center justify-between">
        <h3 className="text-primary text-md font-semibold">{title}</h3>
      </div>

      <div className="flex items-baseline">
        <span className={`text-3xl font-semibold ${styles.text}`}>{value}</span>
        <span className={`text-3xl font-semibold ${styles.text}`}>{unit}</span>
      </div>

      <p className="text-sm text-gray-500">{subtitle}</p>

      <div
        className={`absolute top-1/2 right-5 flex size-12 -translate-y-1/2 items-center justify-center rounded-md ${styles.iconBg}`}
      >
        {styles.icon}
      </div>
    </div>
  );
};
