import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

// Chaos engineering test configuration
export const options = {
  scenarios: {
    constant_load_with_failures: {
      executor: 'constant-vus',
      vus: 50,
      duration: '10m',
    },
  },

  thresholds: {
    http_req_failed: ['rate<0.05'], // Allow up to 5% error rate during chaos
    http_req_duration: ['p(95)<200'], // Allow higher latency during chaos
  },
};

// Base URL for the API
const BASE_URL = __ENV.BASE_URL || 'https://api.streaming-platform.company.com';

export default function () {
  const userId = `chaos-user-${exec.vu.idInTest}`;
  const contentId = `chaos-content-${Math.floor(Math.random() * 100)}`;

  // Simulate normal user behavior during chaos
  const startResponse = http.post(`${BASE_URL}/v1/playback/start`, JSON.stringify({
    userId: userId,
    contentId: contentId,
    deviceType: 'CHAOS_TEST',
    quality: 'HD'
  }), {
    headers: {
      'Content-Type': 'application/json',
    },
  });

  check(startResponse, {
    'chaos start playback status is 201 or 500': (r) => [201, 500, 503].includes(r.status),
    'chaos start playback has reasonable response time': (r) => r.timings.duration < 500,
  });

  sleep(Math.random() * 2 + 1); // Random sleep 1-3 seconds

  // Try to get status (might fail during chaos)
  if (startResponse.status === 201) {
    const sessionId = startResponse.json().sessionId;
    const statusResponse = http.get(`${BASE_URL}/v1/playback/${sessionId}/status`);

    check(statusResponse, {
      'chaos get status is resilient': (r) => [200, 404, 500, 503].includes(r.status),
    });
  }

  sleep(Math.random() * 1 + 0.5); // Random sleep 0.5-1.5 seconds
}

export function setup() {
  console.log('🚨 Starting chaos engineering test...');
  console.log('This test simulates system behavior during failures');
  console.log(`Testing against: ${BASE_URL}`);
  console.log('Chaos experiments should be running in parallel');
}

export function teardown() {
  console.log('🧪 Chaos engineering test completed');
  console.log('Review system resilience and recovery capabilities');
}
