import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "pretendard/dist/web/static/pretendard.css";
import "@/styles/index.css";
import {
  createHashRouter,
  Navigate,
  RouterProvider,
} from "react-router-dom";
import Layout from "@/components/layout/Layout";
import { ToastContainer } from "./components/common/toast/ToastContainer";
import Dashboard from "./pages/Dashboard";
import DataManagement from "./pages/DataManagement";
import IndexManagement from "./pages/IndexManagement";
import Integrations from "./pages/Integrations";


const router = createHashRouter([
  {
    path: "/",
    element: <Layout />, // 모든 페이지에 공통으로 적용될 레이아웃
    // errorElement: <ErrorPage />,
    children: [
      {
        path: "dashboard",
        element: <Dashboard />,
      },
      {
        path: "index-management",
        element: <IndexManagement />,
      },
      {
        path: "data-management",
        element: <DataManagement />,
      },
      {
        path: "integrations",
        element: <Integrations />,
      },
      {
        index: true, // App의 자식 경로 중 기본 경로를 설정
        element: <Navigate to="/dashboard" replace />,
      },
    ],
  },
]);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <RouterProvider router={router} />
    <ToastContainer />
  </StrictMode>,
);
