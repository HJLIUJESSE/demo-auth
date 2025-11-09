import React, { useState } from 'react';
import { login } from '../services/auth';

const Login = ({ onLoginSuccess, onSwitch, onForgot }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');

  const handleLogin = (e) => {
    e.preventDefault();
    setMessage('');

    login(username, password).then(
      () => {
        onLoginSuccess();
      },
      (error) => {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        setMessage(resMessage);
      }
    );
  };

  return (
    <div className="auth-container">
      <h2>Login</h2>
      <form onSubmit={handleLogin}>
        <div className="form-group">
          <label htmlFor="username">Username</label>
          <input
            type="text"
            id="username"
            name="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            type="password"
            id="password"
            name="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <button type="submit">Login</button>
        </div>
      </form>
      {message && <p className="message error">{message}</p>}
      <p>
        <a href="#" onClick={onForgot}>Forgot password?</a>
        {' '}| Don't have an account? <a href="#" onClick={onSwitch}>Register</a>
      </p>
    </div>
  );
};

export default Login;
