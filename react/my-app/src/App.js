import logo from './logo.svg';
import { useState } from "react";
import './App.css';

function App() {
  const [isVisible, setIsVisible] = useState(true);
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" 
        onClick={() => console.log('ロゴがクリックされました')}
        style={{ cursor: 'pointer' }}/>
        <p>
          Welcome to react!
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
     
      <button onClick={() => setIsVisible(!isVisible)}>
  Toggle Text
</button>
{isVisible && <p>このテキストが表示/非表示されます</p>}
 </header>
    </div>
    
    
  );
}

export default App;
