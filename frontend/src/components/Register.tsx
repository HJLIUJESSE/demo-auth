import React, { useState } from 'react';
import { register } from '../services/auth';

const Register = ({ onSwitch }) => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();
    setMessage('');
    setIsSuccess(false);

    try {
      const response = await register(username, email, password);
      const text = await response.text();
      if (response.ok) {
        setMessage(text + ". You can now log in.");
        setIsSuccess(true);
      } else {
        let errorMsg = text;
        try {
            const errorJson = JSON.parse(text);
            if(errorJson.message) {
                errorMsg = errorJson.message;
            }
        } catch (e) {
            // Not a json, use text as is
        }
        throw new Error(errorMsg || 'Registration failed');
      }
    } catch (error) {
      const resMessage = error.message || error.toString();
      setMessage(resMessage);
      setIsSuccess(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Register</h2>
      <form onSubmit={handleRegister}>
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
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            name="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
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
          <button type="submit">Register</button>
        </div>
      </form>
      {message && <p className={`message ${isSuccess ? 'success' : 'error'}`}>{message}</p>}
      <p>
        Already have an account? <a href="#" onClick={onSwitch}>Login</a>
      </p>
    </div>
  );
};

export default Register;