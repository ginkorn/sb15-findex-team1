import { Outlet } from "react-router-dom";
import Navigation from "@/components/layout/Navigation";
import Header from "../common/Header";
import { ModalContainer } from "../common/modals/ModalContainer";

export default function Layout() {
  return (
    <div className="text-primary scrollbar-thin h-screen bg-slate-50">
      <div className="mx-auto flex h-full min-h-0 max-w-350 flex-col">
        {/* 헤더바 */}
        <Navigation />

        {/* 메인 컨텐츠 영역 */}
        <main className="flex min-h-0 flex-1 flex-col overflow-hidden">
          <Header />
          <div className="mx-auto flex min-h-0 w-full flex-1 flex-col px-4 py-6">
            <Outlet />
          </div>
        </main>
      </div>

      {/* 전역 모달 */}
      <ModalContainer />
    </div>
  );
}
