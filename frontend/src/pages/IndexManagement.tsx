import IndexFilterSection from "@/components/pages/index-management/IndexManagementFilter";
import IndexHeader from "@/components/pages/index-management/IndexManagementHeader";
import IndexTable from "@/components/pages/index-management/IndexManagementTable";

const IndexManagement = () => {
  return (
    <div className="border-secondary flex h-full min-h-0 flex-col rounded-xl border bg-white shadow-xs">
      <IndexHeader />
      <IndexFilterSection />
      <IndexTable />
    </div>
  );
};

export default IndexManagement;
