import { authenticateApp, createAuthCode, exchangeTokenWithCode } from './auth-helpers.js';
import { generateCodeVerifierAndChallenge } from './code-challenge.js';

export function performFullLogin(email, password) {
  const { verifier, challenge } = generateCodeVerifierAndChallenge();

  const appToken = authenticateApp();

  const codeResponse = createAuthCode({
    email,
    password,
    challenge,
    appToken,
  });

  if (!codeResponse.code) {
    throw new Error(`Erro ao gerar código: ${JSON.stringify(codeResponse)}`);
  }

  const tokenResponse = exchangeTokenWithCode({
    authCode: codeResponse.code,
    codeVerifier: verifier,
  });

  if (!tokenResponse.access_token) {
    throw new Error(`Erro ao obter token: ${JSON.stringify(tokenResponse)}`);
  }

  return tokenResponse.access_token;
}