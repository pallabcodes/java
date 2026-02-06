// Manul (core java)

enum PaymentStatus {
    SUCCESS,
    FAILED,
    PENDING
}

class PaymentResponse {

    private final String transactionId;
    private final double amount;
    private final String currency;
    private final PaymentStatus status;

    public PaymentResponse(
            String transactionId,
            double amount,
            String currency,
            PaymentStatus status
    ) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    @Override
    public String toString() {
        return transactionId + " " + status;
    }
}

interface PaymentProcessor {
    PaymentResponse process(double amount);
}

class StripeResponse {
    String id;
    double amount;
    String currency;
    String status;

    StripeResponse(
        String id,
        double amount,
        String currency,
        String status
    ) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }
}

class StripeGateway {

    StripeResponse pay(double amount) {
        return new StripeResponse(
            "pay_123",
            amount,
            "USD",
            "succeeded"
        );
    }
}

class PayPalResponse {
    String txnId;
    double value;
    String currencyCode;
    String state;

    PayPalResponse(
        String txnId,
        double value,
        String currencyCode,
        String state
    ) {
        this.txnId = txnId;
        this.value = value;
        this.currencyCode = currencyCode;
        this.state = state;
    }
}


class PayPalGateway {

    PayPalResponse makePayment(double amt) {
        return new PayPalResponse(
            "txn_456",
            amt,
            "USD",
            "approved"
        );
    }
}


class StripeMapper {

    static PaymentResponse map(
            StripeResponse r
    ) {

        PaymentStatus status =
            switch (r.status) {
                case "succeeded" ->
                    PaymentStatus.SUCCESS;
                default ->
                    PaymentStatus.FAILED;
            };

        return new PaymentResponse(
            r.id,
            r.amount,
            r.currency,
            status
        );
    }
}


class PayPalMapper {

    static PaymentResponse map(
            PayPalResponse r
    ) {

        PaymentStatus status =
            switch (r.state) {
                case "approved" ->
                    PaymentStatus.SUCCESS;
                default ->
                    PaymentStatus.FAILED;
            };

        return new PaymentResponse(
            r.txnId,
            r.value,
            r.currencyCode,
            status
        );
    }
}


class StripeAdapter
        implements PaymentProcessor {

    private final StripeGateway gateway;

    StripeAdapter(
        StripeGateway gateway
    ) {
        this.gateway = gateway;
    }

    public PaymentResponse process(
            double amount
    ) {

        StripeResponse res =
            gateway.pay(amount);

        return StripeMapper.map(res);
    }
}


class PayPalAdapter
        implements PaymentProcessor {

    private final PayPalGateway gateway;

    PayPalAdapter(
        PayPalGateway gateway
    ) {
        this.gateway = gateway;
    }

    public PaymentResponse process(
            double amount
    ) {

        PayPalResponse res =
            gateway.makePayment(amount);

        return PayPalMapper.map(res);
    }
}


class ProviderRegistry {

    private final Map<String, PaymentProcessor>
        processors = new HashMap<>();

    ProviderRegistry() {

        processors.put(
            "stripe",
            new StripeAdapter(
                new StripeGateway()
            )
        );

        processors.put(
            "paypal",
            new PayPalAdapter(
                new PayPalGateway()
            )
        );
    }

    PaymentProcessor get(String name) {
        return processors.get(name);
    }
}

class PaymentService {

    private final ProviderRegistry registry;

    PaymentService(
        ProviderRegistry registry
    ) {
        this.registry = registry;
    }

    PaymentResponse pay(
            String provider,
            double amount
    ) {

        return registry
            .get(provider)
            .process(amount);
    }
}


public class Main {

    public static void main(String[] args) {

        ProviderRegistry registry =
            new ProviderRegistry();

        PaymentService service =
            new PaymentService(registry);

        System.out.println(
            service.pay("stripe", 1000)
        );

        System.out.println(
            service.pay("paypal", 2000)
        );
    }
}

// Spring boot

// Port/Target/Unified interface
public interface PaymentProcessor {
    PaymentResponse process(double amount);
}

// SDK Gateways as Beans
@Component
class StripeGateway {

    StripeResponse pay(double amount) {
        return new StripeResponse(
            "pay_123",
            amount,
            "USD",
            "succeeded"
        );
    }
}

// Mapper Beans

@Component
class StripeMapper {

    PaymentResponse map(
            StripeResponse r
    ) {

        PaymentStatus status =
            r.status.equals("succeeded")
            ? PaymentStatus.SUCCESS
            : PaymentStatus.FAILED;

        return new PaymentResponse(
            r.id,
            r.amount,
            r.currency,
            status
        );
    }
}

// Adapter Beans

@Component("stripe")
class StripeAdapter
        implements PaymentProcessor {

    private final StripeGateway gateway;
    private final StripeMapper mapper;

    public StripeAdapter(
            StripeGateway gateway,
            StripeMapper mapper
    ) {
        this.gateway = gateway;
        this.mapper = mapper;
    }

    public PaymentResponse process(
            double amount
    ) {

        return mapper.map(
            gateway.pay(amount)
        );
    }
}

// Automatic Provider Registry (no manual map, since Spring inject automatically)
@Service
class PaymentService {

    private final Map<String,
        PaymentProcessor> processors;

    public PaymentService(
        Map<String,
        PaymentProcessor> processors
    ) {
        this.processors = processors;
    }

    public PaymentResponse pay(
            String provider,
            double amount
    ) {

        return processors
            .get(provider)
            .process(amount);
    }
}

// Controller (Optional API Layer)

@RestController
@RequestMapping("/payments")
class PaymentController {

    private final PaymentService service;

    public PaymentController(
        PaymentService service
    ) {
        this.service = service;
    }

    @PostMapping
    public PaymentResponse pay(
        @RequestParam String provider,
        @RequestParam double amount
    ) {
        return service.pay(provider, amount);
    }
}

