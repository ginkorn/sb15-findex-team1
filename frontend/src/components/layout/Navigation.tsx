import { Link, NavLink } from "react-router-dom";
import { NAV_ITEMS } from "@/constants/navigation";
import { cx } from "@/utils/cx";
import Logo from "@/assets/logo.png";

export default function Navigation() {
  return (
    <nav className="flex justify-between py-6">
      <Link to="/" className="shrink-0">
        <img src={Logo} alt="logo" />
      </Link>

      <div className="flex items-center gap-5 px-3">
        {NAV_ITEMS.map((item) => {
          const Icon = item.icon;

          return (
            <NavLink
              key={item.id}
              to={item.path}
              className={({ isActive }) =>
                cx(
                  "text-tertiary shrink-0 rounded-full font-semibold",
                  "p-3 text-lg sm:px-6 sm:py-2",
                  "inline-flex items-center justify-center",
                  isActive
                    ? "bg-primary-solid text-white"
                    : "hover:bg-gray-200",
                )
              }
            >
              {/* 작은 화면: 아이콘 표시 / sm 이상: 숨김 */}
              <Icon className="block sm:hidden" />

              {/* 작은 화면: 숨김 / sm 이상: 라벨만 표시 */}
              <span className="hidden sm:block">{item.label}</span>
            </NavLink>
          );
        })}
      </div>
    </nav>
  );
}
