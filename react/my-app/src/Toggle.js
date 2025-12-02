import { useState } from "react";

// 関数の引数として (props) を受け取る
const Toggle = (props) => {
  const [isVisible, setIsVisible] = useState(true);

  // クリック時の処理を新しい関数（handleClick）にまとめる
  const handleClick = () => {
    const newVisibility = !isVisible;
    setIsVisible(newVisibility); // 自分のStateを更新

    // 親から渡された props.onToggle があれば実行する
    if (props.onToggle) {
      // 新しい表示状態 (newVisibility) を引数として渡す
      props.onToggle(newVisibility);
    }
  };

  return (
    <div style={{ padding: "20px", border: "1px solid #61dafb" }}>
      {/* onClick で新しい handleClick 関数を呼び出す */}
      <button onClick={handleClick}>
        Toggle Text
      </button>

      {isVisible && <p>このテキストが表示/非表示されます</p>}
    </div>
  );
};

export default Toggle;