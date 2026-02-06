1️⃣ Real-World Example — E-Commerce Order Facade
Problem

Placing an order involves:

1. User authentication

2. Inventory check

3. Payment processing

4. Shipping booking

5. Notification

Without Facade → client must orchestrate all.

Facade → one call: placeOrder().

class AuthService {
    public boolean authenticate(String userId) {
        System.out.println("Authenticating user...");
        return true;
    }
}

class InventoryService {
    public boolean checkStock(String itemId) {
        System.out.println("Checking stock...");
        return true;
    }
}

class PaymentService {
    public boolean processPayment(double amount) {
        System.out.println("Processing payment...");
        return true;
    }
}

class ShippingService {
    public void ship(String itemId) {
        System.out.println("Shipping item...");
    }
}


// Each is independent → complex to coordinate.

class OrderFacade {

    private AuthService auth = new AuthService();
    private InventoryService inventory = new InventoryService();
    private PaymentService payment = new PaymentService();
    private ShippingService shipping = new ShippingService();

    public String placeOrder(String userId, String itemId, double amount) {

        if (!auth.authenticate(userId))
            return "Auth failed";

        if (!inventory.checkStock(itemId))
            return "Out of stock";

        if (!payment.processPayment(amount))
            return "Payment failed";

        shipping.ship(itemId);

        return "Order placed successfully";
    }
}


public class Main {
    public static void main(String[] args) {

        OrderFacade facade = new OrderFacade();

        String result =
            facade.placeOrder("user1", "item1", 5000);

        System.out.println(result);
    }
}

// So, Facade is a typical (left to right or right ot left compose) where argument passed to fist function then it process it then pass the needed arguments to next function and so on.


2️⃣ Microservices API Gateway Facade

Very production-realistic.

Problem

Frontend needs:

User data

Orders

Recommendations

Instead of calling 3 services → call 1 gateway.

class UserService {
    public String getUser(String id) {
        return "User:" + id;
    }
}

class OrderService {
    public String getOrders(String userId) {
        return "Orders for " + userId;
    }
}

class RecommendationService {
    public String getRecs(String userId) {
        return "Recommendations for " + userId;
    }
}


class UserDashboardFacade {

    private UserService user = new UserService();
    private OrderService order = new OrderService();
    private RecommendationService rec = new RecommendationService();

    public String getDashboard(String userId) {

        String profile = user.getUser(userId);
        String orders = order.getOrders(userId);
        String recommendations = rec.getRecs(userId);

        return profile + "\n" + orders + "\n" + recommendations;
    }
}
