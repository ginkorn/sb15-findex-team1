// import type { EmployeeTrendUnit } from "@/model/employee";

// export const formatXAxisTick = (
//   value: string,
//   index: number,
//   unit: EmployeeTrendUnit,
// ): string => {
//   const d = new Date(value);
//   if (Number.isNaN(d.getTime())) return value;

//   switch (unit) {
//     case "day": {
//       // 예: 12.03
//       return d.toLocaleDateString("ko-KR", {
//         month: "2-digit",
//         day: "2-digit",
//       });
//     }

//     case "week": {
//       // 예: 12.03 (주 시작일 기준)
//       return d.toLocaleDateString("ko-KR", {
//         month: "2-digit",
//         day: "2-digit",
//       });
//     }

//     case "month": {
//       if (index === 0) {
//         // 예: 2024. 12월
//         return d.toLocaleDateString("ko-KR", {
//           year: "numeric",
//           month: "short",
//         });
//       }

//       // 예: 1월, 2월 ...
//       return d.toLocaleDateString("ko-KR", { month: "short" });
//     }

//     case "quarter": {
//       const year = d.getFullYear();
//       const quarter = Math.floor(d.getMonth() / 3) + 1;
//       // 예: 2024 Q1
//       return `${year} Q${quarter}`;
//     }

//     case "year": {
//       // 예: 2024
//       return `${d.getFullYear()}`;
//     }

//     default:
//       return value;
//   }
// };

// export const formatTooltipLabel = (
//   value: string,
//   unit: EmployeeTrendUnit,
// ): string => {
//   const d = new Date(value);
//   if (Number.isNaN(d.getTime())) return value;

//   switch (unit) {
//     case "day":
//       // 예: 12월 9일
//       return d.toLocaleDateString("ko-KR", {
//         month: "long",
//         day: "numeric",
//       });

//     case "week":
//       return d.toLocaleDateString("ko-KR", {
//         month: "long",
//         day: "numeric",
//       });

//     case "month":
//       // 예: 2024년 12월
//       return d.toLocaleDateString("ko-KR", {
//         year: "numeric",
//         month: "long",
//       });

//     case "quarter": {
//       const year = d.getFullYear();
//       const quarter = Math.floor(d.getMonth() / 3) + 1;
//       // 예: 2024년 1분기
//       return `${year}년 ${quarter}분기`;
//     }

//     case "year":
//       // 예: 2024년
//       return `${d.getFullYear()}년`;

//     default:
//       return value;
//   }
// };
