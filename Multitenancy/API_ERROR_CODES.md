# API Error Codes (Catalog)

| Code        | HTTP | Domain   | Meaning                               |
|-------------|------|----------|---------------------------------------|
| VAL_400_001 | 400  | Common   | Validation failed (field errors map) |
| GEN_404_001 | 404  | Common   | Resource not found                   |
| GEN_500_000 | 500  | Common   | Unexpected server error              |
| PRJ_409_001 | 409  | Projects | Project key conflict                 |
| ISS_409_001 | 409  | Issues   | Issue key conflict                   |

Notes:
- All error responses use ApiResponse with `success=false`, `status`, `message`, `error`, `errorCode`.
- Extend codes per-module as needed; avoid breaking existing contracts.
