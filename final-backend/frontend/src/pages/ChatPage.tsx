import React, { useState } from 'react';

// --- 型定義 ---
type Props = {
  onBack: () => void;
};

interface Message {
  text: string;
  sender: string;
}

interface APIResponse {
  reply: string;
  
}

// --- コンポーネント本体 ---
const ChatPage = (props: Props) => {
  // 変数（State）の定義
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState<string>("");
  const [isLoading, setIsLoading] = useState(false);

  const API_URL = "https://finalbackend-konishi-fsdqc8gkgrevc4bh.japanwest-01.azurewebsites.net";

  // 送信ボタンを押したときの関数
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText) return;

    // ユーザーのメッセージを追加
    const newMessage: Message = { text: inputText, sender: "user" };
    const newHistory = [...messages, newMessage];
    setMessages(newHistory);

    // 入力欄クリアとローディング開始
    const messageToSend = inputText;
    setInputText("");
    setIsLoading(true);

    try {
      // バックエンドへの送信 (ポート8000)
      const response = await fetch(`${API_URL}/chat`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: messageToSend }),
      });
      if (!response.ok) throw new Error("API error");

      // AIの返答を追加
      const data: APIResponse = await response.json();
      const botMessage: Message = { text: data.reply, sender: "bot" };
      setMessages([...newHistory, botMessage]);

    } catch (error) {
      console.error(error);
      setMessages([...newHistory, { text: "エラーが発生しました", sender: "bot" }]);
    } finally {
      setIsLoading(false);
    }
  };

  // 画面の表示 (return)
  return (
    <div className="chat-container">
      {/* 戻るボタン */}
      <div style={{ padding: '10px' }}>
        <button onClick={props.onBack}>TOPに戻る</button>
      </div>

      {/* チャットログ表示エリア */}
      <div className="chat-display-area">
        {messages.map((message, index) => (
          <div
            key={index}
            className={message.sender === "user" ? "message-right" : "message-left"}
          >
            <div className="message-bubble">
              {message.text}
            </div>
          </div>
        ))}
        {/* ローディング表示 */}
        {isLoading && (
          <div className="message-left">
            <div className="message-bubble">...回答作成中...</div>
          </div>
        )}
      </div>

      {/* 入力エリア */}
      <div className="chat-input-area">
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="質問を入力してください。"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            disabled={isLoading}
          />
          <button type="submit" disabled={isLoading}>送信</button>
        </form>
      </div>
    </div>
  );
};

export default ChatPage;