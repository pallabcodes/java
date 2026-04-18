1️⃣ Real-World Example — ETL Data Pipeline

Very production-realistic.

Think:

Extract data

Transform

Load

Cleanup

Every pipeline follows same steps — but logic differs.

abstract class DataPipeline {

    // Template method (fixed workflow)
    public final void runPipeline() {
        extract();
        transform();
        load();
        validate();
        cleanup();
    }

    protected abstract void extract();
    protected abstract void transform();
    protected abstract void load();

    // Default hooks
    protected void validate() {
        System.out.println("Validating data...");
    }

    protected void cleanup() {
        System.out.println("Cleaning temp files...");
    }
}


class CsvToDatabasePipeline extends DataPipeline {

    @Override
    protected void extract() {
        System.out.println("Reading CSV file...");
    }

    @Override
    protected void transform() {
        System.out.println("Mapping CSV → Entities...");
    }

    @Override
    protected void load() {
        System.out.println("Inserting into Database...");
    }
}


class CsvToDatabasePipeline extends DataPipeline {

    @Override
    protected void extract() {
        System.out.println("Reading CSV file...");
    }

    @Override
    protected void transform() {
        System.out.println("Mapping CSV → Entities...");
    }

    @Override
    protected void load() {
        System.out.println("Inserting into Database...");
    }
}


public class Main {
    public static void main(String[] args) {

        DataPipeline p1 = new CsvToDatabasePipeline();
        p1.runPipeline();

        System.out.println("----");

        DataPipeline p2 = new ApiToWarehousePipeline();
        p2.runPipeline();
    }
}

// N.B: To me both Facade and Template seem to be able to similar thing so which to when and are they any case when used together?

--

2️⃣ Real-World Example — Payment Processing Workflow

Same domain as Adapter earlier — but different problem.

Adapter = interface translation
Template = workflow standardization

// Template Base
abstract class PaymentProcessor {

    // Template method
    public final void processPayment(double amount) {

        authenticate();
        authorize(amount);
        debit(amount);
        notifyUser();
    }

    protected abstract void authenticate();
    protected abstract void authorize(double amount);
    protected abstract void debit(double amount);

    protected void notifyUser() {
        System.out.println("Sending payment notification...");
    }
}

// Credit Card Implemantion

class CreditCardPayment extends PaymentProcessor {

    protected void authenticate() {
        System.out.println("Authenticating via OTP...");
    }

    protected void authorize(double amount) {
        System.out.println("Authorizing credit limit...");
    }

    protected void debit(double amount) {
        System.out.println("Charging credit card: " + amount);
    }
}

// UPI implemenation

class UpiPayment extends PaymentProcessor {

    protected void authenticate() {
        System.out.println("Authenticating via UPI PIN...");
    }

    protected void authorize(double amount) {
        System.out.println("Checking bank balance...");
    }

    protected void debit(double amount) {
        System.out.println("Debiting bank account: " + amount);
    }
}

