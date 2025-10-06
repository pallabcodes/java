package productivity.authz

import data.productivity.authz.allow

test_report_access_allowed_for_admin {
  allow with input as {"method": "GET", "path": ["api", "reports", "summary"], "jwt": {"roles": ["ROLE_ADMIN"]}}
}

test_project_create_denied_for_user {
  not allow with input as {"method": "POST", "path": ["api", "projects", "create"], "jwt": {"roles": ["ROLE_USER"]}}
}

