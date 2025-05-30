import { useEffect, useState } from "react";
import { axiosIns } from "../api/axios";

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
    return <h1>Loading...</h1>;
  } else {
    return (
      <div>
        <h1>MyPage</h1>
        <h2>내 정보</h2>
        <p>이메일: {member.email}</p>
        <p>닉네임: {member.name}</p>
      </div>
    );
  }
}
export default MyPage;
