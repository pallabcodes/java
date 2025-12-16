import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const responseTime = new Trend('response_time');

// Test configuration
export const options = {
  stages: [
    // Ramp up to 100 users over 2 minutes
    { duration: '2m', target: 100 },

    // Stay at 100 users for 5 minutes
    { duration: '5m', target: 100 },

    // Ramp up to 500 users over 3 minutes
    { duration: '3m', target: 500 },

    // Stay at 500 users for 10 minutes (main test)
    { duration: '10m', target: 500 },

    // Ramp up to 1000 users over 5 minutes
    { duration: '5m', target: 1000 },

    // Stay at 1000 users for 5 minutes (stress test)
    { duration: '5m', target: 1000 },

    // Ramp down to 0 users over 2 minutes
    { duration: '2m', target: 0 },
  ],

  thresholds: {
    // HTTP request duration should be < 100ms for 95% of requests
    http_req_duration: ['p(95)<100'],

    // Error rate should be < 1%
    http_req_failed: ['rate<0.01'],

    // Custom error rate should be < 1%
    errors: ['rate<0.01'],
  },

  // Test options
  noConnectionReuse: false,
  userAgent: 'K6LoadTest/1.0',
};

// Base URL for the API
const BASE_URL = __ENV.BASE_URL || 'https://api.streaming-platform.company.com';

// Test data
const testUsers = [
  { id: 'user-001', contentId: 'movie-001' },
  { id: 'user-002', contentId: 'show-001' },
  { id: 'user-003', contentId: 'movie-002' },
  { id: 'user-004', contentId: 'show-002' },
  { id: 'user-005', contentId: 'movie-003' },
];

// Helper functions
function getRandomUser() {
  return testUsers[Math.floor(Math.random() * testUsers.length)];
}

function getAuthToken() {
  // In production, this would get a real JWT token
  // For testing, we'll use a mock token
  return 'mock-jwt-token';
}

function makeRequest(method, url, body = null, headers = {}) {
  const defaultHeaders = {
    'Authorization': `Bearer ${getAuthToken()}`,
    'Content-Type': 'application/json',
    'User-Agent': 'StreamingPlatform/1.0',
    ...headers
  };

  const params = {
    headers: defaultHeaders,
    timeout: '10s',
  };

  if (body) {
    params.body = JSON.stringify(body);
  }

  const response = http.request(method, url, params);

  // Record custom metrics
  responseTime.add(response.timings.duration);
  errorRate.add(response.status >= 400);

  return response;
}

// Test scenarios
export function setup() {
  console.log('Starting load test setup...');

  // Warm up the system with a few requests
  for (let i = 0; i < 10; i++) {
    const response = makeRequest('GET', `${BASE_URL}/v1/health`);
    check(response, {
      'warmup health check passed': (r) => r.status === 200,
    });
    sleep(0.1);
  }

  console.log('Load test setup completed');
  return { timestamp: new Date().toISOString() };
}

export default function (data) {
  const user = getRandomUser();

  // Scenario 1: Health Check (10% of requests)
  if (Math.random() < 0.1) {
    const response = makeRequest('GET', `${BASE_URL}/v1/health`);

    check(response, {
      'health check status is 200': (r) => r.status === 200,
      'health check response time < 50ms': (r) => r.timings.duration < 50,
    });

    sleep(Math.random() * 0.5 + 0.1);
  }

  // Scenario 2: Start Playback (30% of requests)
  else if (Math.random() < 0.3) {
    const payload = {
      userId: user.id,
      contentId: user.contentId,
      deviceType: 'WEB',
      quality: 'HD',
      startPosition: 0
    };

    const response = makeRequest('POST', `${BASE_URL}/v1/playback/start`, payload);

    check(response, {
      'start playback status is 201': (r) => r.status === 201,
      'start playback response time < 100ms': (r) => r.timings.duration < 100,
      'start playback returns session id': (r) => r.json().sessionId !== undefined,
    });

    // Store session ID for subsequent requests
    if (response.status === 201) {
      const sessionId = response.json().sessionId;
      // In a real scenario, you'd store this in a shared context
    }

    sleep(Math.random() * 0.5 + 0.2);
  }

  // Scenario 3: Get Playback Status (25% of requests)
  else if (Math.random() < 0.25) {
    // Use a mock session ID since we can't share state between iterations
    const sessionId = `session-${Math.floor(Math.random() * 1000)}`;
    const response = makeRequest('GET', `${BASE_URL}/v1/playback/${sessionId}/status`);

    check(response, {
      'get status status is 200 or 404': (r) => [200, 404].includes(r.status),
      'get status response time < 80ms': (r) => r.timings.duration < 80,
    });

    sleep(Math.random() * 0.3 + 0.1);
  }

  // Scenario 4: Update Playback Position (20% of requests)
  else if (Math.random() < 0.2) {
    const sessionId = `session-${Math.floor(Math.random() * 1000)}`;
    const payload = {
      position: Math.floor(Math.random() * 3600), // Random position up to 1 hour
      quality: ['SD', 'HD', '4K'][Math.floor(Math.random() * 3)]
    };

    const response = makeRequest('PUT', `${BASE_URL}/v1/playback/${sessionId}/position`, payload);

    check(response, {
      'update position status is 200 or 404': (r) => [200, 404].includes(r.status),
      'update position response time < 100ms': (r) => r.timings.duration < 100,
    });

    sleep(Math.random() * 0.4 + 0.1);
  }

  // Scenario 5: Stop Playback (10% of requests)
  else if (Math.random() < 0.1) {
    const sessionId = `session-${Math.floor(Math.random() * 1000)}`;
    const payload = {
      endPosition: Math.floor(Math.random() * 3600),
      reason: 'USER_STOPPED'
    };

    const response = makeRequest('POST', `${BASE_URL}/v1/playback/${sessionId}/stop`, payload);

    check(response, {
      'stop playback status is 200 or 404': (r) => [200, 404].includes(r.status),
      'stop playback response time < 100ms': (r) => r.timings.duration < 100,
    });

    sleep(Math.random() * 0.5 + 0.2);
  }

  // Scenario 6: Get Analytics (5% of requests)
  else {
    const response = makeRequest('GET', `${BASE_URL}/v1/analytics/dashboard?userId=${user.id}`);

    check(response, {
      'get analytics status is 200': (r) => r.status === 200,
      'get analytics response time < 150ms': (r) => r.timings.duration < 150,
      'get analytics returns data': (r) => r.json().metrics !== undefined,
    });

    sleep(Math.random() * 0.8 + 0.3);
  }

  // Random sleep between requests to simulate real user behavior
  sleep(Math.random() * 1 + 0.5);
}

export function teardown(data) {
  console.log('Load test completed');
  console.log(`Test started at: ${data.timestamp}`);
  console.log(`Test completed at: ${new Date().toISOString()}`);
}
