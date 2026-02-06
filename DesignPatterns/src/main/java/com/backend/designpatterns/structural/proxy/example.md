Real-World Example 1 — Database Query Caching Proxy
Problem

DB calls are expensive:

 - Network latency

 - Disk I/O

 - Query execution cost

If the same user is fetched 100× → wasteful.

So we put a proxy cache in front.


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//
// 1️⃣ Domain Model
//
class User {

    private final String id;
    private final String name;
    private final String email;

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }
}

//
// 2️⃣ Subject Interface
//
interface UserRepository {

    User getUserById(String id);
}

//
// 3️⃣ Real Object (DB Layer)
//
class DatabaseUserRepository implements UserRepository {

    @Override
    public User getUserById(String id) {

        simulateSlowDatabaseCall();

        System.out.println("DB → Fetching user " + id);

        // Fake DB record
        return new User(
                id,
                "John Doe " + id,
                "john" + id + "@example.com"
        );
    }

    private void simulateSlowDatabaseCall() {
        try {
            Thread.sleep(1000); // simulate latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

//
// 4️⃣ Proxy (Caching Layer)
//
class CachedUserRepositoryProxy implements UserRepository {

    private final UserRepository realRepo;

    // Thread-safe cache
    private final Map<String, User> cache =
            new ConcurrentHashMap<>();

    public CachedUserRepositoryProxy(UserRepository repo) {
        this.realRepo = repo;
    }

    @Override
    public User getUserById(String id) {

        // 1️⃣ Try cache first
        User cachedUser = cache.get(id);

        if (cachedUser != null) {
            System.out.println("CACHE HIT → " + id);
            return cachedUser;
        }

        // 2️⃣ Fallback to DB
        System.out.println("CACHE MISS → " + id);

        User user = realRepo.getUserById(id);

        // 3️⃣ Store in cache
        cache.put(id, user);

        return user;
    }
}

//
// 5️⃣ Client / App Runner
//
public class Main {

    public static void main(String[] args) {

        // Real repository
        UserRepository dbRepo =
                new DatabaseUserRepository();

        // Proxy wrapping real repo
        UserRepository cachedRepo =
                new CachedUserRepositoryProxy(dbRepo);

        // First call → DB
        System.out.println(
                cachedRepo.getUserById("101")
        );

        System.out.println();

        // Second call → Cache
        System.out.println(
                cachedRepo.getUserById("101")
        );

        System.out.println();

        // New user → DB again
        System.out.println(
                cachedRepo.getUserById("202")
        );

        System.out.println();

        // Cached again
        System.out.println(
                cachedRepo.getUserById("202")
        );
    }
}

N.B: A decorator and Proxy quite similar but Proxy controls access to the real object and may decide whether the call reaches it at all.

A Decorator primarily enhances or adds behavior while still delegating to the real object.
A Proxy, on the other hand, controls access to the real object and may decide whether the call reaches it at all.

If a wrapper starts blocking or short-circuiting access, it is more accurately described as a Proxy rather than a Decorator (so yes unintenally or badly written decorator can block access to real object then it stops being a decorator and it should be called Proxy even if it was written with intention of being a decorator)