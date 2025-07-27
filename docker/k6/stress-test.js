import http from 'k6/http';
import { check, sleep } from 'k6';
import { authenticateApp } from './auth-helpers.js';
import { performFullLogin } from './login-flow.js';
import { generateUUID } from './code-challenge.js';

export let options = {
  stages: [
    { duration: '30s', target: 10 },  // warm-up
    { duration: '1m', target: 100 },  // pico
    { duration: '30s', target: 0 },   // cooldown
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],      // < 1% falha
    http_req_duration: ['p(95)<500'],    // 95% abaixo de 500ms
  },
};

const BASE_URL = 'http://localhost:8081/api';

function randomUser() {
  return {
    email: `user${generateUUID()}@test.com`,
    password: 'password123K6*',
    firstName: 'User Teste',
    lastName: 'K6Test',
  };
}

function createIdempotencyKey() {
  // generate random unioque key for idempotency
  return `idempotency-${Math.random().toString(36).substring(2, 9)}`;
}

async function tryLoginWithRetries(email, password, maxRetries = 5, delaySeconds = 1) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      const token = await performFullLogin(email, password);
      if (token) return token;
    } catch (e) {
      if (i === maxRetries - 1) throw e;
      sleep(delaySeconds);
    }
  }
}

export default async function () {
  const { email, password, firstName, lastName } = randomUser();

  // Cria usuário
  const appToken = await authenticateApp();
  const createUserOrganizationRes = http.post(`${BASE_URL}/v1/organizations`, JSON.stringify({
    first_name: firstName,
    last_name: lastName,
    email,
    password,
    organization_name: "K6 Test Organization " + generateUUID(),
    description: "Organization for K6 stress tests",
  }), { headers: { 'Content-Type': 'application/json', "Authorization": `Bearer ${appToken}` } });

  check(createUserOrganizationRes, { 'user organization created': (r) => r.status === 201 });
  console.log('Create User Org response:', createUserOrganizationRes.body);
  sleep(1);

  // const token = await performFullLogin(email, password);
  const token = await tryLoginWithRetries(email, password);
  const headers = {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  const createEventRes = http.post(`${BASE_URL}/v1/events`, JSON.stringify({
    organization_id: createUserOrganizationRes.json().organization_id,
    title: 'K6 Stress Test Event ' + generateUUID(),
    description: 'Event created for K6 stress testing',
    event_type: "REMOTE",
    category_id: "mock-category-id",
    start_at: '2025-08-01T18:00:00Z',
    finish_at: '2025-08-10T22:00:00Z',
  }), { headers });
  check(createEventRes, { 'event created': (r) => r.status === 201 });

  const rnd = Math.random();

  if (rnd < 0.2) {
    const res = http.get(`${BASE_URL}/v1/events?search=show&page=0&perPage=10`, { headers });
    check(res, { 'list events': (r) => r.status === 200 });

  } else if (rnd < 0.4) {
    const eventId = createEventRes.json().event_id || 'mock-event-id';
    const res = http.get(`${BASE_URL}/v1/events/${eventId}`, { headers });
    check(res, { 'get event by id': (r) => r.status === 200 });

  } else if (rnd < 0.6) {
    const payload = JSON.stringify({
      event_id: createEventRes.json().event_id || 'mock-event-id',
      name: 'VIP Ticket ' + generateUUID(),
      description: 'VIP access to the event',
      price: "100.50",
      quantity: 20,
      type: "VIP",
      status: "AVAILABLE",
    });
    const ticketRes = http.post(`${BASE_URL}/v1/tickets`, payload, {
      headers: Object.assign({}, headers, { 'Idempotency-Key': createIdempotencyKey() }),
    });
    check(ticketRes, { 'ticket created': (r) => r.status === 201 });

  } else if (rnd < 0.8) {
    const payload = JSON.stringify({
      organization_id: createUserOrganizationRes.json().organization_id,
      title: 'K6 Test Event ' + generateUUID(),
      description: 'Stress test',
      event_type: "REMOTE",
      category_id: "mock-category-id",
      start_at: '2025-08-01T18:00:00Z',
      finish_at: '2025-08-11T22:00:00Z',
    });

    const eventRes = http.post(`${BASE_URL}/v1/events`, payload, {
       headers: Object.assign({}, headers),
      // headers: Object.assign({}, headers, { 'Idempotency-Key': createIdempotencyKey() }),
    });
    check(eventRes, { 'event created': (r) => r.status === 201 });

  }

  sleep(1);
}
