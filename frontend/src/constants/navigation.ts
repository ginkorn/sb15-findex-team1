import { BarChart02, LayoutAlt01, Link03, Server02 } from "@untitledui/icons";

export const NAV_ITEMS = [
  {
    id: 1,
    label: "대시보드",
    path: "/dashboard",
    icon: LayoutAlt01,
  },
  {
    id: 2,
    label: "지수 관리",
    path: "/index-management",
    icon: BarChart02,
  },
  {
    id: 3,
    label: "데이터 관리",
    path: "/data-management",
    icon: Server02,
  },
  {
    id: 4,
    label: "연동 관리",
    path: "/integrations",
    icon: Link03,
  },
];
