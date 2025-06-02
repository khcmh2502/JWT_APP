import { BrowserRouter, Route, Routes } from "react-router-dom";
import "./App.css";
import LoginPage from "./components/MainPage";
import MyPage, { MyPagePw } from "./components/MyPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/myPage" element={<MyPage />} />
        <Route path="/myPage/editPw" element={<MyPagePw />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
