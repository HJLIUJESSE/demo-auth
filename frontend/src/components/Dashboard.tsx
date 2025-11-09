import React, { useState } from 'react';
import Personal from './Personal';

const Placeholder = ({ title }) => (
  <div className="auth-container"><h2>{title}</h2><p>Coming soon...</p></div>
);

const Dashboard = ({ onLogout }) => {
  const [tab, setTab] = useState<'home' | 'chat' | 'personal'>('home');
  return (
    <div className="top-spacing">
      <div className="dashboard-nav" style={{display:'flex', gap:12, borderBottom:'1px solid #ddd', paddingBottom:8, marginBottom:12, alignItems:'center'}}>
        <button onClick={()=>setTab('home')}>首頁</button>
        <button onClick={()=>setTab('chat')}>群聊</button>
        <button onClick={()=>setTab('personal')} style={{marginLeft:'auto'}}>個人</button>
        <button onClick={onLogout}>登出</button>
      </div>
      {tab==='home' && <Placeholder title="首頁" />}
      {tab==='chat' && <Placeholder title="群聊" />}
      {tab==='personal' && <Personal />}
    </div>
  );
};

export default Dashboard;
