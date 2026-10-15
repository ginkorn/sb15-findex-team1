// 지수 관리 목록 즐겨찾기 여부 필터
export const FavoriteOptions: Record<string, string> = {
  all: "전체보기",
  favoritesOnly: "즐겨찾기만 보기",
  excludeFavorites: "즐겨찾기 제외",
};

export const convertViewKeyToFavoriteBoolean = (
  key: string | null,
): boolean | undefined => {
  switch (key) {
    case "favoritesOnly":
      return true;
    case "excludeFavorites":
      return false;
    case "all":
    default:
      return undefined;
  }
};
