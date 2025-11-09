import React, { useEffect, useState } from 'react';

const API_URL = '/api';

const tokenHeader = () => {
  const t = localStorage.getItem('token');
  return t ? { 'Authorization': `Bearer ${t}` } : {};
};

const Personal = () => {
  const [profile, setProfile] = useState<any>(null);
  const [birthDate, setBirthDate] = useState('');
  const [msg, setMsg] = useState('');
  const [uploading, setUploading] = useState(false);

  const load = async () => {
    try {
      const res = await fetch(`${API_URL}/profile`, { headers: { ...tokenHeader() } });
      if (!res.ok) throw new Error('Load failed');
      const data = await res.json();
      setProfile(data);
      setBirthDate(data.birthDate ? String(data.birthDate) : '');
    } catch (e: any) {
      setMsg(e.message || 'Load failed');
    }
  };

  useEffect(() => { load(); }, []);

  const saveBirthDate = async () => {
    setMsg('');
    try {
      const res = await fetch(`${API_URL}/profile`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', ...tokenHeader() },
        body: JSON.stringify({ birthDate })
      });
      if (!res.ok) throw new Error('Save failed');
      setMsg('Updated');
      await load();
    } catch (e: any) {
      setMsg(e.message || 'Save failed');
    }
  };

  const onFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    if (!f) return;
    setUploading(true);
    setMsg('');
    try {
      const fd = new FormData();
      fd.append('file', f);
      const res = await fetch(`${API_URL}/profile/avatar`, { method:'POST', headers: { ...tokenHeader() }, body: fd });
      const data = await res.json();
      if (!res.ok) throw new Error(data?.message || 'Upload failed');
      setMsg('Avatar uploaded');
      await load();
    } catch (e: any) {
      setMsg(e.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>個人頁面</h2>
      {msg && <p className="message">{msg}</p>}
      {profile && (
        <div className="profile-container">
          <div className="profile-avatar">
            <img src={profile.avatarUrl || 'data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2280%22 height=%2280%22><rect width=%2280%22 height=%2280%22 fill=%22%23eee%22/><text x=%2240%22 y=%2246%22 text-anchor=%22middle%22 font-size=%2212%22 fill=%22%23999%22>no avatar</text></svg>'} alt="avatar" />
            <input type="file" accept="image/*" onChange={onFileChange} disabled={uploading} />
          </div>
          <div className="profile-fields">
            <div className="form-group">
              <label>生日</label>
              <div style={{display:'flex', gap:8}}>
                <input type="date" value={birthDate || ''} onChange={e=>setBirthDate(e.target.value)} />
                <button type="button" className="btn-secondary" onClick={saveBirthDate}>儲存</button>
              </div>
            </div>
            <div className="form-group">
              <label>帳號</label>
              <div>{profile.username}</div>
            </div>
            <div className="form-group">
              <label>Email</label>
              <div>{profile.email}</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Personal;
