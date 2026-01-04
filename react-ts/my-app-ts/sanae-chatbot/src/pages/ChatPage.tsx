// 受け取るデータのルール（型）を定義
import { useState } from 'react';
type Props = {
  onBack: () => void;
};

interface Message{
    text:string;
    sender:string;

}

const ChatPage = (props: Props) => {
  const [messages, setMessages] = useState<Message[]>([]);

const [inputText, setInputText] = useState<string>("");
const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // (1) 新しい手紙を作る
    const newMessage: Message = {
      text: inputText, 
      sender: "user"   
    };
    const botMessage: Message = {
      text: "高市さんは日本の総理大臣です。",
      sender: "bot"
    }
    setMessages([...messages, newMessage, botMessage]);

    setInputText("");
  };

// ChatPage.tsx の return 部分
return (
  <div className="chat-container"> 
  

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
    </div>


    <div className="chat-input-area">
  <form onSubmit={handleSubmit}>

  <input type="text" placeholder="何か入力してね"
  value={inputText}
  onChange={(e) => setInputText(e.target.value)}/>
  
  <button type="submit">送信してね</button>
</form>
    </div>
    
  </div>
);
};

export default ChatPage;