import { API, getToken, setToken, clearToken, isTokenExpired } from './api.js';

let currentUser = null;

export function isLoggedIn() {
  return !!getToken();
}

function decodeUser(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return { email: payload.sub ?? 'User' };
  } catch {
    return { email: 'User' };
  }
}

function updateHeaderUI() {
  const actions = document.getElementById('auth-actions');
  if (isLoggedIn() && currentUser) {
    actions.innerHTML = `
      <span class="auth-user">${currentUser.email}</span>
      <button class="auth-btn" id="logout-btn">Logout</button>
    `;
    document.getElementById('logout-btn').addEventListener('click', logout);
  } else {
    actions.innerHTML = `
      <button class="auth-btn" id="login-btn">Login</button>
      <button class="auth-btn auth-btn--accent" id="register-btn">Register</button>
    `;
    document.getElementById('login-btn').addEventListener('click', () => openModal('login'));
    document.getElementById('register-btn').addEventListener('click', () => openModal('register'));
  }
}

function openModal(tab = 'login') {
  document.getElementById('auth-modal').classList.add('open');
  switchTab(tab);
  document.getElementById('auth-error').textContent = '';
}

function closeModal() {
  document.getElementById('auth-modal').classList.remove('open');
  document.getElementById('auth-error').textContent = '';
}

function switchTab(tab) {
  document.querySelectorAll('.modal-tab').forEach(t =>
    t.classList.toggle('active', t.dataset.tab === tab)
  );
  document.getElementById('login-form').style.display = tab === 'login' ? 'flex' : 'none';
  document.getElementById('register-form').style.display = tab === 'register' ? 'flex' : 'none';
}

async function doLogin(email, password) {
  const res = await fetch(`${API}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) throw new Error('Invalid email or password');
  const token = await res.text();
  setToken(token);
  currentUser = decodeUser(token);
}

async function doRegister(username, email, password) {
  const res = await fetch(`${API}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userName: username, email, password }),
  });
  if (!res.ok) throw new Error('Registration failed');
  await doLogin(email, password);
}

function logout() {
  clearToken();
  currentUser = null;
  updateHeaderUI();
  document.dispatchEvent(new CustomEvent('auth:logout'));
}

export function initAuth() {
  const token = getToken();
  if (token) {
    if (isTokenExpired(token)) {
      clearToken();
    } else {
      currentUser = decodeUser(token);
    }
  }

  updateHeaderUI();

  document.addEventListener('auth:unauthorized', () => logout());

  document.getElementById('modal-close').addEventListener('click', closeModal);
  document.getElementById('auth-modal').addEventListener('click', (e) => {
    if (e.target.id === 'auth-modal') closeModal();
  });

  document.querySelectorAll('.modal-tab').forEach(tab => {
    tab.addEventListener('click', () => switchTab(tab.dataset.tab));
  });

  document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    try {
      await doLogin(email, password);
      closeModal();
      updateHeaderUI();
      document.dispatchEvent(new CustomEvent('auth:login'));
    } catch (err) {
      document.getElementById('auth-error').textContent = err.message;
    }
  });

  document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    try {
      await doRegister(username, email, password);
      closeModal();
      updateHeaderUI();
      document.dispatchEvent(new CustomEvent('auth:login'));
    } catch (err) {
      document.getElementById('auth-error').textContent = err.message;
    }
  });
}
