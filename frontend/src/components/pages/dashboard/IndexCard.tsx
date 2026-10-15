import { ArrowDown, ArrowUp } from "@untitledui/icons";
import { ProgressBar } from "@/components/common/progress-indicators/ProgressIndicators";
import type { MajorIndexResponse } from "@/model/dashboard";
import { cx } from "@/utils/cx";

interface IndexCardProps {
  data: MajorIndexResponse;
}

const IndexCard = ({ data }: IndexCardProps) => {
  // versus(변동액) 기준으로 상승 하락
  const isUp = data.versus > 0;
  const isDown = data.versus < 0;
  // 음수 양수 모두 왼쪽부터 프로그래스바 작동
  const progressValue = Math.abs(data.fluctuationRate);

  // 조건별 색상 설정
  const iconColor = isUp ? "#079455" : isDown ? "#D92D20" : "#717680";

  const statusClass = isUp
    ? "text-text-success-primary"
    : isDown
      ? "text-text-error-primary"
      : "text-text-tertiary";

  const progressBgClass = isUp
    ? "bg-fg-success-primary"
    : isDown
      ? "bg-fg-error-primary"
      : "bg-fg-quaternary";

  return (
    <div className="border-secondary min-w-3xs rounded-xl border bg-white p-5 shadow-xs">
      <div className="flex flex-col gap-1 pb-3">
        <p className="text-md truncate font-semibold">{data.indexName}</p>
        <p className="text-text-quaternary text-xs">
          {data.indexClassification}
        </p>
      </div>
      <div className="flex flex-col items-start gap-1.5">
        <p className="text-display-sm font-semibold">
          {data.currentPrice.toLocaleString()}
        </p>
        <div className="flex gap-0.5 pb-2">
          {isUp && <ArrowUp size={16} color={iconColor} />}
          {isDown && <ArrowDown size={16} color={iconColor} />}
          <span className={cx(statusClass, "text-sm font-medium")}>
            {Math.abs(data.versus).toLocaleString()} ({data.fluctuationRate}%)
          </span>
        </div>
        <ProgressBar
          min={0}
          max={100}
          value={progressValue}
          progressClassName={progressBgClass}
        />
      </div>
    </div>
  );
};

export default IndexCard;
