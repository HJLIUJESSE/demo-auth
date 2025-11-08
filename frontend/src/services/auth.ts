const API_URL = '/api';

export const register = (username, email, password) => {
  return fetch(`${API_URL}/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      username,
      email,
      password,
    }),
  });
};

export const login = (username, password) => {
  return fetch(`${API_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      username,
      password,
    }),
  })
    .then(response => {
      if (response.ok) {
        return response.json();
      }
      throw new Error('Login failed');
    })
    .then(data => {
      if (data.token) {
        localStorage.setItem('token', data.token);
      }
      return data;
    });
};

export const logout = () => {
  localStorage.removeItem('token');
};

export const getMe = () => {
    const token = localStorage.getItem('token');
    if (!token) {
        return Promise.reject("No token found");
    }
    return fetch(`${API_URL}/me`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    }).then(res => {
        if(res.ok) return res.text();
        throw new Error("Could not fetch user profile");
    })
}
