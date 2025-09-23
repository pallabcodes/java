## OAuth2

### Flows to know
* Authorization Code with PKCE for clients
* Client Credentials for service to service

### Tokens
* Access token lifetime and scopes
* Refresh token rotation and revocation
* JWT validation and key rotation via JWKS

### Service integration
* Resource server validates tokens and scopes
* Downstream propagation using token exchange when required

### Java reference implementation

#### Resource server config
```java
@Configuration
@EnableWebSecurity
public class ResourceServerSecurityConfig {
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated()
          )
          .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(Customizer.withDefaults())
          );
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(NimbusJwtDecoderJwkSupportFactory factory) {
        return factory.fromJwksUri("https://issuer.example.com/.well-known/jwks.json");
    }
}
```

#### Method security by scope
```java
@RestController
public class AccountController {
    @PreAuthorize("hasAuthority('SCOPE_accounts:read')")
    @GetMapping("/accounts/{id}")
    public Account get(@PathVariable String id) { /* ... */ return null; }
}
```

#### Token propagation to downstream
```java
public class DownstreamClient {
    private final WebClient webClient;

    public DownstreamClient(ReactiveJwtAuthenticationToken auth, WebClient.Builder builder) {
        this.webClient = builder
          .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getToken().getTokenValue())
          .build();
    }
}
```

### Operational practices
* JWKS cache with eviction and background refresh
* Key rotation with overlapping validity windows
* Short lived access tokens and rotated refresh tokens


