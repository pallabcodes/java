package productivity

# Allow if tenantId and userId are present and method is not DELETE on internal endpoints
default allow = false

allow {
  input.path
  input.method
  input.tenantId
  input.userId
  not startswith(input.path, "/api/internal/")
  input.method != "DELETE"
}

# Service-side default: allow method calls; can be tightened per target
service_allow {
  input.target
  input.method
}
