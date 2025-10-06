package istio.mtls

deny[msg] {
  input.kind.kind == "PeerAuthentication"
  input.spec.mtls.mode != "STRICT"
  msg := "PeerAuthentication must enforce STRICT mTLS"
}
