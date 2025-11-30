import logo from "./logo.svg";
import "./App.css";
import Toggle from "./Toggle";



function App() {
  // 子コンポーネントから呼び出される関数を定義
  // newVisibility は、子コンポーネントから渡される新しい表示状態 (true or false)
  const handleToggle = (newVisibility) => {
    console.log("App.js が感知: 表示状態が " + newVisibility + " になりました");
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
          <Toggle onToggle={handleToggle} />
      </header>
    
      

    </div>
  );
}

export default App;