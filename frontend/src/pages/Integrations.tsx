import { IndexSyncListSection } from "@/components/pages/integrations/IndexSyncListSection";
import { StatSection } from "@/components/pages/integrations/StatSection";
import { SyncHistorySection } from "@/components/pages/integrations/SyncHistorySection";

export default function Integrations() {
  return (
    <div className="scrollbar-thin flex min-h-0 flex-1 flex-col gap-6 overflow-auto">
      <StatSection />
      <div className="scrollbar-thin flex min-h-80 flex-1 gap-5 overflow-x-auto">
        <IndexSyncListSection />
        <SyncHistorySection />
      </div>
    </div>
  );
}
