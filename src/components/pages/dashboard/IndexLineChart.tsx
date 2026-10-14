import { useMemo } from "react";
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  XAxis,
  YAxis,
} from "recharts";
import { Dot } from "@/components/common/foundations/dot-icon";
import { useBreakpoint } from "@/hooks/use-breakpoint";
import type { IndexChartResponse } from "@/model/dashboard";
import { cx } from "@/utils/cx";

interface IndexLineChartProps {
  data: IndexChartResponse;
}

export const IndexLineChart = ({ data }: IndexLineChartProps) => {
  const isDesktop = useBreakpoint("lg");

  // 최신순으로 오는 데이터를 시간 순서(과거 -> 현재)로 뒤집고 하나로 합침
  const combinedData = useMemo(() => {
    const points = data?.dataPoints || [];

    return [...points].reverse().map((point) => {
      const ma5Point = data?.ma5DataPoints?.find((m) => m.date === point.date);
      const ma20Point = data?.ma20DataPoints?.find(
        (m) => m.date === point.date,
      );

      return {
        date: point.date,
        close: point.value,
        ma5: ma5Point?.value,
        ma20: ma20Point?.value,
      };
    });
  }, [data]);

  if (!combinedData?.length) {
    return (
      <div className="text-tertiary flex h-72 items-center justify-center">
        데이터가 없습니다.
      </div>
    );
  }

  const colors: Record<string, string> = {
    A: "text-[#6172F3]",
    B: "text-[#FF4405]",
    C: "text-[#36BFFA]",
  };

  const calculatedWidth = combinedData.length * 60;

  return (
    <div className="flex h-80 w-full min-w-0 flex-col">
      <div className="scrollbar-thin w-full overflow-x-auto">
        <div
          style={{
            width: `${calculatedWidth}px`,
            minWidth: "100%",
          }}
          className="h-64"
        >
          <ResponsiveContainer
            width="100%"
            height={256}
            minWidth={0}
            debounce={50}
          >
            <AreaChart
              data={combinedData}
              className="text-tertiary [&_.recharts-text]:text-xs"
              margin={{
                top: 0,
                right: 24,
                left: 0,
                bottom: 0,
              }}
            >
              <CartesianGrid
                vertical={false}
                stroke="currentColor"
                className="text-utility-gray-100"
              />
              <XAxis
                fill="currentColor"
                axisLine={false}
                tickLine={false}
                interval={isDesktop ? 0 : "preserveStartEnd"}
                dataKey="date"
                tick={{ dx: 40, fill: "currentColor" }}
                // 2025-12-04 -> 12.04 형식으로 포맷팅
                tickFormatter={(value: string) => {
                  const parts = value.split("-");
                  return parts.length >= 3 ? `${parts[1]}.${parts[2]}` : value;
                }}
                padding={{ left: 0, right: 0 }}
              ></XAxis>
              <YAxis
                fill="currentColor"
                axisLine={false}
                tickLine={false}
                interval="preserveStartEnd"
                tickFormatter={(value) => Number(value).toLocaleString()}
                domain={["auto", "auto"]}
              ></YAxis>
              <Area
                isAnimationActive={false}
                className={cx(
                  colors["A"],
                  "[&_.recharts-area-area]:translate-y-1.5 [&_.recharts-area-area]:[clip-path:inset(0_0_6px_0)]",
                )}
                dataKey="close"
                name="종가"
                type="monotone"
                stroke="currentColor"
                strokeWidth={2}
                fill="url(#gradient)"
                fillOpacity={0.1}
                activeDot={{
                  className: "fill-bg-primary stroke-[#6172F3] stroke-2",
                }}
              />
              <Area
                isAnimationActive={false}
                className={cx(
                  colors["B"],
                  "[&_.recharts-area-area]:translate-y-1.5 [&_.recharts-area-area]:[clip-path:inset(0_0_6px_0)]",
                )}
                dataKey="ma5"
                name="5일 평균"
                type="monotone"
                stroke="currentColor"
                strokeWidth={2}
                fill="none"
                activeDot={{
                  className: "fill-bg-primary stroke-[#FF4405] stroke-2",
                }}
              />
              <Area
                isAnimationActive={false}
                className={cx(
                  colors["C"],
                  "[&_.recharts-area-area]:translate-y-1.5 [&_.recharts-area-area]:[clip-path:inset(0_0_6px_0)]",
                )}
                dataKey="ma20"
                name="20일 이동 평균"
                type="monotone"
                stroke="currentColor"
                strokeWidth={2}
                fill="none"
                activeDot={{
                  className: "fill-bg-primary stroke-[#36BFFA] stroke-2",
                }}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>
      <div className="flex justify-center border-t border-gray-50 pt-8">
        <div className="text-text-tertiary flex gap-10 text-xs font-medium">
          <div className="flex items-center gap-1.5">
            <Dot size="sm" className="text-[#6172F3]" /> 종가
          </div>
          <div className="flex items-center gap-1.5">
            <Dot size="sm" className="text-[#FF4405]" /> 5일 평균
          </div>
          <div className="flex items-center gap-1.5">
            <Dot size="sm" className="text-[#36BFFA]" /> 20일 이동 평균
          </div>
        </div>
      </div>
    </div>
  );
};
