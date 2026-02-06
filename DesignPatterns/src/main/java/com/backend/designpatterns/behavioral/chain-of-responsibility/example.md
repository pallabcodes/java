# Real-World Example — HTTP Request Pipeline

Incoming Request
   ↓
LoggingHandler
   ↓
AuthHandler
   ↓
RateLimitHandler
   ↓
ValidationHandler
   ↓
BusinessHandler


// Request Object

class Request {
    String user;
    boolean authenticated;
    String payload;

    public Request(String user, boolean authenticated, String payload) {
        this.user = user;
        this.authenticated = authenticated;
        this.payload = payload;
    }
}

// Base Handler
abstract class Handler {

    protected Handler next;

    public Handler setNext(Handler next) {
        this.next = next;
        return next;
    }

    public abstract void handle(Request req);
}

// LogginHandler = { next: null }

// LogginHandler = { next: AuthHandler { next: RateLimitHandler { next: BusinessHandler { next: null } } } }


// Logging Handler

class LoggingHandler extends Handler {

    public void handle(Request req) {
        System.out.println("Logging request for user: " + req.user);

        if (next != null)
            next.handle(req);
    }
}


// Auth Handler

class AuthHandler extends Handler {

    public void handle(Request req) {

        if (!req.authenticated) {
            System.out.println("Unauthorized request");
            return; // stop chain
        }

        System.out.println("Auth success");

        if (next != null)
            next.handle(req);
    }
}


//  Rate limit

class RateLimitHandler extends Handler {

    public void handle(Request req) {

        if ("blockedUser".equals(req.user)) {
            System.out.println("Rate limit exceeded");
            return;
        }

        if (next != null)
            next.handle(req);
    }
}

// Business Handler

class BusinessHandler extends Handler {

    public void handle(Request req) {
        System.out.println("Processing payload: " + req.payload);
    }
}

// usage

public class MainChain {

    public static void main(String[] args) {

        Handler chain = new LoggingHandler();

        chain
            .setNext(new AuthHandler())
            .setNext(new RateLimitHandler())
            .setNext(new BusinessHandler());

        Request req = new Request("user1", true, "order-data");

        chain.handle(req);
    }
}

// Chain of Responsibility Flow:

Perfect — this is the **right way** to internalize Chain of Responsibility:

> Step execution + call flow + recursion visualization.

And yes — what you’re seeing **is recursion**, but not the classic “function calls itself” recursion.

It’s **object recursion / delegated recursion**.

Let’s walk it visually and mentally like a debugger.

---

# 1️⃣ First — Build the Chain (Memory Visualization)

This line builds the linked chain:

```java
Handler chain = new LoggingHandler();

chain
   .setNext(new AuthHandler())
   .setNext(new RateLimitHandler())
   .setNext(new BusinessHandler());
```

### What actually happens step-by-step

---

## Step 1

```java
Handler chain = new LoggingHandler();
```

Memory:

```
LoggingHandler
   next → null
```

---

## Step 2

```java
chain.setNext(new AuthHandler());
```

Now:

```
LoggingHandler → AuthHandler → null
```

Because:

```java
this.next = next;
return next;
```

Returned `AuthHandler`.

---

## Step 3

```java
.setNext(new RateLimitHandler())
```

Now:

```
LoggingHandler → AuthHandler → RateLimitHandler → null
```

---

## Step 4

```java
.setNext(new BusinessHandler());
```

Final chain:

```
Logging
   ↓
Auth
   ↓
RateLimit
   ↓
Business
   ↓
null
```

Think **singly linked list of handlers**.

---

# 2️⃣ Now Execution Starts

```java
chain.handle(req);
```

So first call is:

```
LoggingHandler.handle(req)
```

Let’s trace call stack.

---

# 3️⃣ Call Stack + Flow Visualization

We’ll track two things:

* Call stack
* Console output

---

## 🟢 Step 1 — LoggingHandler

Code:

```java
System.out.println("Logging request for user: " + req.user);

if (next != null)
    next.handle(req);
```

Output:

```
Logging request for user: user1
```

Then it calls:

```
AuthHandler.handle(req)
```

---

### Call stack now

```
handle() → LoggingHandler
   ↳ handle() → AuthHandler
```

This is where recursion begins.

Logging is waiting for Auth to finish.

---

## 🟢 Step 2 — AuthHandler

Code:

```java
if (!req.authenticated) {
   print "Unauthorized";
   return;
}
```

Our request:

```java
authenticated = true
```

So:

```
Auth success
```

Then:

```
next.handle(req)
```

Calls:

```
RateLimitHandler.handle(req)
```

---

### Call stack now

```
Logging.handle
   ↳ Auth.handle
        ↳ RateLimit.handle
```

Nested delegation recursion.

---

## 🟢 Step 3 — RateLimitHandler

Code:

```java
if ("blockedUser".equals(req.user)) {
   print "Rate limit exceeded";
   return;
}
```

User = `user1` → allowed.

So it calls:

```
BusinessHandler.handle(req)
```

---

### Call stack now

```
Logging.handle
   ↳ Auth.handle
        ↳ RateLimit.handle
             ↳ Business.handle
```

Deepest recursion level.

---

## 🟢 Step 4 — BusinessHandler (Terminal Node)

Code:

```java
System.out.println("Processing payload: " + req.payload);
```

Output:

```
Processing payload: order-data
```

No `next`.

So function ends.

---

# 4️⃣ Stack Unwinding (Return Phase)

Now recursion unwinds.

Like:

```
Business.handle()  → returns
RateLimit.handle() → returns
Auth.handle()      → returns
Logging.handle()   → returns
main()             → continues
```

No more output.

---

# 5️⃣ Full Execution Timeline

Let’s visualize vertically:

```
Incoming Request
      │
      ▼
LoggingHandler
      │ print "Logging request"
      ▼
AuthHandler
      │ print "Auth success"
      ▼
RateLimitHandler
      │ (passes)
      ▼
BusinessHandler
      │ print "Processing payload"
      ▼
     END
```

---

# 6️⃣ Console Output (Final)

```
Logging request for user: user1
Auth success
Processing payload: order-data
```

---

# 7️⃣ Where Recursion Actually Happens

Important nuance:

No handler calls **itself**.

Instead:

```
handler → next.handle()
```

So recursion is:

> Across objects, not within one function.

This is called:

* Delegation recursion
* Linked recursion
* Chain recursion

---

# 8️⃣ Compare With Classic Recursion

Classic:

```java
factorial(n) → factorial(n-1)
```

Self-call.

CoR recursion:

```java
Logging → Auth → RateLimit → Business
```

Different objects, same call stack growth.

---

# 9️⃣ Early Termination Visualization

Let’s change request:

```java
Request req = new Request("user1", false, "order-data");
```

Now flow:

```
Logging → Auth → STOP
```

Execution:

```
Logging request for user: user1
Unauthorized request
```

Chain breaks.

No RateLimit, no Business.

This is key CoR power:

> Any handler can terminate pipeline.

---

# 🔟 Another Break Example — Rate Limit

```java
Request req =
   new Request("blockedUser", true, "order-data");
```

Flow:

```
Logging → Auth → RateLimit → STOP
```

Output:

```
Logging request for user: blockedUser
Auth success
Rate limit exceeded
```

Business never executes.

---

# 1️⃣1️⃣ Mental Model — Relay Race

Think baton passing:

```
Runner 1 → Runner 2 → Runner 3 → Runner 4
```

If Runner 2 drops baton → race ends.

---

# 1️⃣2️⃣ Memory + Stack Combined Diagram

```
HEAP (objects)

LoggingHandler ──► AuthHandler ──► RateLimit ──► Business

STACK (calls)

main
 └─ Logging.handle
     └─ Auth.handle
         └─ RateLimit.handle
             └─ Business.handle
```

Handlers live in heap.
Calls stack recursively.

---

# 1️⃣3️⃣ Why CoR Uses Recursion Instead of Loop

Could we loop?

Yes — but recursion gives:

* Cleaner delegation
* Polymorphic flow
* No central controller

Each handler controls continuation.

---

# 🔑 Final Understanding

You just saw 3 layers:

---

### 1. Structural

Linked handlers (like linked list)

---

### 2. Execution

Sequential delegation

---

### 3. Control flow

Recursive call stack

---

So yes:

> Chain of Responsibility internally behaves like recursion over a linked object graph.

---

If you want next, we can visualize even deeper:

* Add **pre + post processing** (like interceptors)
* Async chain (CompletableFuture)
* Exception bubbling
* Retry handlers
* Transaction rollback chains

Those make CoR feel like real backend middleware engines.
