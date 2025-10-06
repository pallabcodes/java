package productivity

@test_allow_get_ok {
  allow with input as {"path": "/api/issues", "method": "GET", "tenantId": "t1", "userId": "u1"}
}

@test_block_delete_internal {
  not allow with input as {"path": "/api/internal/issues", "method": "DELETE", "tenantId": "t1", "userId": "u1"}
}
