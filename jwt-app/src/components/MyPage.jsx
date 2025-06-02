import { useEffect, useState } from "react";
import { axiosIns, logoutAndRedirect } from "../api/axios";
import { Link } from "react-router-dom";
import LoadingSpinner from "./Loading";

function MyPage() {
  const [member, setMember] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // 내정보 조회
  const fetchMemberInfo = async () => {
    try {
      const resp = await axiosIns.get("/auth/getMemberInfo");
      if (resp.status === 200) {
        setMember(resp.data);
      }
    } catch (error) {
      console.log("회원 정보 조회 실패:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchMemberInfo();
  }, []);

  if (isLoading) {
    return <LoadingSpinner />;
  } else {
    return (
      <div className="main-container">
        <h2>마이페이지</h2>
        <ul>
          <li>이메일: {member.email}</li>
          <li>닉네임: {member.name}</li>
        </ul>

        <section className="link-section">
          <Link to="/myPage/editPw">비밀번호 수정</Link>
        </section>
      </div>
    );
  }
}

export function MyPagePw() {
  const [form, setForm] = useState({
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const { password, confirmPassword } = form;
    if (password !== confirmPassword) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    // + 정규식 검사 있으면 좋음
    try {
      const resp = await axiosIns.put("/auth/changePw", form);
      if (resp.status === 200) {
        alert("비밀번호가 변경되었습니다. 재로그인 바랍니다.");
        logoutAndRedirect();
      }
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className="mypage-edit-container">
      <form className="mypage-edit-form" onSubmit={handleSubmit}>
        <h2>비밀번호 수정</h2>

        <div className="form-group">
          <label htmlFor="password">새 비밀번호</label>
          <input
            type="password"
            id="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            placeholder="새 비밀번호"
          />
        </div>

        <div className="form-group">
          <label htmlFor="confirmPassword">비밀번호 확인</label>
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            value={form.confirmPassword}
            onChange={handleChange}
            placeholder="비밀번호 확인"
          />
        </div>

        {error && <div className="error-message">{error}</div>}

        <button type="submit" className="submit-button">
          저장
        </button>
      </form>
    </div>
  );
}
export default MyPage;
