import { useState } from 'react';
import StartPage from './pages/StartPage';
import ChatPage from './pages/ChatPage';


type Page = 'start' | 'chat';

const App = () => {

  const [currentPage, setCurrentPage] = useState<Page>('start');

 return (
    <div style={{ padding: '20px' }}>
      {currentPage === 'start' ? (

        <StartPage onStartChat={() => setCurrentPage('chat')} />
      ) : (

        <ChatPage onBack={() => setCurrentPage('start')} />
      )}
    </div>
  );
};

export default App;