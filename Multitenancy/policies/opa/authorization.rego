package productivity.authz

default allow = false

allowed_roles = {"ROLE_ADMIN", "ROLE_TENANT_ADMIN"}

allow {
  input.method == "GET"
  input.path = ["api", "reports", _]
  some r
  r := input.jwt.roles[_]
  allowed_roles[r]
}

allow {
  input.method == "POST"
  input.path = ["api", "projects", _]
  some r
  r := input.jwt.roles[_]
  r == "ROLE_TENANT_ADMIN"
}

