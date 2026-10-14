import axios, { type AxiosInstance, type AxiosRequestConfig } from "axios";


// API 클라이언트 설정
const API_BASE_URL = "/api";


/**
 * axios 인스턴스
 * - baseURL만 설정한 아주 얇은 래퍼
 */
const axiosInstance: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    Accept: "application/json",
    "Content-Type": "application/json",
  },
});

const axiosMultipartInstance: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    Accept: "application/json",
    "Content-Type": undefined,
  },
});

/**
 * 공통 GET 메서드
 *
 * @param url - "/api/employees" 처럼 HR Bank 기준 상대 경로
 * @param params - 쿼리스트링으로 전송할 객체 (예: { nameOrEmail: "홍" })
 *
 * 제네릭 T는 응답 데이터의 타입 (예: CursorPageResponse<EmployeeDto>)
 */
async function get<T>(url: string, params?: unknown): Promise<T> {
  const response = await axiosInstance.get<T>(url, { params });
  return response.data;
}

/**
 * 공통 POST 메서드
 *
 * @param url - 요청 URL
 * @param data - JSON 객체 또는 FormData
 * @param config - 추가 axios 설정 (headers 등)
 */
async function post<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> {
  const response = await axiosInstance.post<T>(url, data, config);
  return response.data;
}

async function multiPartPost<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> {
  const response = await axiosMultipartInstance.post<T>(url, data, config);
  return response.data;
}

/**
 * 공통 PATCH 메서드
 */
async function patch<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> {
  const response = await axiosInstance.patch<T>(url, data, config);
  return response.data;
}

async function multiPartPatch<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> {
  const response = await axiosMultipartInstance.patch<T>(url, data, config);
  return response.data;
}

/**
 * 공통 PUT 메서드
 */
async function put<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> {
  const response = await axiosInstance.put<T>(url, data, config);
  return response.data;
}

/**
 * 공통 DELETE 메서드
 *
 * @param url - 삭제할 리소스 URL
 * @param config - query 등을 함께 보내고 싶을 때 사용
 */
async function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await axiosInstance.delete<T>(url, config);
  return response.data;
}

/**
 * 도메인별 api 파일에서 사용할 클라이언트
 *
 * 사용 예시:
 * - apiClient.get<CursorPageResponse<EmployeeDto>>("/api/employees", query)
 * - apiClient.post<EmployeeDto>("/api/employees", formData)
 */
const apiClient = {
  get,
  post,
  multiPartPost,
  patch,
  multiPartPatch,
  put,
  delete: del,
};

export default apiClient;
