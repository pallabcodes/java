# Real Intent of Command Pattern

Command is used when you want to:

Queue actions

Log actions

Undo/redo

Retry

Schedule

Decouple sender from receiver



// Receiver (Business Logic Owner)
class UserService {
    public String createUser(String name) {
        return "User created: " + name;
    }
}

- Receiver = actual worker.

- Command should delegate to it.

// Command Interface
interface Command {
    String execute();
}

// Concrete command
class CreateUserCommand implements Command {

    private final UserService userService;
    private final String name;

    public CreateUserCommand(UserService userService, String name) {
        this.userService = userService;
        this.name = name;
    }

    @Override
    public String execute() {
        return userService.createUser(name);
    }
}

- Now command is a wrapper around receiver call.


// Proper Invoker (Not main method) 

- Invoker should not know business logic

class CommandInvoker {
    public void run(Command command) {
        System.out.println(command.execute());
    }
}


public class App {

    public static void main(String[] args) {

        UserService userService = new UserService();

        Command cmd = new CreateUserCommand(userService, "Alice");
        
        CommandInvoker invoker = new CommandInvoker();

        invoker.run(cmd);
    }
}


5️⃣ Why This Matters (Real Value)

Now you can do things impossible before.

Queue<Command> queue = new LinkedList<>();

queue.add(new CreateUserCommand(userService, "Alice"));
queue.add(new CreateUserCommand(userService, "Bob"));

while (!queue.isEmpty()) {
    System.out.println(queue.poll().execute());
}

Used in: Job queues, Background workers

-- Logging/Audit Trail

class LoggingInvoker {

    public void run(Command command) {

        System.out.println("Executing: "
            + command.getClass().getSimpleName());

        System.out.println(command.execute());
    }
}


// Retry Mechanism

class RetryInvoker {

    public void run(Command command) {

        for (int i = 0; i < 3; i++) {
            try {
                System.out.println(command.execute());
                return;
            } catch (Exception e) {
                System.out.println("Retrying...");
            }
        }
    }
}

// UNDO/REDO

interface Command {
    void execute();
    void undo();
}


class CreateUserCommand implements Command {

    private UserService service;
    private String name;

    public CreateUserCommand(UserService s, String n) {
        this.service = s;
        this.name = n;
    }

    public void execute() {
        service.createUser(name);
    }

    public void undo() {
        service.deleteUser(name);
    }
}

- Now you can maintain history stack.

-- More, in-depth

Nice — now you’re moving from **Adapter → Command**, which is a good progression because both are behavioral wrappers but solve different problems.

Your example is again **syntactically correct but toy-level** — similar to your earlier adapter snippet.

So let’s do the same exercise:

1️⃣ Validate your current code
2️⃣ Explain what’s missing architecturally
3️⃣ Refactor into real-world design
4️⃣ Remove hidden smells (like we did with if/else)
5️⃣ Show production patterns built on Command

---

# 1️⃣ Your Current Example — What It Shows

```java
interface Command {
    String execute();
}

class CreateUserCommand implements Command {

    private final String name;

    public CreateUserCommand(String n){
        this.name = n;
    }

    public String execute(){
        return "created:" + name;
    }
}

class Invoker {
    public static void main(String[] args){
        Command c = new CreateUserCommand("Alice");
        System.out.println(c.execute());
    }
}
```

This demonstrates:

✔ Encapsulation of action as object
✔ Polymorphic execution
✔ Basic Command structure

But it hides the **real reason Command exists**.

---

# 2️⃣ Real Intent of Command Pattern

Command is used when you want to:

* Queue actions
* Log actions
* Undo/redo
* Retry
* Schedule
* Decouple sender from receiver

So structure should look like:

```
Invoker → Command → Receiver
```

Your example skipped **Receiver** entirely.

---

# 3️⃣ Add the Missing Piece — Receiver

Let’s refactor properly.

---

## Step 1 — Receiver (Business logic owner)

```java
class UserService {

    public String createUser(String name) {
        return "User created: " + name;
    }
}
```

Receiver = actual worker.

Command should delegate to it.

---

## Step 2 — Command Interface

```java
interface Command {
    String execute();
}
```

---

## Step 3 — Concrete Command

```java
class CreateUserCommand implements Command {

    private final UserService userService;
    private final String name;

    public CreateUserCommand(UserService userService, String name) {
        this.userService = userService;
        this.name = name;
    }

    @Override
    public String execute() {
        return userService.createUser(name);
    }
}
```

Now command is a wrapper around receiver call.

---

# 4️⃣ Proper Invoker (Not main method)

Invoker should not know business logic.

```java
class CommandInvoker {

    public void run(Command command) {
        System.out.println(command.execute());
    }
}
```

---

## Main

```java
public class App {

    public static void main(String[] args) {

        UserService userService = new UserService();

        Command cmd =
            new CreateUserCommand(userService, "Alice");

        CommandInvoker invoker = new CommandInvoker();

        invoker.run(cmd);
    }
}
```

Now structure is correct.

---

# 5️⃣ Why This Matters (Real Value)

Now you can do things impossible before.

---

## 5.1 Queue Commands

```java
Queue<Command> queue = new LinkedList<>();

queue.add(new CreateUserCommand(userService, "Alice"));
queue.add(new CreateUserCommand(userService, "Bob"));

while (!queue.isEmpty()) {
    System.out.println(queue.poll().execute());
}
```

Used in:

* Job queues
* Background workers

---

## 5.2 Logging / Audit Trail

```java
class LoggingInvoker {

    public void run(Command command) {

        System.out.println("Executing: "
            + command.getClass().getSimpleName());

        System.out.println(command.execute());
    }
}
```

---

## 5.3 Retry Mechanism

```java
class RetryInvoker {

    public void run(Command command) {

        for (int i = 0; i < 3; i++) {
            try {
                System.out.println(command.execute());
                return;
            } catch (Exception e) {
                System.out.println("Retrying...");
            }
        }
    }
}
```

---

## 5.4 Undo Support (Classic Command Feature)

Add undo contract:

```java
interface Command {
    void execute();
    void undo();
}
```

Concrete command:

```java
class CreateUserCommand implements Command {

    private UserService service;
    private String name;

    public CreateUserCommand(UserService s, String n) {
        this.service = s;
        this.name = n;
    }

    public void execute() {
        service.createUser(name);
    }

    public void undo() {
        service.deleteUser(name);
    }
}
```

Now you can maintain history stack.

---

# 6️⃣ Real-World Production Use Cases

Command is everywhere.

---

## 6.1 UI Frameworks

Button click → command object.

```
Button → ClickCommand → Action
```

---

## 6.2 CQRS / Event Sourcing

Commands represent intent:

* CreateUserCommand
* UpdateEmailCommand

---

## 6.3 Message Brokers

Kafka / RabbitMQ consumers execute commands.

---

## 6.4 CLI Tools

Each CLI action = command object.

---

# 7️⃣ Remove Hidden Smell in Your Example

Your original version tightly coupled:

```
Invoker → CreateUserCommand → logic
```

No receiver separation.

This makes:

* Testing harder
* Reuse harder
* Undo impossible

Refactored version fixes this.

---

# 8️⃣ Adapter vs Command (Quick Contrast)

Since you just studied Adapter:

| Aspect     | Adapter                   | Command              |
| ---------- | ------------------------- | -------------------- |
| Purpose    | Interface translation     | Action encapsulation |
| Focus      | Compatibility             | Execution            |
| Example    | Stripe → PaymentProcessor | CreateUser action    |
| Adds undo? | ❌                         | ✔                    |
| Queuable?  | ❌                         | ✔                    |

---

# 9️⃣ Production-Grade Enhancement

Add command metadata.

```java
interface Command {

    String execute();

    default String name() {
        return this.getClass().getSimpleName();
    }
}
```

Invoker:

```java
class AuditInvoker {

    public void run(Command command) {

        System.out.println("Running: " + command.name());

        System.out.println(command.execute());
    }
}
```

Now you have audit logs.

---

# 🔑 Final Take

Your example is:

✔ Structurally correct
✔ Minimal GoF form
❌ Missing receiver
❌ No decoupling benefit yet

Production Command requires:

```
Invoker → Command → Receiver
```

Where:

* Invoker triggers
* Command wraps
* Receiver executes

---

If you want, next we can go deeper into:

* Command + Queue + Worker architecture
* Command Bus (used in NestJS, Symfony, Spring)
* CQRS command modeling
* Macro commands (batch execution)
* Command vs Strategy confusion

That’s where Command becomes architectural, not just OO syntax.
