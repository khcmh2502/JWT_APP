import React, { useEffect, useState } from "react";
import { axiosIns, logoutAndRedirect } from "../api/axios";
import { useNavigate } from "react-router-dom";
/*
[Access Token 만료 시]
클라이언트가 서버로 요청을 보냈는데 Access Token이 만료되었다면:
서버는 만료된 Access Token에서 유저 정보를 추출하고,
DB에 저장된 Refresh Token을 조회해서 유효하면 새로운 Access Token을 발급해서 반환.

[Refresh Token도 만료되었거나 없으면]
서버는 401 Unauthorized로 응답하며, 클라이언트는 로그인 페이지로 리다이렉트.
 */
function MainPage() {
  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [member, setMember] = useState(localStorage.getItem("nickname") || "");
  // || 연산자 : 좌변이 false일 경우 우변을 반환
  // -> localStorage.getItem("nickname")이 null이면 빈문자열 세팅
  const navigate = useNavigate();

  // 상태변경 함수
  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((prevForm) => ({
      ...prevForm,
      [name]: value,
    }));
  };

  // 로그인 함수
  const handleLogin = async (e) => {
    e.preventDefault();

    const { email, password } = form;

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

  if (member) {
    return (
      <div className="main-container">
        <h2>{member}님, 안녕하세요!</h2>
        <section className="btn-section">
          <button onClick={() => navigate("/myPage")}>마이페이지</button>
          <button onClick={logoutAndRedirect}>로그아웃</button>
        </section>
      </div>
    );
  } else {
    return <Login handleLogin={handleLogin} handleChange={handleChange} />;
  }
}

const Login = ({ handleLogin, handleChange }) => {
  return (
    <div className="login-container">
      <h2>Sign in</h2>
      <form onSubmit={handleLogin}>
        <input
          name="email"
          type="email"
          placeholder="이메일"
          onChange={handleChange}
          required
        />
        <input
          name="password"
          type="password"
          placeholder="비밀번호"
          onChange={handleChange}
          required
        />
        <button type="submit">로그인</button>
      </form>
    </div>
  );
};

export default MainPage;
