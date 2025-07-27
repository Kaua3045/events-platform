import http from 'k6/http';
import encoding from 'k6/encoding';

const BASE_URL = 'http://localhost:8081/api';
const CLIENT_ID = "default";
const CLIENT_SECRET = "default";

export async function authenticateApp() {
  const credentials = `${CLIENT_ID}:${CLIENT_SECRET}`;
  const encodedCredentials = encoding.b64encode(credentials);

  const headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Authorization': `Basic ${encodedCredentials}`,
  };

  const payload = `grant_type=client_credentials&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}`;

  const res = http.post(`${BASE_URL}/v1/authorize/token`, payload, { headers });

  const json = res.json();
  return json.access_token;
}

export async function createAuthCode({ email, password, challenge, appToken }) {
  const headers = {
    'Authorization': `Bearer ${appToken}`,
    'Content-Type': 'application/json',
  };

  const payload = JSON.stringify({
    client_id: CLIENT_ID,
    client_secret: CLIENT_SECRET,
    code_challenge: challenge,
    code_challenge_method: 'S256',
    email,
    password,
  });

  const res = http.post(`${BASE_URL}/v1/authorize/code`, payload, { headers });
  return res.json();
}

export async function exchangeTokenWithCode({ authCode, codeVerifier }) {
  const credentials = `${CLIENT_ID}:${CLIENT_SECRET}`;
  const encodedCredentials = encoding.b64encode(credentials);

  const headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Authorization': `Basic ${encodedCredentials}`,
  };

  const payload = `grant_type=authorization_code&client_id=${CLIENT_ID}&code=${authCode}&code_verifier=${codeVerifier}`;

  const res = http.post(`${BASE_URL}/v1/authorize/token`, payload, { headers });
  return res.json();
}
