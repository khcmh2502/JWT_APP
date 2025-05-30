import React, { useEffect, useState } from "react";
import { axiosIns, logoutAndRedirect } from "./../api/axios";
import { useNavigate } from "react-router-dom";
/*
[Access Token 만료 시]
클라이언트가 서버로 요청을 보냈는데 Access Token이 만료되었다면:
서버는 만료된 Access Token에서 유저 정보를 추출하고,
DB에 저장된 Refresh Token을 조회해서 유효하면 새로운 Access Token을 발급해서 반환.

[Refresh Token도 만료되었거나 없으면]
서버는 401 Unauthorized로 응답하며, 클라이언트는 로그인 페이지로 리다이렉트.
 */
function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [member, setMember] = useState(localStorage.getItem("nickname") || "");
  // || 연산자 : 좌변이 false일 경우 우변을 반환
  // -> localStorage.getItem("nickname")이 null이면 빈문자열 세팅
  const navigate = useNavigate();

  // 로그인 함수
  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      const response = await axiosIns.post("/auth/login", { email, password });
      const { accessToken, nickname } = response.data;

      // Access Token은 localStorage에 저장해서 요청 헤더에서 사용하고,
      // Refresh Token은 HttpOnly 쿠키로 보관되어 브라우저가 자동으로 서버에 전송하게끔 함.
      // Access Token과 nickname을 localStorage에 저장
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("nickname", nickname);
      setMember(nickname); // 상태에도 닉네임저장
    } catch (err) {
      alert("아이디 또는 비밀번호가 일치하지 않습니다");
      console.log(err);
    }
  };

  // 로그아웃
  // const handleLogout = async () => {
  //   try {
  //     const resp = await axiosIns.get("/auth/logout");

  //     if (resp.status === 200) {
  //       localStorage.removeItem("accessToken");
  //       localStorage.removeItem("nickname");
  //       setMember(null);
  //     }
  //   } catch (error) {
  //     console.log("로그아웃 중 에러 발생 : ", error);
  //   }
  // };

  if (member) {
    return (
      <div>
        <h1>메인페이지</h1>
        <h2>{member}님 환영합니다</h2>
        <div>
          <button onClick={() => navigate("/myPage")}>마이페이지</button>
          <button onClick={logoutAndRedirect}>로그아웃</button>
        </div>
      </div>
    );
  } else {
    return (
      <div style={{ maxWidth: "300px", margin: "100px auto" }}>
        <h2>로그인</h2>
        <form onSubmit={handleLogin}>
          <input
            type="email"
            placeholder="이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <br />
          <br />
          <input
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <br />
          <br />
          <button type="submit">로그인</button>
        </form>
      </div>
    );
  }
}

export default LoginPage;
