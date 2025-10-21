import { useState, useEffect } from 'react';
import './App.css';
import Login from './components/Login';
import Register from './components/Register';
import Profile from './components/Profile';
import { logout } from './services/auth';


function App() {
  const [currentUser, setCurrentUser] = useState(false);
  const [showLogin, setShowLogin] = useState(true);
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

  if (isLoading) {
      return <div>Loading...</div>
  }

  return (
    <div>
      <h1>My Auth App</h1>
      {currentUser ? (
        <Profile onLogout={handleLogout} />
      ) : (
        showLogin ? 
        <Login onLoginSuccess={handleLogin} onSwitch={() => setShowLogin(false)} /> : 
        <Register onSwitch={() => setShowLogin(true)} />
      )}
    </div>
  );
}

export default App;