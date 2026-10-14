import IndexChart from "@/components/pages/dashboard/IndexChart";
import IndexPerformance from "@/components/pages/dashboard/IndexPerformance";
import MajorIndex from "@/components/pages/dashboard/MajorIndex";

export default function Dashboard() {
  return (
    <div className="scroll-hide h-full space-y-16 overflow-auto pb-[120px]">
      <MajorIndex />
      <IndexChart />
      <IndexPerformance />
    </div>
  );
}
