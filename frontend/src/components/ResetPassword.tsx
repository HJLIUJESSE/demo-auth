import React, { useMemo, useState } from 'react';

const API_URL = '/api';

const useQuery = () => {
  return useMemo(() => new URLSearchParams(window.location.search), []);
}

const ResetPassword = ({ onBackToLogin }) => {
  const qs = useQuery();
  const token = qs.get('token') || '';
  const [password, setPassword] = useState('');
  const [msg, setMsg] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsg('');
    try {
      const res = await fetch(`${API_URL}/auth/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword: password })
      });
      const text = await res.text();
      if (res.ok) {
        setMsg('Password reset OK. You can login now.');
      } else {
        setMsg(text || 'Reset failed');
      }
    } catch (err: any) {
      setMsg(err.message || 'Reset failed');
    }
  };

  const back = () => { try { onBackToLogin?.(); } finally { window.location.assign('/'); } };

  if (!token) {
    return (
      <div className="auth-container">
        <h2>Reset Password</h2>
        <p className="message">請從「重設密碼」的郵件連結開啟此頁。</p>
        <div className="form-group">
          <button type="button" onClick={back}>Back to Login</button>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-container">
      <h2>Reset Password</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>New Password</label>
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        </div>
        <div className="form-group" style={{display:'flex', gap:8}}>
          <button type="submit">Reset</button>
          <button type="button" className="btn-secondary" onClick={back}>Back to Login</button>
        </div>
      </form>
      {msg && <p className="message">{msg}</p>}
    </div>
  );
}

export default ResetPassword;
