import { useState, useEffect } from 'react';
import './App.css';
import Login from './components/Login';
import Register from './components/Register';
import ForgotPassword from './components/ForgotPassword';
import ResetPassword from './components/ResetPassword';
import Dashboard from './components/Dashboard';
import { logout } from './services/auth';


function App() {
  const [currentUser, setCurrentUser] = useState(false);
  const [mode, setMode] = useState<'login'|'register'|'forgot'|'reset'>('login');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setCurrentUser(true);
    }
    setIsLoading(false);
  }, []);

  const handleLogin = () => {
    setCurrentUser(true);
  };

  const handleLogout = () => {
    logout();
    setCurrentUser(false);
  };

  // 支援直接開啟 /reset-password?token=
  if (isLoading) {
      return <div>Loading...</div>
  }

  // 簡易路由
  const path = window.location.pathname;
  if (path.startsWith('/reset-password')) {
    return <ResetPassword onBackToLogin={() => { window.history.pushState({}, '', '/'); setMode('login'); }} />
  }

  return (
    <div>
      <h1>My Auth App</h1>
      {currentUser ? (
        <Dashboard onLogout={handleLogout} />
      ) : (
        mode==='login' ? (
          <Login onLoginSuccess={handleLogin} onSwitch={() => setMode('register')} onForgot={() => setMode('forgot')} />
        ) : mode==='register' ? (
          <Register onSwitch={() => setMode('login')} />
        ) : (
          <ForgotPassword onBack={() => setMode('login')} />
        )
      )}
    </div>
  );
}

export default App;
