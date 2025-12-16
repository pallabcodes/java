import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Test configuration for smoke tests
export const options = {
  vus: 1, // 1 virtual user
  duration: '30s', // Run for 30 seconds

  thresholds: {
    http_req_failed: ['rate<0.1'], // Error rate < 10%
    http_req_duration: ['p(95)<500'], // 95% of requests < 500ms
  },
};

// Base URL for the API
const BASE_URL = __ENV.BASE_URL || 'https://api.streaming-platform.company.com';

export default function () {
  // Test 1: Health Check
  const healthResponse = http.get(`${BASE_URL}/v1/health`);
  check(healthResponse, {
    'health check status is 200': (r) => r.status === 200,
    'health check response time < 100ms': (r) => r.timings.duration < 100,
  });

  // Test 2: Infrastructure Service Health
  const infraResponse = http.get(`${BASE_URL}/v1/infrastructure/health`);
  check(infraResponse, {
    'infrastructure health status is 200': (r) => r.status === 200,
  });

  // Test 3: Analytics Service Health
  const analyticsResponse = http.get(`${BASE_URL}/v1/analytics/health`);
  check(analyticsResponse, {
    'analytics health status is 200': (r) => r.status === 200,
  });

  // Test 4: Playback Service Health
  const playbackResponse = http.get(`${BASE_URL}/v1/playback/health`);
  check(playbackResponse, {
    'playback health status is 200': (r) => r.status === 200,
  });

  // Test 5: Basic API Functionality
  const basicResponse = http.get(`${BASE_URL}/v1/api/info`);
  check(basicResponse, {
    'API info status is 200': (r) => r.status === 200,
    'API info contains version': (r) => r.json().version !== undefined,
  });

  sleep(1); // Wait 1 second between iterations
}

export function setup() {
  console.log('Starting smoke tests...');
  console.log(`Testing against: ${BASE_URL}`);
}

export function teardown() {
  console.log('Smoke tests completed successfully');
}
