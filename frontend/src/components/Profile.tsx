import React, { useState, useEffect } from 'react';
import { getMe } from '../services/auth';

const Profile = ({ onLogout }) => {
  const [username, setUsername] = useState('');
  const [message, setMessage] = useState('Loading profile...');

  useEffect(() => {
    getMe().then(
      (response) => {
        setUsername(response);
        setMessage('');
      },
      (error) => {
        setMessage(error.message || error.toString());
        if (error.message.includes("401") || error.message.includes("403")) {
            onLogout(); // Token is invalid/expired, so log out
        }
      }
    );
  }, []);

  const handleLogout = () => {
    onLogout();
  };

  return (
    <div className="auth-container">
      <h2>Profile</h2>
      {message && <p className="message error">{message}</p>}
      {username && <p>Welcome, <strong>{username}</strong>!</p>}
      <div className="form-group">
        <button onClick={handleLogout}>Logout</button>
      </div>
    </div>
  );
};

export default Profile;