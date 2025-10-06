package istio.mtls

import data.isp

# Allow running with: opa test -v .

@test_strict_mtls_required {
  deny[_] with input as {
    "kind": {"kind": "PeerAuthentication"},
    "spec": {"mtls": {"mode": "PERMISSIVE"}}
  }
}

@test_strict_mtls_ok {
  not deny[_] with input as {
    "kind": {"kind": "PeerAuthentication"},
    "spec": {"mtls": {"mode": "STRICT"}}
  }
}
