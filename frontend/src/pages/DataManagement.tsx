import { useState } from "react";
import DataManagementFilter from "@/components/pages/data-management/DataManagementFilter";
import DataManagementHeader from "@/components/pages/data-management/DataManagementHeader";
import DataManagementTable from "@/components/pages/data-management/DataManagementTable";

export interface Index {
  id: number;
  label: string;
}

export default function DataManagement() {
  const [selectedIndex, setSelectedIndex] = useState<Index | null>(null);

  const onIndexChange = (index: Index) => {
    setSelectedIndex(index);
  };

  return (
    <div className="border-secondary flex min-h-0 flex-1 flex-col overflow-auto rounded-xl border bg-white">
      <DataManagementHeader index={selectedIndex} />
      <DataManagementFilter onIndexChange={onIndexChange} />
      <DataManagementTable index={selectedIndex} />
    </div>
  );
}
