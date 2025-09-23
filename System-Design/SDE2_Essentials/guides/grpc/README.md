## gRPC

### Why
* Strongly typed contracts using Protocol Buffers
* Efficient binary over HTTP2 with streaming

### Patterns
* Unary calls
* Server streaming
* Client streaming
* Bidirectional streaming

### Production checklist
* Deadlines and cancellation
* Retries with backoff and jitter
* mTLS and per call auth
* Interceptors for metrics and tracing
* Error model with canonical status codes

### Java reference implementation

#### Proto
```proto
syntax = "proto3";
package accounts.v1;

service AccountService {
  rpc GetAccount(GetAccountRequest) returns (GetAccountResponse) {}
}

message GetAccountRequest { string id = 1; }
message GetAccountResponse { string id = 1; string name = 2; int64 balance = 3; }
```

#### Server
```java
@GrpcService
public class AccountServiceGrpcImpl extends AccountServiceGrpc.AccountServiceImplBase {
    private final AccountRepository repository;

    public AccountServiceGrpcImpl(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public void getAccount(GetAccountRequest request, StreamObserver<GetAccountResponse> responseObserver) {
        try {
            Account a = repository.findById(request.getId());
            GetAccountResponse resp = GetAccountResponse.newBuilder()
                    .setId(a.getId())
                    .setName(a.getName())
                    .setBalance(a.getBalance())
                    .build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
```

#### Client with deadlines and retries
```java
public final class AccountClient {
    private final AccountServiceGrpc.AccountServiceBlockingStub stub;

    public AccountClient(ManagedChannel channel) {
        this.stub = AccountServiceGrpc.newBlockingStub(channel)
                .withWaitForReady()
                .withDeadlineAfter(300, TimeUnit.MILLISECONDS);
    }

    public GetAccountResponse getAccount(String id) {
        return stub.getAccount(GetAccountRequest.newBuilder().setId(id).build());
    }
}
```

#### Observability interceptors
```java
public class MetricsClientInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        long start = System.nanoTime();
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void close(Status status, Metadata trailers) {
                long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                // record metrics with method.getFullMethodName(), status.getCode(), durationMs
                super.close(status, trailers);
            }
        };
    }
}
```

### Security
* Use mTLS between services with distinct identities
* Validate authorization via per call metadata and scopes
* Rotate certificates and pin trust stores


