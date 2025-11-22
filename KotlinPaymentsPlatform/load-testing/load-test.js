import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const paymentCreationTrend = new Trend('payment_creation_duration');
const riskEvaluationTrend = new Trend('risk_evaluation_duration');

// Test configuration
export const options = {
  stages: [
    { duration: '2m', target: 10 },   // Ramp up to 10 users
    { duration: '5m', target: 50 },   // Ramp up to 50 users
    { duration: '10m', target: 100 }, // Ramp up to 100 users
    { duration: '5m', target: 100 },  // Stay at 100 users
    { duration: '2m', target: 0 },    // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    http_req_failed: ['rate<0.1'],    // Error rate should be below 10%
    errors: ['rate<0.1'],             // Custom error rate
  },
};

// Base URL
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Test data
const testUsers = [
  { id: 'user_001', email: 'user001@test.com' },
  { id: 'user_002', email: 'user002@test.com' },
  { id: 'user_003', email: 'user003@test.com' },
  { id: 'user_004', email: 'user004@test.com' },
  { id: 'user_005', email: 'user005@test.com' },
];

const riskProfiles = [
  { level: 'low', country: 'US', amount: 50.00, expectedDecision: 'APPROVE' },
  { level: 'medium', country: 'CA', amount: 500.00, expectedDecision: 'APPROVE' },
  { level: 'high', country: 'MX', amount: 5000.00, expectedDecision: 'REVIEW' },
  { level: 'critical', country: 'KP', amount: 100.00, expectedDecision: 'DECLINE' },
];

export default function () {
  // Get random test data
  const user = testUsers[Math.floor(Math.random() * testUsers.length)];
  const riskProfile = riskProfiles[Math.floor(Math.random() * riskProfiles.length)];

  // Test 1: Risk Evaluation
  const riskRequest = {
    paymentId: `pay_${Date.now()}_${__VU}_${__ITER}`,
    amount: riskProfile.amount,
    currency: 'USD',
    customerId: user.id,
    merchantId: 'merchant_123',
    cardLastFour: generateCardNumber(),
    countryCode: riskProfile.country,
    ipAddress: generateIPAddress(),
    userAgent: 'Mozilla/5.0 (Load Test)',
  };

  const riskStartTime = new Date().getTime();
  const riskResponse = http.post(
    `${BASE_URL}/api/v1/risk/decisions`,
    JSON.stringify(riskRequest),
    {
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: '10s',
    }
  );

  const riskDuration = new Date().getTime() - riskStartTime;
  riskEvaluationTrend.add(riskDuration);

  check(riskResponse, {
    'risk evaluation status is 200': (r) => r.status === 200,
    'risk evaluation has decision': (r) => r.json().decision !== undefined,
    'risk evaluation response time < 2000ms': (r) => r.timings.duration < 2000,
  }) || errorRate.add(1);

  // If risk evaluation approves, create payment intent
  if (riskResponse.status === 200 && riskResponse.json().decision === 'APPROVE') {
    sleep(Math.random() * 2 + 1); // Random sleep 1-3 seconds

    // Test 2: Payment Intent Creation
    const paymentRequest = {
      amount: Math.floor(riskProfile.amount * 100), // Convert to cents
      currency: 'USD',
      description: `Load test payment for ${user.email}`,
    };

    const paymentStartTime = new Date().getTime();
    const paymentResponse = http.post(
      `${BASE_URL}/api/v1/payments/intents`,
      JSON.stringify(paymentRequest),
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${generateTestToken()}`,
        },
        timeout: '10s',
      }
    );

    const paymentDuration = new Date().getTime() - paymentStartTime;
    paymentCreationTrend.add(paymentDuration);

    check(paymentResponse, {
      'payment creation status is 201': (r) => r.status === 201,
      'payment creation has id': (r) => r.json().id !== undefined,
      'payment creation response time < 3000ms': (r) => r.timings.duration < 3000,
    }) || errorRate.add(1);

    // If payment created successfully, confirm it
    if (paymentResponse.status === 201) {
      sleep(Math.random() * 3 + 2); // Random sleep 2-5 seconds

      // Test 3: Payment Confirmation
      const paymentId = paymentResponse.json().id;
      const confirmResponse = http.post(
        `${BASE_URL}/api/v1/payments/intents/${paymentId}/confirm`,
        null,
        {
          headers: {
            'Authorization': `Bearer ${generateTestToken()}`,
          },
          timeout: '10s',
        }
      );

      check(confirmResponse, {
        'payment confirmation status is 200': (r) => r.status === 200,
        'payment confirmation response time < 5000ms': (r) => r.timings.duration < 5000,
      }) || errorRate.add(1);
    }
  }

  sleep(Math.random() * 5 + 2); // Random sleep 2-7 seconds between iterations
}

// Helper functions
function generateCardNumber() {
  // Generate a random 4-digit number for testing
  return Math.floor(Math.random() * 9000 + 1000).toString();
}

function generateIPAddress() {
  // Generate a random IPv4 address
  return `${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`;
}

function generateTestToken() {
  // Generate a mock JWT token for testing
  // In real scenarios, this would be obtained from authentication service
  return 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJpYXQiOjE1MTYyMzkwMjJ9.test-signature';
}

// Setup function - runs before the test starts
export function setup() {
  // Warm up the services
  console.log('Starting load test setup...');

  // Health check
  const healthResponse = http.get(`${BASE_URL}/actuator/health`);
  check(healthResponse, {
    'setup health check passed': (r) => r.status === 200,
  });

  console.log('Load test setup completed');
}

// Teardown function - runs after the test completes
export function teardown(data) {
  console.log('Load test completed');
}

// Handle summary - custom summary output
export function handleSummary(data) {
  const summary = {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    'load-test-results.json': JSON.stringify(data, null, 2),
    'metrics-summary.json': JSON.stringify({
      metrics: {
        http_req_duration: data.metrics.http_req_duration,
        http_req_failed: data.metrics.http_req_failed,
        errors: data.metrics.errors,
        payment_creation_duration: data.metrics.payment_creation_duration,
        risk_evaluation_duration: data.metrics.risk_evaluation_duration,
      },
      thresholds: options.thresholds,
    }, null, 2),
  };

  return summary;
}

function textSummary(data, options) {
  return `
📊 Load Test Summary
==================

Test Duration: ${data.metrics.iteration_duration.values.avg}ms avg iteration
Total Requests: ${data.metrics.http_reqs.values.count}
Failed Requests: ${data.metrics.http_req_failed.values.rate * 100}%

🚀 Performance Metrics
=====================

Response Time (avg): ${Math.round(data.metrics.http_req_duration.values.avg)}ms
Response Time (95th): ${Math.round(data.metrics.http_req_duration.values['p(95)'])}ms
Response Time (99th): ${Math.round(data.metrics.http_req_duration.values['p(99)'])}ms

Risk Evaluation (avg): ${Math.round(data.metrics.risk_evaluation_duration.values.avg)}ms
Payment Creation (avg): ${Math.round(data.metrics.payment_creation_duration.values.avg)}ms

📈 Throughput
============

Requests/second: ${Math.round(data.metrics.http_reqs.values.rate)}
Data received: ${Math.round(data.metrics.data_received.values.rate / 1024)} KB/s
Data sent: ${Math.round(data.metrics.data_sent.values.rate / 1024)} KB/s

⚠️  Error Analysis
================

HTTP Errors: ${data.metrics.http_req_failed.values.rate * 100}%
Custom Errors: ${data.metrics.errors.values.rate * 100}%

🔍 Threshold Results
==================

${Object.entries(options.thresholds).map(([metric, thresholds]) => {
  const metricData = data.metrics[metric];
  if (!metricData) return `${metric}: No data`;

  return thresholds.map(threshold => {
    const passed = evaluateThreshold(metricData, threshold);
    const status = passed ? '✅' : '❌';
    return `${status} ${metric}: ${threshold}`;
  }).join('\n');
}).join('\n')}

💡 Recommendations
================

${generateRecommendations(data)}
`;
}

function evaluateThreshold(metricData, threshold) {
  // Simple threshold evaluation logic
  if (threshold.includes('<')) {
    const value = parseFloat(threshold.split('<')[1]);
    return metricData.values.avg < value;
  } else if (threshold.includes('>')) {
    const value = parseFloat(threshold.split('>')[1]);
    return metricData.values.avg > value;
  }
  return true;
}

function generateRecommendations(data) {
  const recommendations = [];

  if (data.metrics.http_req_duration.values['p(95)'] > 1000) {
    recommendations.push('• Consider optimizing database queries for better response times');
  }

  if (data.metrics.http_req_failed.values.rate > 0.05) {
    recommendations.push('• Investigate error patterns and improve error handling');
  }

  if (data.metrics.errors.values.rate > 0.02) {
    recommendations.push('• Review business logic errors and input validation');
  }

  if (recommendations.length === 0) {
    recommendations.push('• System performance is within acceptable parameters');
  }

  return recommendations.join('\n');
}
