import React from 'react';


interface StartPageProps {
  onStartChat: () => void;
}

const StartPage: React.FC<StartPageProps> = ({ onStartChat }) => {
  return (
    <div className="page-container" style={{ textAlign: 'center', marginTop: '50px' }}>
      <h1>早苗さんの政策について学ぼう！</h1>
      <p>任意のお問い合わせに対して早苗さんの考えを返答します。</p>
      
      <button onClick={onStartChat} style={{ padding: '10px 20px', marginTop: '20px' }}>
        チャットを始める
      </button>
    </div>
  );
};

export default StartPage;