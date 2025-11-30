import logo from "./logo.svg";
import "./App.css";
import { useState } from "react";

function App() {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [tag,setTag]= useState("");

  const handleSave = () => {
    // 現在のState（title と content）をオブジェクトとしてコンソールに出力
    console.log("保存するメモ: ", { title, content,tag });
  };

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
      </header>
      {/* メモ帳のフォーム */}
      <div style={{ display: "flex", flexDirection: "column", padding: 20 }}>
        <label>タイトル: </label>
        <input
          type={"text"}
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          style={{ marginBottom: 10 }}
        />
        <label>内容: </label>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          style={{ marginBottom: 20, height: 80 }}
        />

         <label>タグ: </label>
        <input
          type={"text"}
          value={tag}
          onChange={(e) => setTag(e.target.value)}
          style={{ marginBottom: 10 }}
        />
        <button onClick={handleSave}>保存</button>
      </div>
    </div>
  );
}

export default App;