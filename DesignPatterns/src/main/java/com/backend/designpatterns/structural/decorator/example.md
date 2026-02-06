Perfect progression 👌 — Adapter → Composite → now **Decorator**.
And just like before, we’ll do:

1️⃣ Map toy → real world
2️⃣ Show proper production design
3️⃣ Remove smells
4️⃣ Show advanced variants

---

# 1️⃣ First — Classic Toy Example (For Reference)

Typical GoF demo:

```java
interface Coffee {
    String getDescription();
    double cost();
}

class SimpleCoffee implements Coffee {
    public String getDescription() { return "Coffee"; }
    public double cost() { return 5.0; }
}

class MilkDecorator implements Coffee {

    private Coffee coffee;

    public MilkDecorator(Coffee coffee) {
        this.coffee = coffee;
    }

    public String getDescription() {
        return coffee.getDescription() + ", Milk";
    }

    public double cost() {
        return coffee.cost() + 1.5;
    }
}
```

Usage:

```java
Coffee c = new MilkDecorator(new SimpleCoffee());
```

Structurally correct… but toy.

Let’s production-ify it.

---

# 2️⃣ Real-World Example — Notification System

This is a very real enterprise use case.

Problem:

You send notifications via:

* Email
* SMS
* Push
* Slack

But sometimes you want combinations:

* Email + SMS
* Email + Slack + Retry + Logging

You don’t want 50 subclasses like:

```
EmailSmsNotifier
EmailSmsSlackNotifier
EmailRetryNotifier
...
```

Decorator solves this explosion.

---

## Step 1 — Component

```java
interface Notifier {
    void send(String message);
}
```

---

## Step 2 — Concrete Component

```java
class EmailNotifier implements Notifier {

    @Override
    public void send(String message) {
        System.out.println("Sending EMAIL: " + message);
    }
}
```

Base behavior.

---

## Step 3 — Base Decorator

Production systems add this abstraction:

```java
abstract class NotifierDecorator implements Notifier {

    protected Notifier wrappee;

    public NotifierDecorator(Notifier notifier) {
        this.wrappee = notifier;
    }

    public void send(String message) {
        wrappee.send(message);
    }
}
```

Why?

So all decorators share wiring logic.

---

## Step 4 — Concrete Decorators

---

### SMS Decorator

```java
class SMSDecorator extends NotifierDecorator {

    public SMSDecorator(Notifier notifier) {
        super(notifier);
    }

    @Override
    public void send(String message) {
        super.send(message);
        sendSMS(message);
    }

    private void sendSMS(String msg) {
        System.out.println("Sending SMS: " + msg);
    }
}
```

---

### Slack Decorator

```java
class SlackDecorator extends NotifierDecorator {

    public SlackDecorator(Notifier notifier) {
        super(notifier);
    }

    @Override
    public void send(String message) {
        super.send(message);
        sendSlack(message);
    }

    private void sendSlack(String msg) {
        System.out.println("Sending SLACK: " + msg);
    }
}
```

---

## Step 5 — Client

```java
public class App {

    public static void main(String[] args) {

        Notifier notifier =
            new SlackDecorator(
                new SMSDecorator(
                    new EmailNotifier()
                )
            );

        notifier.send("Server is down!");
    }
}
```

Output:

```
Sending EMAIL: Server is down!
Sending SMS: Server is down!
Sending SLACK: Server is down!
```

Behavior stacked dynamically.

---

# 3️⃣ Mental Model

Decorator = **behavior wrapping onion**

```
Slack
  ↑
SMS
  ↑
Email
```

Each layer adds behavior before/after delegation.

---

# 4️⃣ Mapping to Real Systems

| Domain            | Component   | Decorators        |
| ----------------- | ----------- | ----------------- |
| Notifications     | Notifier    | SMS, Slack, Retry |
| Streams (Java IO) | InputStream | Buffered, GZIP    |
| UI                | View        | Border, Scroll    |
| Security          | Handler     | Auth, RateLimit   |
| Logging           | Service     | Metrics, Tracing  |

---

# 5️⃣ Real Java Example — Java IO (Production)

Decorator is everywhere in `java.io`.

Example:

```java
InputStream file =
    new BufferedInputStream(
        new FileInputStream("data.txt")
    );
```

Stack:

```
BufferedInputStream
   wraps FileInputStream
```

Add compression:

```java
InputStream file =
    new GZIPInputStream(
        new BufferedInputStream(
            new FileInputStream("data.txt")
        )
    );
```

Classic decorator chain.

---

# 6️⃣ Smells to Avoid (Like Your Adapter Insight)

---

### ❌ Decorator with if/else

```java
if(addSMS) { ... }
if(addSlack) { ... }
```

Then it’s not decorator — it’s conditional logic.

---

### ❌ Subclass explosion

```
EmailSMSNotifier
EmailSMSRetryNotifier
```

Decorator exists to avoid this.

---

# 7️⃣ Transparent vs Opaque Decorators

---

## Transparent (Common)

Same interface as component.

```java
Notifier n = new SMSDecorator(email);
```

Client unaware of decoration.

---

## Opaque

Decorator exposes extra APIs.

Rare but used for:

* Metrics access
* Retry counters

---

# 8️⃣ Advanced Production Decorators

---

## 8.1 Retry Decorator

```java
class RetryDecorator extends NotifierDecorator {

    public RetryDecorator(Notifier notifier) {
        super(notifier);
    }

    @Override
    public void send(String message) {

        for(int i=0;i<3;i++) {
            try {
                super.send(message);
                return;
            } catch(Exception e) {
                System.out.println("Retrying...");
            }
        }
    }
}
```

Used in:

* Payment calls
* HTTP clients

---

## 8.2 Logging Decorator

```java
class LoggingDecorator extends NotifierDecorator {

    public LoggingDecorator(Notifier notifier) {
        super(notifier);
    }

    @Override
    public void send(String message) {
        System.out.println("LOG: Sending → " + message);
        super.send(message);
    }
}
```

---

## 8.3 Security Decorator

```java
class AuthDecorator extends NotifierDecorator {

    public AuthDecorator(Notifier notifier) {
        super(notifier);
    }

    @Override
    public void send(String message) {

        if(!authorized()) {
            throw new RuntimeException("Unauthorized");
        }

        super.send(message);
    }

    private boolean authorized() { return true; }
}
```

---

# 9️⃣ Composite vs Decorator (Since You Just Studied It)

People confuse these a lot.

| Aspect   | Composite      | Decorator    |
| -------- | -------------- | ------------ |
| Purpose  | Tree structure | Add behavior |
| Relation | Parent-child   | Wrapper      |
| Focus    | Hierarchy      | Enhancement  |
| Example  | UI layout      | UI styling   |

They combine often:

```
Panel (Composite)
  └─ ScrollDecorator
        └─ BorderDecorator
              └─ TextBox
```

---

# 10️⃣ Mental Shortcut

If you see:

* Wrapping same interface
* Adding behavior dynamically
* Delegation + augmentation

Think Decorator.

---

# 🔑 Final Take

Decorator is used when you want to:

✔ Add behavior at runtime
✔ Avoid subclass explosion
✔ Stack features dynamically
✔ Keep interface same

Structure:

```
Component
   ↑
ConcreteComponent
   ↑
Decorator (abstract)
   ↑
ConcreteDecorators
```

---

If you want, next we can go deeper into:

* Decorator vs Proxy vs AOP
* Spring Boot decorators (HandlerInterceptor, Filters)
* HTTP client middleware chains
* React HOCs vs Decorators mapping

That’s where Decorator becomes architectural, not just structural.
