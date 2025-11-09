import React, { useState } from 'react';

const API_URL = '/api';

const ForgotPassword = ({ onBack }) => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  // 後端不再回 token；僅顯示提示訊息

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage('');
    try {
      const res = await fetch(`${API_URL}/auth/forgot-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });
      await res.json().catch(() => ({}));
      setMessage('If the email exists, a reset link has been sent.');
    } catch (err: any) {
      setMessage(err.message || 'Request failed');
    }
  };

  return (
    <div className="auth-container">
      <h2>Forgot Password</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        </div>
        <div className="form-group" style={{display:'flex', gap:8}}>
          <button type="submit">Send reset</button>
          <button type="button" className="btn-secondary" onClick={onBack}>Back</button>
        </div>
      </form>
      {message && <p className="message success">{message}</p>}
    </div>
  );
};

export default ForgotPassword;
