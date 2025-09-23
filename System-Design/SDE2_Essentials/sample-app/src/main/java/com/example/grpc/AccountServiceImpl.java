package com.example.grpc;

import com.example.account.Account;
import com.example.account.AccountRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class AccountServiceImpl extends AccountServiceGrpc.AccountServiceImplBase {
    private final AccountRepository repository;

    public AccountServiceImpl(AccountRepository repository) {
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


