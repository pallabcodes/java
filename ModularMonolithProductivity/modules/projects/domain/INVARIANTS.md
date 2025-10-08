# Domain Invariants

- key is immutable after creation
- key format: uppercase letters and digits only
- name length within reasonable bounds
- tenant id required on every entity
- composite unique constraints: (tenant_id, key) and (tenant_id, name)
