// import { sha256 } from 'k6/crypto';
// import encoding from 'k6/encoding';

// export function generateCodeVerifierAndChallenge() {
//   const verifier = generateRandomString();
//   const challenge = generateCodeChallenge(verifier);
//   return { verifier, challenge };
// }

// function generateRandomString(length = 64) {
//   const charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
//   let result = "";
//   const randomValues = new Uint8Array(length);
//   for (let i = 0; i < length; i++) {
//     result += charset[randomValues[i] % charset.length];
//   }
//   return result;
// }

// function generateCodeChallenge(verifier) {
//   const hashed = sha256(verifier, 'binary'); // k6's crypto.sha256
//   return base64UrlEncode(hashed);
// }

// function base64UrlEncode(buffer) {
//   return encoding.b64encode(buffer, 'rawurl'); // base64url sem padding
// }

import { sha256 } from 'k6/crypto';
import encoding from 'k6/encoding';

// Gera UUID v4-like para usar como verifier (36 caracteres, incluindo hífens)
export function generateUUID() {
  const randomValues = new Uint8Array(16);
  crypto.getRandomValues(randomValues);

  // Ajustes para v4 UUID conforme RFC 4122
  randomValues[6] = (randomValues[6] & 0x0f) | 0x40; // versão 4
  randomValues[8] = (randomValues[8] & 0x3f) | 0x80; // variante 10

  const byteToHex = [];
  for (let i = 0; i < 256; ++i) {
    byteToHex[i] = (i + 0x100).toString(16).substr(1);
  }

  return (
    byteToHex[randomValues[0]] +
    byteToHex[randomValues[1]] +
    byteToHex[randomValues[2]] +
    byteToHex[randomValues[3]] +
    '-' +
    byteToHex[randomValues[4]] +
    byteToHex[randomValues[5]] +
    '-' +
    byteToHex[randomValues[6]] +
    byteToHex[randomValues[7]] +
    '-' +
    byteToHex[randomValues[8]] +
    byteToHex[randomValues[9]] +
    '-' +
    byteToHex[randomValues[10]] +
    byteToHex[randomValues[11]] +
    byteToHex[randomValues[12]] +
    byteToHex[randomValues[13]] +
    byteToHex[randomValues[14]] +
    byteToHex[randomValues[15]]
  );
}

export function generateCodeVerifierAndChallenge() {
  const verifier = generateUUID();
  const challenge = generateCodeChallenge(verifier);
  return { verifier, challenge };
}

function generateCodeChallenge(verifier) {
  const hashed = sha256(verifier, 'binary'); // k6 crypto.sha256
  return base64UrlEncode(hashed);
}

function base64UrlEncode(buffer) {
  return encoding.b64encode(buffer, 'rawurl'); // base64url sem padding
}
