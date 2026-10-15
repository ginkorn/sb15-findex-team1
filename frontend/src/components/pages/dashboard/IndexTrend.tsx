import { ArrowDown, ArrowUp, Minus } from "@untitledui/icons";
import { ProgressBar } from "@/components/common/progress-indicators/ProgressIndicators";
import { cx } from "@/utils/cx";

interface Props {
  diff?: number;
  rate?: number;
  progressValue?: number;
}

export const IndexTrend = ({ diff, rate, progressValue }: Props) => {
  const isUp = (rate ?? diff ?? 0) > 0;
  const isDown = (rate ?? diff ?? 0) < 0;
  const isNeutral = !isUp && !isDown;
  // 값에 따른 상태
  const statusClass = isUp
    ? "text-text-success-primary"
    : isDown
      ? "text-text-error-primary"
      : "text-text-tertiary";

  const iconColor = isUp ? "#079455" : isDown ? "#D92D20" : "#A4A7AE";

  // 프로그래스바 색상
  const progressColor = isUp
    ? "bg-text-success-primary"
    : isDown
      ? "bg-fg-error-primary"
      : "bg-quaternary";

  return (
    <div className="w-full">
      <div className="flex items-center gap-0.5 pb-2">
        {isUp && <ArrowUp size={16} color={iconColor} />}
        {isDown && <ArrowDown size={16} color={iconColor} />}
        {isNeutral && <Minus size={16} color={iconColor} />}
        <span className={cx(statusClass, "text-sm font-medium tabular-nums")}>
          {diff !== undefined && Math.abs(diff).toFixed(2)}
          {rate !== undefined && (
            <>
              {diff !== undefined
                ? ` (${Math.trunc(rate)}%)`
                : `${Math.trunc(rate)}%`}
            </>
          )}
        </span>
      </div>

      {progressValue !== undefined && (
        <ProgressBar
          min={0}
          max={100}
          value={progressValue}
          progressClassName={progressColor}
        />
      )}
    </div>
  );
};
