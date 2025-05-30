import axios from "axios";

// 로그아웃 함수
export async function logoutAndRedirect() {
  try {
    await axiosIns.delete("/auth/logout");
  } catch (err) {
    alert("로그아웃 에러 발생. (서버 확인)");
    console.error(err);
  } finally {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("nickname");
    document.cookie =
      "refreshToken=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
    window.location.href = "/";
  }
}

export const axiosIns = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true, // 쿠키 전송. 이 설정이 있어야 서버에 요청할 때 헤더에 쿠키를 포함 (refreshToken이 전송됨)
});

// 요청 전: access token을 헤더에 포함하여 요청보냄
axiosIns.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");

  /* /auth/refresh 요청도 interceptors.request를 통해 access token을 보내면
  -> access token이 만료된 상태인데도 Authorization 헤더가 계속 붙음 
  -> 서버가 또 401 -> 무한 반복. */
  // refresh 요청에는 access token을 붙이지 않음
  if (!config.url.includes("/auth/refresh") && token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// 응답 후: access token 만료 시, 자동으로 refresh 시도
axiosIns.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // access token 만료로 401 발생 시 처리
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url.includes("/auth/refresh") // /auth/refresh 요청은 재시도 대상에서 제외
      // /auth/refresh가 실패해도 다시 /auth/refresh 요청을 계속해서 발생시키는 구조로 무한루프에 걸림
      // 리프레시 토큰이 만료되었거나 잘못된 토큰인 경우 로그아웃 유도해야하는데,
      // 계속해서 /auth/refresh를 요청하여 토큰 재발급을 하려고하면 안되기 때문에
    ) {
      originalRequest._retry = true;
      try {
        // refresh 요청 (HttpOnly 쿠키가 자동으로 전송됨)
        const res = await axiosIns.post("/auth/refresh");

        if (!res.data.success) {
          // success key값이 false라면
          console.log(res.data.message);
          if (
            res.data.message === "expired" ||
            res.data.message === "invalid"
          ) {
            logoutAndRedirect(); // 로그아웃 처리
            alert("재로그인 해주세요");
          }
          return Promise.reject(new Error("토큰 갱신 실패"));
        }

        // 토큰 재발급 성공 시 localStorage에 재발급 받은 newAccessToken으로 accessToken 덮어쓰기
        const newAccessToken = res.data.accessToken;
        localStorage.setItem("accessToken", newAccessToken);

        // 헤더에 새 토큰 넣고 원래 요청 재시도
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return axiosIns(originalRequest); // 재요청
      } catch (refreshError) {
        logoutAndRedirect();
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);
