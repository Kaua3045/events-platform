import { authenticateApp, createAuthCode, exchangeTokenWithCode } from './auth-helpers.js';
import { generateCodeVerifierAndChallenge } from './code-challenge.js';

export async function performFullLogin(email, password) {
  const { verifier, challenge } = generateCodeVerifierAndChallenge();

  const appToken = await authenticateApp();

  const codeResponse = await createAuthCode({
    email,
    password,
    challenge,
    appToken,
  });

  console.log(`Código de autorização gerado: ${JSON.stringify(codeResponse)}`);

  if (!codeResponse.code) {
    throw new Error(`Erro ao gerar código: ${JSON.stringify(codeResponse)}`);
  }

  const tokenResponse = await exchangeTokenWithCode({
    authCode: codeResponse.code,
    codeVerifier: verifier,
  });

  if (!tokenResponse.access_token) {
    throw new Error(`Erro ao obter token: ${JSON.stringify(tokenResponse)}`);
  }

  return tokenResponse.access_token;
}