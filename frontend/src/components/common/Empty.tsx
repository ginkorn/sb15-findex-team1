import React from "react";

interface EmptyProps {
  message: string;
  button?: React.ReactNode;
}

/**
 * Empty 컴포넌트
 *
 * 데이터가 없을 때 표시하는 공통 Empty State 컴포넌트입니다.
 * 중앙 정렬된 메시지와 선택적 액션 버튼을 표시합니다.
 *
 * @param message - 표시할 안내 메시지 (필수)
 * @param button - 표시할 버튼 컴포넌트 (선택)
 *
 */
export const Empty: React.FC<EmptyProps> = ({ message, button }) => {
  return (
    <div className="flex flex-col items-center justify-center px-4 py-12">
      <div className="space-y-3 text-center">
        <p className="text-quaternary text-sm font-semibold">{message}</p>
        {button && <div>{button}</div>}
      </div>
    </div>
  );
};
