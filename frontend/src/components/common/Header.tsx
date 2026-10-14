import type { FC } from "react";
import { useLocation } from "react-router-dom";
import { NAV_ITEMS } from "@/constants/navigation";

interface NavigationItem {
  label: string;
  path: string;
  icon: FC<{ className?: string }>;
}

const Header = () => {
  const { pathname } = useLocation();

  const currentMenu: NavigationItem | undefined = NAV_ITEMS.find((item) =>
    pathname.startsWith(item.path),
  );

  const IconComponent = currentMenu?.icon;

  return (
    <header className="mt-10 flex items-center gap-3 pl-5">
      {IconComponent && <IconComponent className="size-8" aria-hidden="true" />}
      <h1 className="text-display-xs font-bold">
        {currentMenu?.label || "Findex"}
      </h1>
    </header>
  );
};

export default Header;
