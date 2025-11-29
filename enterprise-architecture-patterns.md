# Chapter 9

Here is the content extracted from **Chapter 9: Domain Logic Patterns** of *Patterns of Enterprise Application Architecture*, formatted in Markdown.

# Chapter 9: Domain Logic Patterns

## Transaction Script

[cite\_start]**Organizes business logic by procedures where each procedure handles a single request from the presentation.** [cite: 940]

Most business applications can be thought of as a series of transactions. A transaction may view some information as organized in a particular way, another will make changes to it. Each interaction between a client system and a server system contains a certain amount of logic. In some cases this can be as simple as displaying information in the database. In others it may involve many steps of validations and calculations.

A **Transaction Script** organizes all this logic primarily as a single procedure, making calls directly to the database or through a thin database wrapper. Each transaction will have its own Transaction Script, although common subtasks can be broken into subprocedures.

### How It Works

With Transaction Script the domain logic is primarily organized by the transactions that you carry out with the system. [cite\_start]If your need is to book a hotel room, the logic to check room availability, calculate rates, and update the database is found inside the `Book Hotel Room` procedure[cite: 940].

For simple cases there isn’t much to say about how you organize this. Of course, as with any other program you should structure the code into modules in a way that makes sense. Unless the transaction is particularly complicated, that won’t be much of a challenge. One of the benefits of this approach is that you don’t need to worry about what other transactions are doing. Your task is to get the input, interrogate the database, munge, and save your results to the database.

Where you put the Transaction Script will depend on how you organize your layers. It may be in a server page, a CGI script, or a distributed session object. My preference is to separate Transaction Scripts as much as you can. At the very least put them in distinct subroutines; better still, put them in classes separate from those that handle presentation and data source. [cite\_start]In addition, don’t have any calls from the Transaction Scripts to any presentation logic; that will make it easier to modify the code and test the Transaction Scripts [cite: 940-941].

You can organize your Transaction Scripts into classes in two ways. The most common is to have several Transaction Scripts in a single class, where each class defines a subject area of related Transaction Scripts. This is straightforward and the best bet for most cases. The other way is to have each Transaction Script in its own class, using the Command pattern. In this case you define a supertype for your commands that specifies some execute method in which Transaction Script logic fits. [cite\_start]The advantage of this is that it allows you to manipulate instances of scripts as objects at runtime, although I’ve rarely seen a need to do this with the kinds of systems that use Transaction Scripts to organize domain logic[cite: 941].

### When to Use It

The glory of Transaction Script is its simplicity. Organizing logic this way is natural for applications with only a small amount of logic, and it involves very little overhead either in performance or in understanding.

As the business logic gets more complicated, however, it gets progressively harder to keep it in a well-designed state. One particular problem to watch for is its duplication between transactions. Since the whole point is to handle one transaction, any common code tends to be duplicated.

Careful factoring can alleviate many of these problems, but more complex business domains need to build a **Domain Model (116)**. A Domain Model will give you many more options in structuring the code, increasing readability and decreasing duplication.

It’s hard to quantify the cutover level, especially when you’re more familiar with one pattern than the other. You can refactor a Transaction Script design to a Domain Model design, but it’s a harder change than it otherwise needs to be. Therefore, an early shot is often the best way to move forward.

However much of an object bigot you become, don’t rule out Transaction Script. [cite\_start]There are a lot of simple problems out there, and a simple solution will get you up and running much faster [cite: 941-942].

### The Revenue Recognition Problem

Revenue recognition is a common problem in business systems. It’s all about when you can actually count the money you receive on your books. If I sell you a cup of coffee, it’s a simple matter: I give you the coffee, I take your money, and I count the money to the books that nanosecond. For many things it gets complicated, however. Say you pay me a retainer to be available that year. Even if you pay me some ridiculous fee today, I may not be able to put it on my books right away because the service is to be performed over the course of a year. One approach might be to count only one-twelfth of that fee for each month in the year, since you might pull out of the contract after a month when you realize that writing has atrophied my programming skills.

The rules for revenue recognition are many, various, and volatile. Some are set by regulation, some by professional standards, and some by company policy. Revenue tracking ends up being quite a complex problem.

I don’t fancy delving into the complexity right now, so instead we’ll imagine a company that sells three kinds of products: word processors, databases, and spreadsheets. According to the rules, when you sign a contract for a word processor you can book all the revenue right away. If it’s a spreadsheet, you can book one-third today, one-third in sixty days, and one-third in ninety days. [cite\_start]If it’s a database, you can book one-third today, one-third in thirty days, and one-third in sixty days[cite: 942].

### Example: Revenue Recognition (Java)

This example uses two transaction scripts: one to calculate the revenue recognitions for a contract and one to tell how much revenue on a contract has been recognized by a certain date. [cite\_start]The database structure has three tables: one for the products, one for the contracts, and one for the revenue recognitions[cite: 943].

```sql
CREATE TABLE products (ID int primary key, name varchar, type varchar)
CREATE TABLE contracts (ID int primary key, product int, revenue decimal, dateSigned date)
CREATE TABLE revenueRecognitions (contract int, amount decimal, recognizedOn date, PRIMARY KEY (contract, recognizedOn))
```

The first script calculates the amount of recognition due by a particular day. I can do this in two stages: In the first I select the appropriate rows in the revenue recognitions table; in the second I sum up the amounts.

Many Transaction Script designs have scripts that operate directly on the database, putting SQL code in the procedure. Here I’m using a simple **Table Data Gateway (144)** to wrap the SQL queries. Since this example is so simple, I’m using a single gateway rather than one for each table. [cite\_start]I can define an appropriate find method on the gateway[cite: 943].

```java
class Gateway...
    public ResultSet findRecognitionsFor(long contractID, MfDate asof) throws SQLException{
        PreparedStatement stmt = db.prepareStatement(findRecognitionsStatement);
        stmt.setLong(1, contractID);
        stmt.setDate(2, asof.toSqlDate());
        ResultSet result = stmt.executeQuery();
        return result;
    }
    
    private static final String findRecognitionsStatement =
        "SELECT amount " +
        " FROM revenueRecognitions " +
        " WHERE contract = ? AND recognizedOn <= ?";
    private Connection db;
```

[cite\_start]I then use the script to sum up based on the result set passed back from the gateway[cite: 944].

```java
class RecognitionService...
    public Money recognizedRevenue(long contractNumber, MfDate asOf) {
        Money result = Money.dollars(0);
        try {
            ResultSet rs = db.findRecognitionsFor(contractNumber, asOf);
            while (rs.next()) {
                result = result.add(Money.dollars(rs.getBigDecimal("amount")));
            }
            return result;
        } catch (SQLException e) {throw new ApplicationException (e);
        }
    }
```

For calculating the revenue recognitions on an existing contract, I use a similar split. [cite\_start]The script on the service carries out the business logic [cite: 944-945].

```java
class RecognitionService...
    public void calculateRevenueRecognitions(long contractNumber) {
        try {
            ResultSet contracts = db.findContract(contractNumber);
            contracts.next();
            Money totalRevenue = Money.dollars(contracts.getBigDecimal("revenue"));
            MfDate recognitionDate = new MfDate(contracts.getDate("dateSigned"));
            String type = contracts.getString("type");
            
            if (type.equals("S")){
                Money[] allocation = totalRevenue.allocate(3);
                db.insertRecognition(contractNumber, allocation[0], recognitionDate);
                db.insertRecognition(contractNumber, allocation[1], recognitionDate.addDays(60));
                db.insertRecognition(contractNumber, allocation[2], recognitionDate.addDays(90));
            } else if (type.equals("W")){
                db.insertRecognition(contractNumber, totalRevenue, recognitionDate);
            } else if (type.equals("D")) {
                Money[] allocation = totalRevenue.allocate(3);
                db.insertRecognition(contractNumber, allocation[0], recognitionDate);
                db.insertRecognition(contractNumber, allocation[1], recognitionDate.addDays(30));
                db.insertRecognition(contractNumber, allocation[2], recognitionDate.addDays(60));
            }
        } catch (SQLException e) {throw new ApplicationException (e);
        }
    }
```

Notice that I’m using **Money (488)** to carry out the allocation. [cite\_start]When splitting an amount three ways it’s very easy to lose a penny[cite: 945].

-----

## Domain Model

[cite\_start]**An object model of the domain that incorporates both behavior and data.** [cite: 946]

At its worst business logic can be very complex. Rules and logic describe many different cases and slants of behavior, and it’s this complexity that objects were designed to work with. A **Domain Model** creates a web of interconnected objects, where each object represents some meaningful individual, whether as large as a corporation or as small as a single line on an order form.

### How It Works

Putting a Domain Model in an application involves inserting a whole layer of objects that model the business area you’re working in. You’ll find objects that mimic the data in the business and objects that capture the rules the business uses. [cite\_start]Mostly the data and process are combined to cluster the processes close to the data they work with[cite: 946].

An OO domain model will often look similar to a database model, yet it will still have a lot of differences. A Domain Model mingles data and process, has multivalued attributes and a complex web of associations, and uses inheritance.

As a result I see two styles of Domain Model in the field. A simple Domain Model looks very much like the database design with mostly one domain object for each database table. A rich Domain Model can look different from the database design, with inheritance, strategies, and other Gang of Four patterns, and complex webs of small interconnected objects. [cite\_start]A rich Domain Model is better for more complex logic, but is harder to map to the database[cite: 947].

Since the behavior of the business is subject to a lot of change, it’s important to be able to modify, build, and test this layer easily. As a result you’ll want the minimum of coupling from the Domain Model to other layers in the system.

With a Domain Model there are a number of different scopes you might use. The simplest case is a single-user application where the whole object graph is read from a file and put into memory. Without an OO database you have to do this yourself. Usually a session will involve pulling in an object graph of all the objects involved in it. This will certainly not be all objects and usually not all the classes.

**Java Implementation**
There’s always a lot of heat generated when people talk about developing a Domain Model in J2EE. [cite\_start]Many of the teaching materials and introductory J2EE books suggest that you use entity beans to develop a domain model, but there are some serious problems with this approach, at least with the current (2.0) specification[cite: 948].

  * Entity beans can't be re-entrant.
  * Entity beans may be remotable. If you have remote objects with fine-grained interfaces you get terrible performance.
  * To run with entity beans you need a container and a database connected. This will increase build times.

The alternative is to use normal Java objects (POJOs). My view on the whole is that using entity beans as a Domain Model works if you have pretty modest domain logic. [cite\_start]If you have a richer domain logic, you’re better off with a POJO domain model and **Data Mapper (165)** [cite: 948-949].

### When to Use It

It all comes down to the complexity of the behavior in your system. If you have complicated and everchanging business rules involving validation, calculations, and derivations, chances are that you’ll want an object model to handle them. [cite\_start]On the other hand, if you have simple not-null checks and a couple of sums to calculate, a **Transaction Script (110)** is a better bet[cite: 949].

### Example: Revenue Recognition (Java)

I’m using the same example (page 112) that I used for Transaction Script (110), a little matter of revenue recognition. An immediate thing to notice is that every class, in this small example (Figure 9.3) contains both behavior and data. [cite\_start]Even the humble Revenue Recognition class contains a simple method to find out if that object’s value is recognizable on a certain date[cite: 950].

```java
class RevenueRecognition...
    private Money amount;
    private MfDate date;
    public RevenueRecognition(Money amount, MfDate date) {
        this.amount = amount;
        this.date = date;
    }
    public Money getAmount() {
        return amount;
    }
    boolean isRecognizableBy(MfDate asOf) {
        return asOf.after(date) || asOf.equals(date);
    }
```

[cite\_start]Calculating how much revenue is recognized on a particular date involves both the contract and revenue recognition classes[cite: 950].

```java
class Contract...
    private List revenueRecognitions = new ArrayList();
    public Money recognizedRevenue(MfDate asOf) {
        Money result = Money.dollars(0);
        Iterator it = revenueRecognitions.iterator();
        while (it.hasNext()) {
            RevenueRecognition r = (RevenueRecognition) it.next();
            if (r.isRecognizableBy(asOf))
                result = result.add(r.getAmount());
        }
        return result;
    }
```

Looking at calculating and creating these revenue recognition objects further demonstrates the notion of lots of little objects. In this case the calculation and creation begin with the customer and are handed off via the product to a strategy hierarchy. [cite\_start]The strategy pattern allows you to combine a group of operations in a small class hierarchy[cite: 952].

```java
class Contract...
    private Product product;
    private Money revenue;
    private MfDate whenSigned;
    private Long id;

class Product...
    private String name;
    private RecognitionStrategy recognitionStrategy;
    public static Product newWordProcessor(String name) {
        return new Product(name, new CompleteRecognitionStrategy());
    }
    public static Product newSpreadsheet(String name) {
        return new Product(name, new ThreeWayRecognitionStrategy(60, 90));
    }
    public static Product newDatabase(String name) {
        return new Product(name, new ThreeWayRecognitionStrategy(30, 60));
    }

class RecognitionStrategy...
    abstract void calculateRevenueRecognitions(Contract contract);

class CompleteRecognitionStrategy...
    void calculateRevenueRecognitions(Contract contract) {
        contract.addRevenueRecognition(new RevenueRecognition(contract.getRevenue(), contract.getWhenSigned()));
    }

class ThreeWayRecognitionStrategy...
    void calculateRevenueRecognitions(Contract contract) {
        Money[] allocation = contract.getRevenue().allocate(3);
        contract.addRevenueRecognition(new RevenueRecognition(allocation[0], contract.getWhenSigned()));
        contract.addRevenueRecognition(new RevenueRecognition(allocation[1], contract.getWhenSigned().addDays(firstRecognitionOffset)));
        contract.addRevenueRecognition(new RevenueRecognition(allocation[2], contract.getWhenSigned().addDays(secondRecognitionOffset)));
    }
```

[cite\_start]Once everything is set up, calculating the recognitions requires no knowledge of the strategy subclasses[cite: 953].

```java
class Contract...
    public void calculateRecognitions() {
        product.calculateRevenueRecognitions(this);
    }
class Product...
    void calculateRevenueRecognitions(Contract contract) {
        recognitionStrategy.calculateRevenueRecognitions(contract);
    }
```

-----

## Table Module

[cite\_start]**A single instance that handles the business logic for all rows in a database table or view.** [cite: 955]

[Image of Table Module Pattern Structure]

One of the key messages of object orientation is bundling the data with the behavior that uses it. The traditional object-oriented approach is based on objects with identity, along the lines of **Domain Model (116)**. Thus, if we have an Employee class, any instance of it corresponds to a particular employee.

A **Table Module** organizes domain logic with one class per table in the database, and a single instance of a class contains the various procedures that will act on the data. The primary distinction with Domain Model (116) is that, if you have many orders, a Domain Model will have one order object per order while a Table Module will have one object to handle all orders.

### How It Works

The strength of Table Module is that it allows you to package the data and behavior together and at the same time play to the strengths of a relational database. On the surface Table Module looks much like a regular object. The key difference is that it has no notion of an identity for the objects it’s working with. Thus, if you want to obtain the address of an employee, you use a method like `anEmployeeModule.getAddress(long employeeID)`. Every time you want to do something to a particular employee you have to pass in some kind of identity reference. [cite\_start]Often this will be the primary key used in the database[cite: 956].

Usually you use Table Module with a backing data structure that’s table oriented. The tabular data is normally the result of a SQL call and is held in a **Record Set (508)** that mimics a SQL table.

The Table Module may be an instance or it may be a collection of static methods. The advantage of an instance is that it allows you to initialize the Table Module with an existing record set, perhaps the result of a query. [cite\_start]You can then use the instance to manipulate the rows in the record set[cite: 957].

### When to Use It

Table Module is very much based on table-oriented data, so obviously using it makes sense when you’re accessing tabular data using **Record Set (508)**. It also puts that data structure very much in the center of the code, so you also want the way you access the data structure to be fairly straightforward.

However, Table Module doesn’t give you the full power of objects in organizing complex logic. You can’t have direct instance-to-instance relationships, and polymorphism doesn’t work well. [cite\_start]So, for handling complicated domain logic, a **Domain Model (116)** is a better choice[cite: 958].

The most well-known situation in which I’ve come across this pattern is in Microsoft COM designs. In COM (and .NET) the Record Set is the primary repository of data in an application.

### Example: Revenue Recognition with a Table Module (C\#)

Time to revisit the revenue recognition example (page 112) I used in the other domain modeling patterns, this time with a Table Module. In this example we have different rules for word processors, spreadsheets, and databases.

[Image of Database Schema for Revenue Recognition]

The classes that manipulate this data are in pretty much the same form; there’s one Table Module class for each table. In the .NET architecture a data set object provides an in-memory representation of a database structure. Each Table Module class has a data member of a data table, which is the .NET system class corresponding to a table within the data set. [cite\_start]This ability to read a table is common to all Table Modules and so can appear in a **Layer Supertype (475)**[cite: 959].

```csharp
class TableModule...
    protected DataTable table;
    protected TableModule(DataSet ds, String tableName) {
        table = ds.Tables[tableName];
    }
```

The first piece of functionality calculates the revenue recognition for a contract, updating the revenue recognition table accordingly. The amount recognized depends on the kind of product we have. [cite\_start]Since this behavior mainly uses data from the contract table, I decided to add the method to the contract class[cite: 960].

```csharp
class Contract...
    public void CalculateRecognitions (long contractID) {
        DataRow contractRow = this[contractID];
        Decimal amount = (Decimal)contractRow["amount"];
        RevenueRecognition rr = new RevenueRecognition (table.DataSet);
        Product prod = new Product(table.DataSet);
        long prodID = GetProductId(contractID);
        
        if (prod.GetProductType(prodID) == ProductType.WP) {
            rr.Insert(contractID, amount, (DateTime) GetWhenSigned(contractID));
        } else if (prod.GetProductType(prodID) == ProductType.SS) {
            Decimal[] allocation = allocate(amount,3);
            rr.Insert(contractID, allocation[0], (DateTime) GetWhenSigned(contractID));
            rr.Insert(contractID, allocation[1], (DateTime) GetWhenSigned(contractID).AddDays(60));
            rr.Insert(contractID, allocation[2], (DateTime) GetWhenSigned(contractID).AddDays(90));
        } else if (prod.GetProductType(prodID) == ProductType.DB) {
            Decimal[] allocation = allocate(amount,3);
            rr.Insert(contractID, allocation[0], (DateTime) GetWhenSigned(contractID));
            rr.Insert(contractID, allocation[1], (DateTime) GetWhenSigned(contractID).AddDays(30));
            rr.Insert(contractID, allocation[2], (DateTime) GetWhenSigned(contractID).AddDays(60));
        } else throw new Exception("invalid product id");
    }
```

To carry this out, we need some behavior that’s defined on the other classes. The product needs to be able to tell us which type it is. [cite\_start]We can do this with an enum for the product type and a lookup method[cite: 961].

```csharp
class Product...
    public ProductType GetProductType (long id) {
        String typeCode = (String) this[id]["type"];
        return (ProductType) Enum.Parse(typeof(ProductType), typeCode);
    }
```

The second piece of functionality is to sum up all the revenue recognized on a contract by a given date. [cite\_start]Since this uses the revenue recognition table it makes sense to define the method there[cite: 962].

```csharp
class RevenueRecognition...
    public Decimal RecognizedRevenue (long contractID, DateTime asOf) {
        String filter = String.Format("ContractID = {0} AND date <= #{1:d}#", contractID,asOf); 
        DataRow[] rows = table.Select(filter);
        Decimal result = 0m;
        foreach (DataRow row in rows) {
            result += (Decimal)row["amount"];
        }
        return result;
    }
```

-----

## Service Layer

[cite\_start]**Defines an application’s boundary with a layer of services that establishes a set of available operations and coordinates the application’s response in each operation.** [cite: 963]

Enterprise applications typically require different kinds of interfaces to the data they store and the logic they implement: data loaders, user interfaces, integration gateways, and others. Despite their different purposes, these interfaces often need common interactions with the application to access and manipulate its data and invoke its business logic. Encoding the logic of the interactions separately in each interface causes a lot of duplication.

A **Service Layer** defines an application’s boundary and its set of available operations from the perspective of interfacing client layers. It encapsulates the application’s business logic, controlling transactions and coordinating responses in the implementation of its operations.

### How It Works

A Service Layer can be implemented in a couple of different ways. [cite\_start]The differences appear in the allocation of responsibility behind the Service Layer interface[cite: 964].

  * **Kinds of "Business Logic":** Many designers like to divide "business logic" into two kinds: "domain logic," having to do purely with the problem domain (such as strategies for calculating revenue recognition), and "application logic," having to do with application responsibilities (such as notifying contract administrators). Service Layer factors each kind of business logic into a separate layer.
  * **Implementation Variations:**
      * **Domain Facade approach:** A Service Layer is implemented as a set of thin facades over a Domain Model. The classes implementing the facades don’t implement any business logic.
      * **Operation Script approach:** A Service Layer is implemented as a set of thicker classes that directly implement application logic but delegate to encapsulated domain object classes for domain logic.
  * **To Remote or Not:** The interface of a Service Layer class is coarse grained almost by definition. Therefore, Service Layer classes are well suited to remote invocation. However, remote invocation comes at the cost of dealing with object distribution. My advice is to start with a locally invocable Service Layer whose method signatures deal in domain objects. [cite\_start]Add remotability when you need it by putting **Remote Facades (388)** on your Service Layer[cite: 965].

**Java Implementation**
My preferred way of applying a Service Layer in J2EE is with EJB 2.0 stateless session beans, using local interfaces, and the operation script approach, delegating to POJO domain object classes. [cite\_start]It’s just so darned convenient to implement a Service Layer using stateless session bean, because of the distributed container-managed transactions provided by EJB[cite: 966].

### When to Use It

The benefit of Service Layer is that it defines a common set of application operations available to many kinds of clients and it coordinates an application’s response in each operation. The response may involve application logic that needs to be transacted atomically across multiple transactional resources. Thus, in an application with more than one kind of client of its business logic, and complex responses in its use cases involving multiple transactional resources, it makes a lot of sense to include a Service Layer with container-managed transactions, even in an undistributed architecture.

[cite\_start]You probably don’t need a Service Layer if your application’s business logic will only have one kind of client—say, a user interface—and its use case responses don’t involve multiple transactional resources[cite: 967].

### Example: Revenue Recognition (Java)

This example continues the revenue recognition example of the Transaction Script and Domain Model patterns, demonstrating how Service Layer is used to script application logic and delegate for domain logic in a Service Layer operation. It uses the operation script approach to implement a Service Layer, first with POJOs and then with EJBs.

We start by changing the `RecognitionService` class from the Transaction Script example to extend a **Layer Supertype (475)** and to use a couple of **Gateways (466)** in carrying out application logic. [cite\_start]`RecognitionService` becomes a POJO implementation of a Service Layer application service[cite: 968].

```java
public class RecognitionService extends ApplicationService {
    public void calculateRevenueRecognitions(long contractNumber) {
        Contract contract = Contract.readForUpdate(contractNumber);
        contract.calculateRecognitions();
        getEmailGateway().sendEmailMessage(
            contract.getAdministratorEmailAddress(),
            "RE: Contract #" + contractNumber,
            contract + " has had revenue recognitions calculated.");
        getIntegrationGateway().publishRevenueRecognitionCalculation(contract);
    }
    public Money recognizedRevenue(long contractNumber, Date asOf) {
        return Contract.read(contractNumber).recognizedRevenue(asOf);
    }
}
```

Persistence details are again left out of the example. Suffice it to say that the Contract class implements static methods to read contracts from the Data Source layer. Transaction control details are also left out of the example. [cite\_start]The `calculateRevenueRecognitions()` method is inherently transactional because, during its execution, persistent contract objects are modified; messages are enqueued in message-oriented middleware; and e-mail messages are sent[cite: 970].

In the J2EE platform we can let the EJB container manage distributed transactions by implementing application services as stateless session beans. Figure 9.8 shows the class diagram of a `RecognitionService` implementation that uses EJB 2.0 local interfaces and the "business interface" idiom.

The important point about the example is that the Service Layer uses both operation scripting and domain object classes in coordinating the transactional response of the operation. [cite\_start]The `calculateRevenueRecognitions` method scripts the application logic of the response required by the application’s use cases, but it delegates to the domain object classes for domain logic[cite: 970].

# Chapter 10

Here is the content extracted from **Chapter 10: Data Source Architectural Patterns** of *Patterns of Enterprise Application Architecture*, formatted in Markdown.

# Chapter 10: Data Source Architectural Patterns

## Table Data Gateway

**An object that acts as a Gateway (466) to a database table. One instance handles all the rows in the table.**

Mixing SQL in application logic can cause several problems. Many developers aren’t comfortable with SQL, and many who are comfortable may not write it well. Database administrators need to be able to find SQL easily so they can figure out how to tune and evolve the database.

A **Table Data Gateway** holds all the SQL for accessing a single table or view: selects, inserts, updates, and deletes. Other code calls its methods for all interaction with the database.

### How It Works

A Table Data Gateway has a simple interface, usually consisting of several find methods to get data from the database and update, insert, and delete methods. Each method maps the input parameters into a SQL call and executes the SQL against a database connection. The Table Data Gateway is usually stateless, as its role is to push data back and forth.

The trickiest thing about a Table Data Gateway is how it returns information from a query. Even a simple find-by-ID query will return multiple data items. In environments where you can return multiple items you can use that for a single row, but many languages give you only a single return value and many queries return multiple rows.

One alternative is to return some simple data structure, such as a map. A map works, but it forces data to be copied out of the record set that comes from the database into the map. I think that using maps to pass data around is bad form because it defeats compile time checking and isn’t a very explicit interface, leading to bugs as people misspell what’s in the map. A better alternative is to use a **Data Transfer Object (401)**. It’s another object to create but one that may well be used elsewhere.

To save all this you can return the **Record Set (508)** that comes from the SQL query. This is conceptually messy, as ideally the in-memory object doesn’t have to know anything about the SQL interface. It may also make it difficult to substitute the database for a file if you can’t easily create record sets in your own code. Nevertheless, in many environments that use Record Set (508) widely, such as .NET, it’s a very effective approach. A Table Data Gateway thus goes very well with **Table Module (125)**. If all of your updates are done through the Table Data Gateway, the returned data can be based on views rather than on the actual tables, which reduces the coupling between your code and the database.

If you’re using a **Domain Model (116)**, you can have the Table Data Gateway return the appropriate domain object. The problem with this is that you then have bidirectional dependencies between the domain objects and the gateway. The two are closely connected, so that isn’t necessarily a terrible thing, but it’s something I’m always reluctant to do.

Most times when you use Table Data Gateway, you’ll have one for each table in the database. For very simple cases, however, you can have a single Table Data Gateway that handles all methods for all tables. You can also have one for views or even for interesting queries that aren’t kept in the database as views. Obviously, view-based Table Data Gateways often can’t update and so won’t have update behavior. However, if you can make updates to the underlying tables, then encapsulating the updates behind update operations on the Table Data Gateway is a very good technique.

### When to Use It

As with **Row Data Gateway (152)** the decision regarding Table Data Gateway is first whether to use a **Gateway (466)** approach at all and then which one.

I find that Table Data Gateway is probably the simplest database interface pattern to use, as it maps so nicely onto a database table or record type. It also makes a natural point to encapsulate the precise access logic of the data source. I use it least with **Domain Model (116)** because I find that **Data Mapper (165)** gives a better isolation between the Domain Model (116) and the database.

Table Data Gateway works particularly well with **Table Module (125)**, where it produces a record set data structure for the Table Module (125) to work on. Indeed, I can’t really imagine any other database-mapping approach for Table Module (125).

Just like Row Data Gateway (152), Table Data Gateway is very suitable for **Transaction Scripts (110)**. The choice between the two really boils down to how they deal with multiple rows of data. Many people like using a **Data Transfer Object (401)**, but that seems to me like more work than is worthwhile, unless the same Data Transfer Object (401) is used elsewhere. I prefer Table Data Gateway when the result set representation is convenient for the Transaction Script (110) to work with.

Interestingly, it often makes sense to have the **Data Mappers (165)** talk to the database via Table Data Gateways. Although this isn’t useful when everything is handcoded, it can be very effective if you want to use metadata for the Table Data Gateways but prefer handcoding for the actual mapping to the domain objects.

One of the benefits of using a Table Data Gateway to encapsulate database access is that the same interface can work both for using SQL to manipulate the database and for using stored procedures. Indeed, stored procedures themselves are often organized as Table Data Gateways. That way the insert and update stored procedures encapsulate the actual table structure. [cite\_start]The find procedures in this case can return views, which helps to hide the underlying table structure [cite: 974-976].

### Example: Person Gateway (C\#)

Table Data Gateway is the usual form of database access in the windows world, so it makes sense to illustrate one with C\#. I have to stress, however, that this classic form of Table Data Gateway doesn’t quite fit in the .NET environment since it doesn’t take advantage of the ADO.NET data set; instead, it uses the data reader, which is a cursor-like interface to database records. The data reader is the right choice for manipulating larger amounts of information when you don’t want to bring everything into memory in one go.

For the example I’m using a Person Gateway class that connects to a person table in a database. The Person Gateway contains the finder code, returning ADO.NET’s data reader to access the returned data.

```csharp
class PersonGateway...
    public IDataReader FindAll() {
        String sql = "select * from person";
        return new OleDbCommand(sql, DB.Connection).ExecuteReader();
    }
    public IDataReader FindWithLastName(String lastName) {
        String sql = "SELECT * FROM person WHERE lastname = ?";
        IDbCommand comm = new OleDbCommand(sql, DB.Connection);
        comm.Parameters.Add(new OleDbParameter("lastname", lastName));
        return comm.ExecuteReader();
    }
    public IDataReader FindWhere(String whereClause) {
        String sql = String.Format("select * from person where {0}", whereClause);
        return new OleDbCommand(sql, DB.Connection).ExecuteReader();
    }
```

The update and insert methods receive the necessary data in arguments and invoke the appropriate SQL routines.

```csharp
class PersonGateway...
    public void Update (long key, String lastname, String firstname, long numberOfDependents){
        String sql = @"
            UPDATE person 
            SET lastname = ?, firstname = ?, numberOfDependents = ? 
            WHERE id = ?";
        IDbCommand comm = new OleDbCommand(sql, DB.Connection);
        comm.Parameters.Add(new OleDbParameter ("last", lastname));
        comm.Parameters.Add(new OleDbParameter ("first", firstname));
        comm.Parameters.Add(new OleDbParameter ("numDep", numberOfDependents));
        comm.Parameters.Add(new OleDbParameter ("key", key));
        comm.ExecuteNonQuery();
    }
    
    public long Insert(String lastName, String firstName, long numberOfDependents) {
        String sql = "INSERT INTO person VALUES (?,?,?,?)";
        long key = GetNextID();
        IDbCommand comm = new OleDbCommand(sql, DB.Connection);
        comm.Parameters.Add(new OleDbParameter ("key", key));
        comm.Parameters.Add(new OleDbParameter ("last", lastName));
        comm.Parameters.Add(new OleDbParameter ("first", firstName));
        comm.Parameters.Add(new OleDbParameter ("numDep", numberOfDependents));
        comm.ExecuteNonQuery();
        return key;
    }
```

The deletion method just needs a key.

```csharp
class PersonGateway...
    public void Delete (long key) {
        String sql = "DELETE FROM person WHERE id = ?";
        IDbCommand comm = new OleDbCommand(sql, DB.Connection);
        comm.Parameters.Add(new OleDbParameter ("key", key));
        comm.ExecuteNonQuery();
    }
```

-----

## Row Data Gateway

**An object that acts as a Gateway (466) to a single record in a data source. There is one instance per row.**

Embedding database access code in in-memory objects can leave you with a few disadvantages. For a start, if your in-memory objects have business logic of their own, adding the database manipulation code increases complexity. Testing is awkward too since, if your in-memory objects are tied to a database, tests are slower to run because of all the database access. You may have to access multiple databases with all those annoying little variations on their SQL.

A **Row Data Gateway** gives you objects that look exactly like the record in your record structure but can be accessed with the regular mechanisms of your programming language. All details of data source access are hidden behind this interface.

### How It Works

A Row Data Gateway acts as an object that exactly mimics a single record, such as one database row. In it each column in the database becomes one field. The Row Data Gateway will usually do any type conversion from the data source types to the in-memory types, but this conversion is pretty simple. This pattern holds the data about a row so that a client can then access the Row Data Gateway directly. The gateway acts as a good interface for each row of data. This approach works particularly well for **Transaction Scripts (110)**.

With a Row Data Gateway you’re faced with the questions of where to put the find operations that generate this pattern. You can use static find methods, but they preclude polymorphism should you want to substitute different finder methods for different data sources. In this case it often makes sense to have separate finder objects so that each table in a relational database will have one finder class and one gateway class for the results.

It’s often hard to tell the difference between a Row Data Gateway and an **Active Record (160)**. The crux of the matter is whether there’s any domain logic present; if there is, you have an Active Record (160). A Row Data Gateway should contain only database access logic and no domain logic.

Row Data Gateways tend to be somewhat tedious to write, but they’re a very good candidate for code generation based on a **Metadata Mapping (306)**. [cite\_start]This way all your database access code can be automatically built for you during your automated build process [cite: 982-983].

### When to Use It

The choice of Row Data Gateway often takes two steps: first whether to use a gateway at all and second whether to use Row Data Gateway or **Table Data Gateway (144)**.

I use Row Data Gateway most often when I’m using a **Transaction Script (110)**. In this case it nicely factors out the database access code and allows it to be reused easily by different Transaction Scripts (110).

I don’t use a Row Data Gateway when I’m using a **Domain Model (116)**. If the mapping is simple, **Active Record (160)** does the same job without an additional layer of code. If the mapping is complex, **Data Mapper (165)** works better, as it’s better at decoupling the data structure from the domain objects because the domain objects don’t need to know the layout of the database.

Interestingly, I’ve seen Row Data Gateway used very nicely with Data Mapper (165). Although this seems like extra work, it can be effective if the Row Data Gateways are automatically generated from metadata while the Data Mappers (165) are done by hand.

If you use Transaction Script (110) with Row Data Gateway, you may notice that you have business logic that’s repeated across multiple scripts; logic that would make sense in the Row Data Gateway. [cite\_start]Moving that logic will gradually turn your Row Data Gateway into an Active Record (160), which is often good as it reduces duplication in the business logic[cite: 983].

### Example: A Person Record (Java)

Here’s an example for Row Data Gateway. It’s a simple person table.

```sql
create table people (ID int primary key, lastname varchar, firstname varchar, number_of_dependents int)
```

`PersonGateway` is a gateway for the table. It starts with data fields and accessors. The gateway class itself can handle updates and inserts.

```java
class PersonGateway...
    public void update() {
        PreparedStatement updateStatement = null;
        try {
            updateStatement = DB.prepare(updateStatementString);
            updateStatement.setString(1, lastName);
            updateStatement.setString(2, firstName);
            updateStatement.setInt(3, numberOfDependents);
            updateStatement.setInt(4, getID().intValue());
            updateStatement.execute();
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {DB.cleanUp(updateStatement);
        }
    }
```

To pull people out of the database, we have a separate `PersonFinder`. This works with the gateway to create new gateway objects.

```java
class PersonFinder...
    public PersonGateway find(Long id) {
        PersonGateway result = (PersonGateway) Registry.getPerson(id);
        if (result != null) return result;
        PreparedStatement findStatement = null;
        ResultSet rs = null;
        try {
            findStatement = DB.prepare(findStatementString);
            findStatement.setLong(1, id.longValue());
            rs = findStatement.executeQuery();
            rs.next();
            result = PersonGateway.load(rs);
            return result;
        } catch (SQLException e) {
            throw new ApplicationException(e);
        } finally {DB.cleanUp(findStatement, rs);
        }
    }
```

-----

## Active Record

**An object that wraps a row in a database table or view, encapsulates the database access, and adds domain logic on that data.**

An object carries both data and behavior. Much of this data is persistent and needs to be stored in a database. Active Record uses the most obvious approach, putting data access logic in the domain object. This way all people know how to read and write their data to and from the database.

### How It Works

The essence of an Active Record is a **Domain Model (116)** in which the classes match very closely the record structure of an underlying database. Each Active Record is responsible for saving and loading to the database and also for any domain logic that acts on the data. This may be all the domain logic in the application, or you may find that some domain logic is held in **Transaction Scripts (110)** with common and data-oriented code in the Active Record.

The data structure of the Active Record should exactly match that of the database: one field in the class for each column in the table. Type the fields the way the SQL interface gives you the data—don’t do any conversion at this stage. You may consider **Foreign Key Mapping (236)**, but you may also leave the foreign keys as they are. You can use views or tables with Active Record, although updates through views are obviously harder. Views are particularly useful for reporting purposes.

The Active Record class typically has methods that do the following:

  * Construct an instance of the Active Record from a SQL result set row
  * Construct a new instance for later insertion into the table
  * Static finder methods to wrap commonly used SQL queries and return Active Record objects
  * Update the database and insert into it the data in the Active Record
  * Get and set the fields
  * Implement some pieces of business logic

Active Record is very similar to **Row Data Gateway (152)**. The principal difference is that a Row Data Gateway (152) contains only database access logic while an Active Record contains both data source and domain logic. [cite\_start]Like most boundaries in software, the line between the two isn’t terribly sharp, but it’s useful [cite: 990-991].

### When to Use It

Active Record is a good choice for domain logic that isn’t too complex, such as creates, reads, updates, and deletes. Derivations and validations based on a single record work well in this structure.

In an initial design for a **Domain Model (116)** the main choice is between Active Record and **Data Mapper (165)**. Active Record has the primary advantage of simplicity. It’s easy to build Active Records, and they are easy to understand. Their primary problem is that they work well only if the Active Record objects correspond directly to the database tables: an isomorphic schema. If your business logic is complex, you’ll soon want to use your object’s direct relationships, collections, inheritance, and so forth. These don’t map easily onto Active Record, and adding them piecemeal gets very messy. That’s what will lead you to use Data Mapper (165) instead.

Active Record is a good pattern to consider if you’re using **Transaction Script (110)** and are beginning to feel the pain of code duplication and the difficulty in updating scripts and tables that Transaction Script (110) often brings. [cite\_start]In this case you can gradually start creating Active Records and then slowly refactor behavior into them [cite: 991-992].

### Example: A Simple Person (Java)

This is a simple, even simplistic, example to show how the bones of Active Record work. We begin with a basic Person class.

```java
class Person...
    private String lastName;
    private String firstName;
    private int numberOfDependents;
```

To load an object, the person class acts as the finder and also performs the load. It uses static methods on the person class.

```java
class Person...
    public static Person find(Long id) {
        Person result = (Person) Registry.getPerson(id);
        if (result != null) return result;
        PreparedStatement findStatement = null;
        ResultSet rs = null;
        try {
            findStatement = DB.prepare(findStatementString);
            findStatement.setLong(1, id.longValue());
            rs = findStatement.executeQuery();
            rs.next();
            result = load(rs);
            return result;
        } catch (SQLException e) {
            throw new ApplicationException(e);
        } finally {
            DB.cleanUp(findStatement, rs);
        }
    }
```

Updating an object takes a simple instance method.

```java
class Person...
    public void update() {
        PreparedStatement updateStatement = null;
        try {
            updateStatement = DB.prepare(updateStatementString);
            updateStatement.setString(1, lastName);
            updateStatement.setString(2, firstName);
            updateStatement.setInt(3, numberOfDependents);
            updateStatement.setInt(4, getID().intValue());
            updateStatement.execute();
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            DB.cleanUp(updateStatement);
        }
    }
```

Any business logic, such as calculating the exemption, sits directly in the Person class.

```java
class Person...
    public Money getExemption() {
        Money baseExemption = Money.dollars(1500);
        Money dependentExemption = Money.dollars(750);
        return baseExemption.add(dependentExemption.multiply(this.getNumberOfDependents()));
    }
```

-----

## Data Mapper

**A layer of Mappers (473) that moves data between objects and a database while keeping them independent of each other and the mapper itself.**

[Image of Data Mapper pattern diagram]

Objects and relational databases have different mechanisms for structuring data. Many parts of an object, such as collections and inheritance, aren’t present in relational databases. When you build an object model with a lot of business logic it’s valuable to use these mechanisms to better organize the data and the behavior that goes with it. Doing so leads to variant schemas; that is, the object schema and the relational schema don’t match up.

You still need to transfer data between the two schemas, and this data transfer becomes a complexity in its own right. If the in-memory objects know about the relational database structure, changes in one tend to ripple to the other.

The **Data Mapper** is a layer of software that separates the in-memory objects from the database. Its responsibility is to transfer data between the two and also to isolate them from each other. With Data Mapper the in-memory objects needn’t know even that there’s a database present; they need no SQL interface code, and certainly no knowledge of the database schema. Since it’s a form of **Mapper (473)**, Data Mapper itself is even unknown to the domain layer.

### How It Works

The separation between domain and data source is the main function of a Data Mapper, but there are plenty of details that have to be addressed to make this happen.

A simple case would have a Person and Person Mapper class. To load a person from the database, a client would call a find method on the mapper. The mapper uses an **Identity Map (195)** to see if the person is already loaded; if not, it loads it. A client asks the mapper to save a domain object. The mapper pulls the data out of the domain object and shuttles it to the database.

A simple Data Mapper would just map a database table to an equivalent in-memory class on a field-to-field basis. Mappers need a variety of strategies to handle classes that turn into multiple fields, classes that have multiple tables, classes with inheritance, and the joys of connecting together objects once they’ve been sorted out.

When it comes to inserts and updates, the database mapping layer needs to understand what objects have changed, which new ones have been created, and which ones have been destroyed. It also has to fit the whole workload into a transactional framework. The **Unit of Work (184)** pattern is a good way to organize this.

**Handling Finders:** In order to work with an object, you have to load it from the database. Usually the presentation layer will initiate things by loading some initial objects. Then control moves into the domain layer, at which point the code will mainly move from object to object using associations between them. On occasion you may need the domain objects to invoke find methods on the Data Mapper. However, I’ve found that with a good **Lazy Load (200)** you can completely avoid this.

**Mapping Data to Domain Fields:** Mappers need access to the fields in the domain objects. Often this can be a problem because you need public methods to support the mappers you don’t want for domain logic. You can use reflection, which can often bypass the visibility rules of the language.

### When to Use It

The primary occasion for using Data Mapper is when you want the database schema and the object model to evolve independently. The most common case for this is with a **Domain Model (116)**. Data Mapper’s primary benefit is that when working on the domain model you can ignore the database, both in design and in the build and testing process.

The price, of course, is the extra layer that you don’t get with **Active Record (160)**, so the test for using these patterns is the complexity of the business logic. If you have fairly simple business logic, you probably won’t need a Domain Model (116) or a Data Mapper. More complicated logic leads you to Domain Model (116) and therefore to Data Mapper.

I wouldn’t choose Data Mapper without Domain Model (116).

Remember that you don’t have to build a full-featured database-mapping layer. It’s a complicated beast to build, and there are products available that do this for you. [cite\_start]For most cases I recommend buying a database-mapping layer rather than building one yourself [cite: 995-1001].

### Example: A Simple Database Mapper (Java)

Here’s an absurdly simple use of Data Mapper to give you a feel for the basic structure. Our example is a person with an isomorphic people table. We’ll use the simple case here, where the Person Mapper class also implements the finder and **Identity Map (195)**. However, I’ve added an abstract mapper **Layer Supertype (475)** to indicate where I can pull out some common behavior.

The find behavior starts in the Person Mapper, which wraps calls to an abstract find method to find by ID.

```java
class PersonMapper...
    protected String findStatement() {
        return "SELECT " + COLUMNS + " FROM people" + " WHERE id = ?";
    }
    public Person find(Long id) {
        return (Person) abstractFind(id);
    }
```

The find method calls the load method, which is split between the abstract and person mappers. The abstract mapper checks the ID, pulling it from the data and registering the new object in the **Identity Map (195)**.

```java
class AbstractMapper...
    protected DomainObject load(ResultSet rs) throws SQLException {
        Long id = new Long(rs.getLong(1));
        if (loadedMap.containsKey(id)) return (DomainObject) loadedMap.get(id);
        DomainObject result = doLoad(id, rs);
        loadedMap.put(id, result);
        return result;
    }
```

Notice that the Identity Map (195) is checked twice, once by abstractFind and once by load. I need to check the map in the finder because, if the object is already there, I can save myself a trip to the database. But I also need to check in the load because I may have queries that I can’t be sure of resolving in the Identity Map (195).

With the update the JDBC code is specific to the subtype.

```java
class PersonMapper...
    public void update(Person subject) {
        PreparedStatement updateStatement = null;
        try {
            updateStatement = DB.prepare(updateStatementString);
            updateStatement.setString(1, subject.getLastName());
            updateStatement.setString(2, subject.getFirstName());
            updateStatement.setInt(3, subject.getNumberOfDependents());
            updateStatement.setInt(4, subject.getID().intValue());
            updateStatement.execute();
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            DB.cleanUp(updateStatement);
        }
    }
```

Here is the content extracted from **Chapter 9: Domain Logic Patterns** of *Patterns of Enterprise Application Architecture*, formatted in Markdown.

# Chapter 9: Domain Logic Patterns

## Transaction Script

[cite\_start]**Organizes business logic by procedures where each procedure handles a single request from the presentation.** [cite: 940]

Most business applications can be thought of as a series of transactions. A transaction may view some information as organized in a particular way, another will make changes to it. Each interaction between a client system and a server system contains a certain amount of logic. In some cases this can be as simple as displaying information in the database. In others it may involve many steps of validations and calculations.

A **Transaction Script** organizes all this logic primarily as a single procedure, making calls directly to the database or through a thin database wrapper. Each transaction will have its own Transaction Script, although common subtasks can be broken into subprocedures.

### How It Works

With Transaction Script the domain logic is primarily organized by the transactions that you carry out with the system. [cite\_start]If your need is to book a hotel room, the logic to check room availability, calculate rates, and update the database is found inside the `Book Hotel Room` procedure[cite: 940].

For simple cases there isn’t much to say about how you organize this. Of course, as with any other program you should structure the code into modules in a way that makes sense. Unless the transaction is particularly complicated, that won’t be much of a challenge. One of the benefits of this approach is that you don’t need to worry about what other transactions are doing. Your task is to get the input, interrogate the database, munge, and save your results to the database.

Where you put the Transaction Script will depend on how you organize your layers. It may be in a server page, a CGI script, or a distributed session object. My preference is to separate Transaction Scripts as much as you can. At the very least put them in distinct subroutines; better still, put them in classes separate from those that handle presentation and data source. [cite\_start]In addition, don’t have any calls from the Transaction Scripts to any presentation logic; that will make it easier to modify the code and test the Transaction Scripts [cite: 940-941].

You can organize your Transaction Scripts into classes in two ways. The most common is to have several Transaction Scripts in a single class, where each class defines a subject area of related Transaction Scripts. This is straightforward and the best bet for most cases. The other way is to have each Transaction Script in its own class, using the Command pattern. In this case you define a supertype for your commands that specifies some execute method in which Transaction Script logic fits. [cite\_start]The advantage of this is that it allows you to manipulate instances of scripts as objects at runtime, although I’ve rarely seen a need to do this with the kinds of systems that use Transaction Scripts to organize domain logic[cite: 941].

### When to Use It

The glory of Transaction Script is its simplicity. Organizing logic this way is natural for applications with only a small amount of logic, and it involves very little overhead either in performance or in understanding.

As the business logic gets more complicated, however, it gets progressively harder to keep it in a well-designed state. One particular problem to watch for is its duplication between transactions. Since the whole point is to handle one transaction, any common code tends to be duplicated.

Careful factoring can alleviate many of these problems, but more complex business domains need to build a **Domain Model (116)**. A Domain Model will give you many more options in structuring the code, increasing readability and decreasing duplication.

It’s hard to quantify the cutover level, especially when you’re more familiar with one pattern than the other. You can refactor a Transaction Script design to a Domain Model design, but it’s a harder change than it otherwise needs to be. Therefore, an early shot is often the best way to move forward.

However much of an object bigot you become, don’t rule out Transaction Script. [cite\_start]There are a lot of simple problems out there, and a simple solution will get you up and running much faster [cite: 941-942].

### The Revenue Recognition Problem

Revenue recognition is a common problem in business systems. It’s all about when you can actually count the money you receive on your books. If I sell you a cup of coffee, it’s a simple matter: I give you the coffee, I take your money, and I count the money to the books that nanosecond. For many things it gets complicated, however. Say you pay me a retainer to be available that year. Even if you pay me some ridiculous fee today, I may not be able to put it on my books right away because the service is to be performed over the course of a year. One approach might be to count only one-twelfth of that fee for each month in the year, since you might pull out of the contract after a month when you realize that writing has atrophied my programming skills.

The rules for revenue recognition are many, various, and volatile. Some are set by regulation, some by professional standards, and some by company policy. Revenue tracking ends up being quite a complex problem.

I don’t fancy delving into the complexity right now, so instead we’ll imagine a company that sells three kinds of products: word processors, databases, and spreadsheets. According to the rules, when you sign a contract for a word processor you can book all the revenue right away. If it’s a spreadsheet, you can book one-third today, one-third in sixty days, and one-third in ninety days. [cite\_start]If it’s a database, you can book one-third today, one-third in thirty days, and one-third in sixty days[cite: 942].

### Example: Revenue Recognition (Java)

This example uses two transaction scripts: one to calculate the revenue recognitions for a contract and one to tell how much revenue on a contract has been recognized by a certain date. [cite\_start]The database structure has three tables: one for the products, one for the contracts, and one for the revenue recognitions[cite: 943].

```sql
CREATE TABLE products (ID int primary key, name varchar, type varchar)
CREATE TABLE contracts (ID int primary key, product int, revenue decimal, dateSigned date)
CREATE TABLE revenueRecognitions (contract int, amount decimal, recognizedOn date, PRIMARY KEY (contract, recognizedOn))
```

The first script calculates the amount of recognition due by a particular day. I can do this in two stages: In the first I select the appropriate rows in the revenue recognitions table; in the second I sum up the amounts.

Many Transaction Script designs have scripts that operate directly on the database, putting SQL code in the procedure. Here I’m using a simple **Table Data Gateway (144)** to wrap the SQL queries. Since this example is so simple, I’m using a single gateway rather than one for each table. [cite\_start]I can define an appropriate find method on the gateway[cite: 943].

```java
class Gateway...
    public ResultSet findRecognitionsFor(long contractID, MfDate asof) throws SQLException{
        PreparedStatement stmt = db.prepareStatement(findRecognitionsStatement);
        stmt.setLong(1, contractID);
        stmt.setDate(2, asof.toSqlDate());
        ResultSet result = stmt.executeQuery();
        return result;
    }
    
    private static final String findRecognitionsStatement =
        "SELECT amount " +
        " FROM revenueRecognitions " +
        " WHERE contract = ? AND recognizedOn <= ?";
    private Connection db;
```

[cite\_start]I then use the script to sum up based on the result set passed back from the gateway[cite: 944].

```java
class RecognitionService...
    public Money recognizedRevenue(long contractNumber, MfDate asOf) {
        Money result = Money.dollars(0);
        try {
            ResultSet rs = db.findRecognitionsFor(contractNumber, asOf);
            while (rs.next()) {
                result = result.add(Money.dollars(rs.getBigDecimal("amount")));
            }
            return result;
        } catch (SQLException e) {throw new ApplicationException (e);
        }
    }
```

For calculating the revenue recognitions on an existing contract, I use a similar split. [cite\_start]The script on the service carries out the business logic [cite: 944-945].

```java
class RecognitionService...
    public void calculateRevenueRecognitions(long contractNumber) {
        try {
            ResultSet contracts = db.findContract(contractNumber);
            contracts.next();
            Money totalRevenue = Money.dollars(contracts.getBigDecimal("revenue"));
            MfDate recognitionDate = new MfDate(contracts.getDate("dateSigned"));
            String type = contracts.getString("type");
            
            if (type.equals("S")){
                Money[] allocation = totalRevenue.allocate(3);
                db.insertRecognition(contractNumber, allocation[0], recognitionDate);
                db.insertRecognition(contractNumber, allocation[1], recognitionDate.addDays(60));
                db.insertRecognition(contractNumber, allocation[2], recognitionDate.addDays(90));
            } else if (type.equals("W")){
                db.insertRecognition(contractNumber, totalRevenue, recognitionDate);
            } else if (type.equals("D")) {
                Money[] allocation = totalRevenue.allocate(3);
                db.insertRecognition(contractNumber, allocation[0], recognitionDate);
                db.insertRecognition(contractNumber, allocation[1], recognitionDate.addDays(30));
                db.insertRecognition(contractNumber, allocation[2], recognitionDate.addDays(60));
            }
        } catch (SQLException e) {throw new ApplicationException (e);
        }
    }
```

Notice that I’m using **Money (488)** to carry out the allocation. [cite\_start]When splitting an amount three ways it’s very easy to lose a penny[cite: 945].

-----

## Domain Model

[cite\_start]**An object model of the domain that incorporates both behavior and data.** [cite: 946]

At its worst business logic can be very complex. Rules and logic describe many different cases and slants of behavior, and it’s this complexity that objects were designed to work with. A **Domain Model** creates a web of interconnected objects, where each object represents some meaningful individual, whether as large as a corporation or as small as a single line on an order form.

### How It Works

Putting a Domain Model in an application involves inserting a whole layer of objects that model the business area you’re working in. You’ll find objects that mimic the data in the business and objects that capture the rules the business uses. [cite\_start]Mostly the data and process are combined to cluster the processes close to the data they work with[cite: 946].

An OO domain model will often look similar to a database model, yet it will still have a lot of differences. A Domain Model mingles data and process, has multivalued attributes and a complex web of associations, and uses inheritance.

As a result I see two styles of Domain Model in the field. A simple Domain Model looks very much like the database design with mostly one domain object for each database table. A rich Domain Model can look different from the database design, with inheritance, strategies, and other Gang of Four patterns, and complex webs of small interconnected objects. [cite\_start]A rich Domain Model is better for more complex logic, but is harder to map to the database[cite: 947].

Since the behavior of the business is subject to a lot of change, it’s important to be able to modify, build, and test this layer easily. As a result you’ll want the minimum of coupling from the Domain Model to other layers in the system.

With a Domain Model there are a number of different scopes you might use. The simplest case is a single-user application where the whole object graph is read from a file and put into memory. Without an OO database you have to do this yourself. Usually a session will involve pulling in an object graph of all the objects involved in it. This will certainly not be all objects and usually not all the classes.

**Java Implementation**
There’s always a lot of heat generated when people talk about developing a Domain Model in J2EE. [cite\_start]Many of the teaching materials and introductory J2EE books suggest that you use entity beans to develop a domain model, but there are some serious problems with this approach, at least with the current (2.0) specification[cite: 948].

  * Entity beans can't be re-entrant.
  * Entity beans may be remotable. If you have remote objects with fine-grained interfaces you get terrible performance.
  * To run with entity beans you need a container and a database connected. This will increase build times.

The alternative is to use normal Java objects (POJOs). My view on the whole is that using entity beans as a Domain Model works if you have pretty modest domain logic. [cite\_start]If you have a richer domain logic, you’re better off with a POJO domain model and **Data Mapper (165)** [cite: 948-949].

### When to Use It

It all comes down to the complexity of the behavior in your system. If you have complicated and everchanging business rules involving validation, calculations, and derivations, chances are that you’ll want an object model to handle them. [cite\_start]On the other hand, if you have simple not-null checks and a couple of sums to calculate, a **Transaction Script (110)** is a better bet[cite: 949].

### Example: Revenue Recognition (Java)

I’m using the same example (page 112) that I used for Transaction Script (110), a little matter of revenue recognition. An immediate thing to notice is that every class, in this small example (Figure 9.3) contains both behavior and data. [cite\_start]Even the humble Revenue Recognition class contains a simple method to find out if that object’s value is recognizable on a certain date[cite: 950].

```java
class RevenueRecognition...
    private Money amount;
    private MfDate date;
    public RevenueRecognition(Money amount, MfDate date) {
        this.amount = amount;
        this.date = date;
    }
    public Money getAmount() {
        return amount;
    }
    boolean isRecognizableBy(MfDate asOf) {
        return asOf.after(date) || asOf.equals(date);
    }
```

[cite\_start]Calculating how much revenue is recognized on a particular date involves both the contract and revenue recognition classes[cite: 950].

```java
class Contract...
    private List revenueRecognitions = new ArrayList();
    public Money recognizedRevenue(MfDate asOf) {
        Money result = Money.dollars(0);
        Iterator it = revenueRecognitions.iterator();
        while (it.hasNext()) {
            RevenueRecognition r = (RevenueRecognition) it.next();
            if (r.isRecognizableBy(asOf))
                result = result.add(r.getAmount());
        }
        return result;
    }
```

Looking at calculating and creating these revenue recognition objects further demonstrates the notion of lots of little objects. In this case the calculation and creation begin with the customer and are handed off via the product to a strategy hierarchy. [cite\_start]The strategy pattern allows you to combine a group of operations in a small class hierarchy[cite: 952].

```java
class Contract...
    private Product product;
    private Money revenue;
    private MfDate whenSigned;
    private Long id;

class Product...
    private String name;
    private RecognitionStrategy recognitionStrategy;
    public static Product newWordProcessor(String name) {
        return new Product(name, new CompleteRecognitionStrategy());
    }
    public static Product newSpreadsheet(String name) {
        return new Product(name, new ThreeWayRecognitionStrategy(60, 90));
    }
    public static Product newDatabase(String name) {
        return new Product(name, new ThreeWayRecognitionStrategy(30, 60));
    }

class RecognitionStrategy...
    abstract void calculateRevenueRecognitions(Contract contract);

class CompleteRecognitionStrategy...
    void calculateRevenueRecognitions(Contract contract) {
        contract.addRevenueRecognition(new RevenueRecognition(contract.getRevenue(), contract.getWhenSigned()));
    }

class ThreeWayRecognitionStrategy...
    void calculateRevenueRecognitions(Contract contract) {
        Money[] allocation = contract.getRevenue().allocate(3);
        contract.addRevenueRecognition(new RevenueRecognition(allocation[0], contract.getWhenSigned()));
        contract.addRevenueRecognition(new RevenueRecognition(allocation[1], contract.getWhenSigned().addDays(firstRecognitionOffset)));
        contract.addRevenueRecognition(new RevenueRecognition(allocation[2], contract.getWhenSigned().addDays(secondRecognitionOffset)));
    }
```

[cite\_start]Once everything is set up, calculating the recognitions requires no knowledge of the strategy subclasses[cite: 953].

```java
class Contract...
    public void calculateRecognitions() {
        product.calculateRevenueRecognitions(this);
    }
class Product...
    void calculateRevenueRecognitions(Contract contract) {
        recognitionStrategy.calculateRevenueRecognitions(contract);
    }
```

-----

## Table Module

[cite\_start]**A single instance that handles the business logic for all rows in a database table or view.** [cite: 955]

[Image of Table Module Pattern Structure]

One of the key messages of object orientation is bundling the data with the behavior that uses it. The traditional object-oriented approach is based on objects with identity, along the lines of **Domain Model (116)**. Thus, if we have an Employee class, any instance of it corresponds to a particular employee.

A **Table Module** organizes domain logic with one class per table in the database, and a single instance of a class contains the various procedures that will act on the data. The primary distinction with Domain Model (116) is that, if you have many orders, a Domain Model will have one order object per order while a Table Module will have one object to handle all orders.

### How It Works

The strength of Table Module is that it allows you to package the data and behavior together and at the same time play to the strengths of a relational database. On the surface Table Module looks much like a regular object. The key difference is that it has no notion of an identity for the objects it’s working with. Thus, if you want to obtain the address of an employee, you use a method like `anEmployeeModule.getAddress(long employeeID)`. Every time you want to do something to a particular employee you have to pass in some kind of identity reference. [cite\_start]Often this will be the primary key used in the database[cite: 956].

Usually you use Table Module with a backing data structure that’s table oriented. The tabular data is normally the result of a SQL call and is held in a **Record Set (508)** that mimics a SQL table.

The Table Module may be an instance or it may be a collection of static methods. The advantage of an instance is that it allows you to initialize the Table Module with an existing record set, perhaps the result of a query. [cite\_start]You can then use the instance to manipulate the rows in the record set[cite: 957].

### When to Use It

Table Module is very much based on table-oriented data, so obviously using it makes sense when you’re accessing tabular data using **Record Set (508)**. It also puts that data structure very much in the center of the code, so you also want the way you access the data structure to be fairly straightforward.

However, Table Module doesn’t give you the full power of objects in organizing complex logic. You can’t have direct instance-to-instance relationships, and polymorphism doesn’t work well. [cite\_start]So, for handling complicated domain logic, a **Domain Model (116)** is a better choice[cite: 958].

The most well-known situation in which I’ve come across this pattern is in Microsoft COM designs. In COM (and .NET) the Record Set is the primary repository of data in an application.

### Example: Revenue Recognition with a Table Module (C\#)

Time to revisit the revenue recognition example (page 112) I used in the other domain modeling patterns, this time with a Table Module. In this example we have different rules for word processors, spreadsheets, and databases.

[Image of Database Schema for Revenue Recognition]

The classes that manipulate this data are in pretty much the same form; there’s one Table Module class for each table. In the .NET architecture a data set object provides an in-memory representation of a database structure. Each Table Module class has a data member of a data table, which is the .NET system class corresponding to a table within the data set. [cite\_start]This ability to read a table is common to all Table Modules and so can appear in a **Layer Supertype (475)**[cite: 959].

```csharp
class TableModule...
    protected DataTable table;
    protected TableModule(DataSet ds, String tableName) {
        table = ds.Tables[tableName];
    }
```

The first piece of functionality calculates the revenue recognition for a contract, updating the revenue recognition table accordingly. The amount recognized depends on the kind of product we have. [cite\_start]Since this behavior mainly uses data from the contract table, I decided to add the method to the contract class[cite: 960].

```csharp
class Contract...
    public void CalculateRecognitions (long contractID) {
        DataRow contractRow = this[contractID];
        Decimal amount = (Decimal)contractRow["amount"];
        RevenueRecognition rr = new RevenueRecognition (table.DataSet);
        Product prod = new Product(table.DataSet);
        long prodID = GetProductId(contractID);
        
        if (prod.GetProductType(prodID) == ProductType.WP) {
            rr.Insert(contractID, amount, (DateTime) GetWhenSigned(contractID));
        } else if (prod.GetProductType(prodID) == ProductType.SS) {
            Decimal[] allocation = allocate(amount,3);
            rr.Insert(contractID, allocation[0], (DateTime) GetWhenSigned(contractID));
            rr.Insert(contractID, allocation[1], (DateTime) GetWhenSigned(contractID).AddDays(60));
            rr.Insert(contractID, allocation[2], (DateTime) GetWhenSigned(contractID).AddDays(90));
        } else if (prod.GetProductType(prodID) == ProductType.DB) {
            Decimal[] allocation = allocate(amount,3);
            rr.Insert(contractID, allocation[0], (DateTime) GetWhenSigned(contractID));
            rr.Insert(contractID, allocation[1], (DateTime) GetWhenSigned(contractID).AddDays(30));
            rr.Insert(contractID, allocation[2], (DateTime) GetWhenSigned(contractID).AddDays(60));
        } else throw new Exception("invalid product id");
    }
```

To carry this out, we need some behavior that’s defined on the other classes. The product needs to be able to tell us which type it is. [cite\_start]We can do this with an enum for the product type and a lookup method[cite: 961].

```csharp
class Product...
    public ProductType GetProductType (long id) {
        String typeCode = (String) this[id]["type"];
        return (ProductType) Enum.Parse(typeof(ProductType), typeCode);
    }
```

The second piece of functionality is to sum up all the revenue recognized on a contract by a given date. [cite\_start]Since this uses the revenue recognition table it makes sense to define the method there[cite: 962].

```csharp
class RevenueRecognition...
    public Decimal RecognizedRevenue (long contractID, DateTime asOf) {
        String filter = String.Format("ContractID = {0} AND date <= #{1:d}#", contractID,asOf); 
        DataRow[] rows = table.Select(filter);
        Decimal result = 0m;
        foreach (DataRow row in rows) {
            result += (Decimal)row["amount"];
        }
        return result;
    }
```

-----

## Service Layer

[cite\_start]**Defines an application’s boundary with a layer of services that establishes a set of available operations and coordinates the application’s response in each operation.** [cite: 963]

Enterprise applications typically require different kinds of interfaces to the data they store and the logic they implement: data loaders, user interfaces, integration gateways, and others. Despite their different purposes, these interfaces often need common interactions with the application to access and manipulate its data and invoke its business logic. Encoding the logic of the interactions separately in each interface causes a lot of duplication.

A **Service Layer** defines an application’s boundary and its set of available operations from the perspective of interfacing client layers. It encapsulates the application’s business logic, controlling transactions and coordinating responses in the implementation of its operations.

### How It Works

A Service Layer can be implemented in a couple of different ways. [cite\_start]The differences appear in the allocation of responsibility behind the Service Layer interface[cite: 964].

  * **Kinds of "Business Logic":** Many designers like to divide "business logic" into two kinds: "domain logic," having to do purely with the problem domain (such as strategies for calculating revenue recognition), and "application logic," having to do with application responsibilities (such as notifying contract administrators). Service Layer factors each kind of business logic into a separate layer.
  * **Implementation Variations:**
      * **Domain Facade approach:** A Service Layer is implemented as a set of thin facades over a Domain Model. The classes implementing the facades don’t implement any business logic.
      * **Operation Script approach:** A Service Layer is implemented as a set of thicker classes that directly implement application logic but delegate to encapsulated domain object classes for domain logic.
  * **To Remote or Not:** The interface of a Service Layer class is coarse grained almost by definition. Therefore, Service Layer classes are well suited to remote invocation. However, remote invocation comes at the cost of dealing with object distribution. My advice is to start with a locally invocable Service Layer whose method signatures deal in domain objects. [cite\_start]Add remotability when you need it by putting **Remote Facades (388)** on your Service Layer[cite: 965].

**Java Implementation**
My preferred way of applying a Service Layer in J2EE is with EJB 2.0 stateless session beans, using local interfaces, and the operation script approach, delegating to POJO domain object classes. [cite\_start]It’s just so darned convenient to implement a Service Layer using stateless session bean, because of the distributed container-managed transactions provided by EJB[cite: 966].

### When to Use It

The benefit of Service Layer is that it defines a common set of application operations available to many kinds of clients and it coordinates an application’s response in each operation. The response may involve application logic that needs to be transacted atomically across multiple transactional resources. Thus, in an application with more than one kind of client of its business logic, and complex responses in its use cases involving multiple transactional resources, it makes a lot of sense to include a Service Layer with container-managed transactions, even in an undistributed architecture.

[cite\_start]You probably don’t need a Service Layer if your application’s business logic will only have one kind of client—say, a user interface—and its use case responses don’t involve multiple transactional resources[cite: 967].

### Example: Revenue Recognition (Java)

This example continues the revenue recognition example of the Transaction Script and Domain Model patterns, demonstrating how Service Layer is used to script application logic and delegate for domain logic in a Service Layer operation. It uses the operation script approach to implement a Service Layer, first with POJOs and then with EJBs.

We start by changing the `RecognitionService` class from the Transaction Script example to extend a **Layer Supertype (475)** and to use a couple of **Gateways (466)** in carrying out application logic. [cite\_start]`RecognitionService` becomes a POJO implementation of a Service Layer application service[cite: 968].

```java
public class RecognitionService extends ApplicationService {
    public void calculateRevenueRecognitions(long contractNumber) {
        Contract contract = Contract.readForUpdate(contractNumber);
        contract.calculateRecognitions();
        getEmailGateway().sendEmailMessage(
            contract.getAdministratorEmailAddress(),
            "RE: Contract #" + contractNumber,
            contract + " has had revenue recognitions calculated.");
        getIntegrationGateway().publishRevenueRecognitionCalculation(contract);
    }
    public Money recognizedRevenue(long contractNumber, Date asOf) {
        return Contract.read(contractNumber).recognizedRevenue(asOf);
    }
}
```

Persistence details are again left out of the example. Suffice it to say that the Contract class implements static methods to read contracts from the Data Source layer. Transaction control details are also left out of the example. [cite\_start]The `calculateRevenueRecognitions()` method is inherently transactional because, during its execution, persistent contract objects are modified; messages are enqueued in message-oriented middleware; and e-mail messages are sent[cite: 970].

In the J2EE platform we can let the EJB container manage distributed transactions by implementing application services as stateless session beans. Figure 9.8 shows the class diagram of a `RecognitionService` implementation that uses EJB 2.0 local interfaces and the "business interface" idiom.

The important point about the example is that the Service Layer uses both operation scripting and domain object classes in coordinating the transactional response of the operation. [cite\_start]The `calculateRevenueRecognitions` method scripts the application logic of the response required by the application’s use cases, but it delegates to the domain object classes for domain logic[cite: 970].


# Chapter 11

Here is the content extracted from **Chapter 11: Object-Relational Behavioral Patterns** of *Patterns of Enterprise Application Architecture*, formatted in Markdown.

# [cite\_start]Chapter 11: Object-Relational Behavioral Patterns [cite: 1013]

## [cite\_start]Unit of Work [cite: 1014]

**Maintains a list of objects affected by a business transaction and coordinates the writing out of changes and the resolution of concurrency problems.**

When you’re pulling data in and out of a database, it’s important to keep track of what you’ve changed; otherwise, that data won’t be written back into the database. Similarly you have to insert new objects you create and remove any objects you delete.

You can change the database with each change to your object model, but this can lead to lots of very small database calls, which ends up being very slow. Furthermore it requires you to have a transaction open for the whole interaction, which is impractical if you have a business transaction that spans multiple requests. The situation is even worse if you need to keep track of the objects you’ve read so you can avoid inconsistent reads.

A **Unit of Work** keeps track of everything you do during a business transaction that can affect the database. When you’re done, it figures out everything that needs to be done to alter the database as a result of your work.

### [cite\_start]How It Works [cite: 1014]

The obvious things that cause you to deal with the database are changes: new object created and existing ones updated or deleted. Unit of Work is an object that keeps track of these things. As soon as you start doing something that may affect a database, you create a Unit of Work to keep track of the changes. Every time you create, change, or delete an object you tell the Unit of Work. You can also let it know about objects you’ve read so that it can check for inconsistent reads by verifying that none of the objects changed on the database during the business transaction.

The key thing about Unit of Work is that, when it comes time to commit, the Unit of Work decides what to do. It opens a transaction, does any concurrency checking (using **Optimistic Offline Lock (416)** or **Pessimistic Offline Lock (426)**), and writes changes out to the database. Application programmers never explicitly call methods for database updates. [cite\_start]This way they don’t have to keep track of what’s changed or worry about how referential integrity affects the order in which they need to do things. [cite: 1015]

Of course for this to work the Unit of Work needs to know what objects it should keep track of. You can do this either by the caller doing it or by getting the object to tell the Unit of Work.

#### Caller Registration vs. Object Registration

  * **Caller Registration:** The user of an object has to remember to register the object with the Unit of Work for changes. Any objects that aren’t registered won’t be written out on commit. Although this allows forgetfulness to cause trouble, it does give flexibility in allowing people to make in-memory changes that they don’t want written out. Still, I would argue that it’s going to cause far more confusion than would be worthwhile. [cite\_start]It’s better to make an explicit copy for that purpose. [cite: 1015, 1016]
  * **Object Registration:** The onus is removed from the caller. The usual trick here is to place registration methods in object methods. Loading an object from the database registers the object as clean; the setting methods register the object as dirty. For this scheme to work the Unit of Work needs either to be passed to the object or to be in a well-known place. [cite\_start]Passing the Unit of Work around is tedious but usually no problem to have it present in some kind of session object. [cite: 1016, 1017]

Even object registration leaves something to remember; that is, the developer of the object has to remember to add a registration call in the right places. The consistency becomes habitual, but is still an awkward bug when missed. [cite\_start]This is a natural place for code generation or aspect-oriented programming. [cite: 1017]

#### Unit of Work Controller

Another technique is **unit of work controller**. Here the Unit of Work handles all reads from the database and registers clean objects whenever they’re read. Rather than marking objects as dirty the Unit of Work takes a copy at read time and then compares the object at commit time. [cite\_start]Although this adds overhead to the commit process, it allows a selective update of only those fields that were actually changed; it also avoids registration calls in the domain objects. [cite: 1017]

#### Other Considerations

  * **Object Creation:** Often a special time to consider caller registration. [cite\_start]It’s not uncommon for people to create objects that are only supposed to be transient (e.g., for testing). [cite: 1017]
  * **Update Order:** A Unit of Work can be helpful in update order when a database uses referential integrity. Most of the time you can avoid this issue by ensuring that the database only checks referential integrity when the transaction commits. [cite\_start]If the database checks on each write, the Unit of Work must figure out which order to write to the database. [cite: 1017, 1018]
  * **Deadlocks:** You can use a similar technique to minimize deadlocks. If every transaction uses the same sequence of tables to edit, you greatly reduce the risk of deadlocks. [cite\_start]The Unit of Work is an ideal place to hold a fixed sequence of table writes. [cite: 1018]
  * **Batch Updates:** Unit of Work makes an obvious point of handling batch updates. [cite\_start]The idea behind a batch update is to send multiple SQL commands as a single unit so that they can be processed in a single remote call. [cite: 1019]

### [cite\_start]When to Use It [cite: 1019]

The fundamental problem that Unit of Work deals with is keeping track of the various objects you’ve manipulated so that you know which ones you need to consider to synchronize your in-memory data with the database. If you’re able to do all your work within a system transaction, the only objects you need to worry about are those you alter. Although Unit of Work is generally the best way of doing this, there are alternatives:

1.  **Explicit Save:** Explicitly save any object whenever you alter it. The problem here is that you may get many more database calls than you want.
2.  **Dirty Flags:** Give each object a dirty flag that you set when the object changes. Then you need to find all the dirty objects at the end of your transaction and write them out. The value of this technique hinges on how easy it is to find the dirty objects.

The great strength of Unit of Work is that it keeps all this information in one place. Once you have it working for you, you don’t have to remember to do much in order to keep track of your changes. [cite\_start]Also, Unit of Work is a firm platform for more complicated situations, such as handling business transactions that span several system transactions using **Optimistic Offline Lock (416)** and **Pessimistic Offline Lock (426)**. [cite: 1020]

### [cite\_start]Example: Unit of Work with Object Registration (Java) [cite: 1020]

*by David Rice*

Here’s a Unit of Work that can track all changes for a given business transaction and then commit them to the database when instructed to do so. Our domain layer has a **Layer Supertype (475)**, `DomainObject`, with which the Unit of Work will interact. To store the change set we use three lists: new, dirty, and removed domain objects.

```java
class UnitOfWork...
    private List newObjects = new ArrayList();
    private List dirtyObjects = new ArrayList();
    private List removedObjects = new ArrayList();

    public void registerNew(DomainObject obj) {
        Assert.notNull("id not null", obj.getId());
        Assert.isTrue("object not dirty", !dirtyObjects.contains(obj));
        Assert.isTrue("object not removed", !removedObjects.contains(obj));
        Assert.isTrue("object not already registered new", !newObjects.contains(obj));
        newObjects.add(obj);
    }

    public void registerDirty(DomainObject obj) {
        Assert.notNull("id not null", obj.getId());
        Assert.isTrue("object not removed", !removedObjects.contains(obj));
        if (!dirtyObjects.contains(obj) && !newObjects.contains(obj)) {
            dirtyObjects.add(obj);
        }
    }

    public void registerRemoved(DomainObject obj) {
        Assert.notNull("id not null", obj.getId());
        if (newObjects.remove(obj)) return;
        dirtyObjects.remove(obj);
        if (!removedObjects.contains(obj)) {
            removedObjects.add(obj);
        }
    }

    public void registerClean(DomainObject obj) {
        Assert.notNull("id not null", obj.getId());
    }
```

[cite\_start][cite: 1020, 1021]

Notice that `registerClean()` doesn’t do anything here. A common practice is to place an **Identity Map (195)** within a Unit of Work. Without the Identity Map (195) you have the option of not including `registerClean()` in your Unit of Work.

`commit()` will locate the **Data Mapper (165)** for each object and invoke the appropriate mapping method.

```java
class UnitOfWork...
    public void commit() {
        insertNew();
        updateDirty();
        deleteRemoved();
    }

    private void insertNew() {
        for (Iterator objects = newObjects.iterator(); objects.hasNext();) {
            DomainObject obj = (DomainObject) objects.next();
            MapperRegistry.getMapper(obj.getClass()).insert(obj);
        }
    }
```

[cite\_start][cite: 1021]

Next we need to facilitate object registration. First each domain object needs to find the Unit of Work serving the current business transaction. As each business transaction executes within a single thread we can associate the Unit of Work with the currently executing thread using the `java.lang.ThreadLocal` class.

```java
class UnitOfWork...
    private static ThreadLocal current = new ThreadLocal();
    
    public static void newCurrent() {
        setCurrent(new UnitOfWork());
    }
    
    public static void setCurrent(UnitOfWork uow) {
        current.set(uow);
    }
    
    public static UnitOfWork getCurrent() {
        return (UnitOfWork) current.get();
    }
```

[cite\_start][cite: 1022]

Now we can give our abstract domain object the marking methods to register itself with the current Unit of Work.

```java
class DomainObject...
    protected void markNew() {
        UnitOfWork.getCurrent().registerNew(this);
    }
    protected void markClean() {
        UnitOfWork.getCurrent().registerClean(this);
    }
    protected void markDirty() {
        UnitOfWork.getCurrent().registerDirty(this);
    }
    protected void markRemoved() {
        UnitOfWork.getCurrent().registerRemoved(this);
    }
```

[cite\_start][cite: 1022]

Concrete domain objects need to remember to mark themselves new and dirty where appropriate.

```java
class Album...
    public static Album create(String name) {
        Album obj = new Album(IdGenerator.nextId(), name);
        obj.markNew();
        return obj;
    }
    
    public void setTitle(String title) {
        this.title = title;
        markDirty();
    }
```

[cite\_start][cite: 1023]

-----

## [cite\_start]Identity Map [cite: 1025]

**Ensures that each object gets loaded only once by keeping every loaded object in a map. Looks up objects using the map when referring to them.**

[Image of Identity Map Pattern Structure]

An old proverb says that a man with two watches never knows what time it is. If two watches are confusing, you can get in an even bigger mess with loading objects from a database. If you aren’t careful you can load the data from the same database record into two different objects. Then, when you update them both you’ll have an interesting time writing the changes out to the database correctly.

An **Identity Map** keeps a record of all objects that have been read from the database in a single business transaction. Whenever you want an object, you check the Identity Map first to see if you already have it.

### [cite\_start]How It Works [cite: 1025]

The basic idea behind the Identity Map is to have a series of maps containing objects that have been pulled from the database. In a simple case, with an isomorphic schema, you’ll have one map per database table. When you load an object from the database, you first check the map. If there’s an object in it that corresponds to the one you’re loading, you return it. [cite\_start]If not, you go to the database, putting the objects into the map for future reference as you load them. [cite: 1026]

  * [cite\_start]**Choice of Keys:** The obvious choice is the primary key of the corresponding database table. [cite: 1026]
  * **Explicit or Generic:** You have to choose whether to make the Identity Map explicit (distinct methods for each kind of object, e.g., `findPerson(1)`) or generic (single method, e.g., `find("Person", 1)`). I prefer an explicit Identity Map. [cite\_start]It gives compile-time checking and is easier to see what maps are available. [cite: 1026]
  * **How Many:** The decision varies between one map per class and one map for the whole session. [cite\_start]If you have multiple maps, the obvious route is one map per class or per table. [cite: 1026]
  * **Inheritance:** If you have cars as a subtype of vehicle, do you have one map or separate maps? [cite\_start]I prefer to use a single map for each inheritance tree, but that means that you should also make your keys unique across inheritance trees. [cite: 1027]
  * **Where to Put Them:** Identity Maps need to be somewhere where they’re easy to find. They’re also tied to the process context you’re working in. You need to ensure that each session gets its own instance that’s isolated from any other session’s instance. If you’re using **Unit of Work (184)** that’s by far the best place. [cite\_start]If not, a **Registry (480)** tied to the session is the best bet. [cite: 1027]

### [cite\_start]When to Use It [cite: 1028]

In general you use an Identity Map to manage any object brought from a database and modified. The key reason is that you don’t want a situation where two in-memory objects correspond to a single database record—you might modify the two records inconsistently. Another value in Identity Map is that it acts as a cache for database reads.

You may not need an Identity Map for immutable objects (like **Value Objects (486)**). Since Value Objects are immutable, you don’t have to worry about modification anomalies. [cite\_start]You don’t need an Identity Map for a **Dependent Mapping (262)**. [cite: 1028]

### [cite\_start]Example: Methods for an Identity Map (Java) [cite: 1028]

For each Identity Map we have a map field and accessors.

```java
private Map people = new HashMap();

public static void addPerson(Person arg) {
    soleInstance.people.put(arg.getID(), arg);
}

public static Person getPerson(Long key) {
    return (Person) soleInstance.people.get(key);
}

public static Person getPerson(long key) {
    return getPerson(new Long(key));
}
```

[cite\_start][cite: 1028]

-----

## [cite\_start]Lazy Load [cite: 1030]

**An object that doesn’t contain all of the data you need but knows how to get it.**

For loading data from a database into memory it’s handy to design things so that as you load an object of interest you also load the objects that are related to it. However, if you take this to its logical conclusion, you reach the point where loading one object can have the effect of loading a huge number of related objects.

A **Lazy Load** interrupts this loading process for the moment, leaving a marker in the object structure so that if the data is needed it can be loaded only when it is used.

### [cite\_start]How It Works [cite: 1030]

There are four main ways you can implement Lazy Load: lazy initialization, virtual proxy, value holder, and ghost.

1.  **Lazy Initialization:** Every access to the field checks first to see if it’s null. If so, it calculates the value of the field before returning the field. [cite\_start]To make this work you have to ensure that the field is self-encapsulated. [cite: 1031]
2.  **Virtual Proxy:** An object that looks like the object that should be in the field, but doesn’t actually contain anything. Only when one of its methods is called does it load the correct object from the database. The good thing is it looks exactly like the object. [cite\_start]The bad thing is that it isn’t that object, so you can easily run into identity problems. [cite: 1031]
3.  **Value Holder:** An object that wraps some other object. [cite\_start]To get the underlying object you ask the value holder for its value, but only on the first access does it pull the data from the database. [cite: 1031]
4.  **Ghost:** The real object in a partial state. When you load the object from the database it contains just its ID. Whenever you try to access a field it loads its full state. [cite\_start]Think of a ghost as an object where every field is lazy-initialized in one fell swoop. [cite: 1032]

**Ripple Loading:** A danger with Lazy Load is that it can easily cause more database accesses than you need. A good example is if you fill a collection with Lazy Loads and then look at them one at a time. This causes you to go to the database once for each object instead of reading them all in at once. [cite\_start]One way to avoid it is to never have a collection of Lazy Loads but rather make the collection itself a Lazy Load and, when you load it, load all the contents. [cite: 1032]

### [cite\_start]When to Use It [cite: 1033]

Deciding when to use Lazy Load is all about deciding how much you want to pull back from the database as you load an object, and how many database calls that will require. It’s usually pointless to use Lazy Load on a field that’s stored in the same row as the rest of the object. The best time to use Lazy Load is when it involves an extra call and the data you’re calling isn’t used when the main object is used.

### [cite\_start]Example: Lazy Initialization (Java) [cite: 1033]

The essence of lazy initialization is code like this:

```java
class Supplier...
    public List getProducts() {
        if (products == null) products = Product.findForSupplier(getID());
        return products;
    }
```

### [cite\_start]Example: Virtual Proxy (Java) [cite: 1033]

The key to the virtual proxy is providing a class that looks like the actual class you normally use but that actually holds a simple wrapper around the real class.

```java
class SupplierVL...
    private List products;
```

We define an interface for the loading behavior.

```java
public interface VirtualListLoader {
    List load();
}
```

Then we can instantiate the virtual list with a loader that calls the appropriate mapper method.

```java
class SupplierMapper...
    public static class ProductLoader implements VirtualListLoader {
        private Long id;
        public ProductLoader(Long id) {
            this.id = id;
        }
        public List load() {
            return ProductMapper.create().findForSupplier(id);
        }
    }
```

[cite\_start][cite: 1034]

During the load method we assign the product loader to the list field.

```java
class SupplierMapper...
    protected DomainObject doLoad(Long id, ResultSet rs) throws SQLException {
        String nameArg = rs.getString(2);
        SupplierVL result = new SupplierVL(id, nameArg);
        result.setProducts(new VirtualList(new ProductLoader(id)));
        return result;
    }
```

[cite\_start][cite: 1034]

The virtual list’s source list is self-encapsulated and evaluates the loader on first reference.

```java
class VirtualList...
    private List source;
    private VirtualListLoader loader;
    
    public VirtualList(VirtualListLoader loader) {
        this.loader = loader;
    }
    
    private List getSource() {
        if (source == null) source = loader.load();
        return source;
    }
    
    public int size() {
        return getSource().size();
    }
    // ... delegation for other list methods
```

[cite\_start][cite: 1034, 1035]

### [cite\_start]Example: Using a Value Holder (Java) [cite: 1035]

A value holder can be used as a generic Lazy Load.

```java
class SupplierVH...
    private ValueHolder products;
    public List getProducts() {
        return (List) products.getValue();
    }
```

The value holder itself does the Lazy Load behavior.

```java
class ValueHolder...
    private Object value;
    private ValueLoader loader;
    
    public ValueHolder(ValueLoader loader) {
        this.loader = loader;
    }
    
    public Object getValue() {
        if (value == null) value = loader.load();
        return value;
    }
    
    public interface ValueLoader {
        Object load();
    }
```

[cite\_start][cite: 1035]

### [cite\_start]Example: Using Ghosts (C\#) [cite: 1036]

I’ll begin our exploration of ghosts by looking at the domain object **Layer Supertype (475)**. Each domain object knows if it’s a ghost or not.

```csharp
class DomainObject...
    LoadStatus Status;
    public DomainObject (long key) {
        this.Key = key;
    }
    public Boolean IsGhost {
        get {return Status == LoadStatus.GHOST;}
    }
    public Boolean IsLoaded {
        get {return Status == LoadStatus.LOADED;}
    }
    public void MarkLoading() {
        Debug.Assert(IsGhost);
        Status = LoadStatus.LOADING;
    }
    public void MarkLoaded() {
        Debug.Assert(Status == LoadStatus.LOADING);
        Status = LoadStatus.LOADED;
    }
    enum LoadStatus {GHOST, LOADING, LOADED};
```

[cite\_start][cite: 1036]

The most intrusive element of ghosts is that every accessor needs to be modified so that it will trigger a load if the object actually is a ghost.

```csharp
class Employee...
    public String Name {
        get {
            Load();
            return _name;
        }
        set {
            Load();
            _name = value;
        }
    }
    String _name;

class Domain Object...
    protected void Load() {
        if (IsGhost)
            DataSource.Load(this);
    }
```


In order for the loading to work, the domain object needs to call the correct mapper. [cite\_start]To avoid the dependency, I need to use an interesting combination of **Registry (480)** and **Separated Interface (476)**. [cite: 1037]

A registry of mappers, defined in the data source layer, implements the data source interface. The `Load` method finds the correct mapper and tells it to load the appropriate domain object.

```csharp
class MapperRegistry : IDataSource...
    public void Load (DomainObject obj) {
        Mapper(obj.GetType()).Load (obj);
    }
    public static Mapper Mapper(Type type) {
        return (Mapper) instance.mappers[type];
    }
    IDictionary mappers = new Hashtable();
```


Concrete mapper classes have their own find methods. As you can see, the find method returns an object in its ghost state. The actual data does not come from the database until the load is triggered.

```csharp
class EmployeeMapper...
    public override DomainObject CreateGhost(long key) {
        return new Employee(key);
    }

class Mapper...
    public void Load (DomainObject obj) {
        if (! obj.IsGhost) return;
        IDbCommand comm = new OleDbCommand(findStatement(), DB.connection);
        comm.Parameters.Add(new OleDbParameter("key",obj.Key));
        IDataReader reader = comm.ExecuteReader();
        reader.Read();
        LoadLine (reader, obj);
        reader.Close();
    }
    
    public void LoadLine (IDataReader reader, DomainObject obj) {
        if (obj.IsGhost) {
            obj.MarkLoading();
            doLoadLine (reader, obj);
            obj.MarkLoaded();
        }
    }
```


The collection is the most complicated case. To avoid ripple loading, it’s important to load all the time records in a single query. For this we need a special list implementation that acts as a ghost list.

```csharp
class DomainList...
    IList data {
        get {
            Load();
            return _data;
        }
        set {_data = value;}
    }
    IList _data = new ArrayList();
    
    public void Load () {
        if (IsGhost) {
            MarkLoading();
            RunLoader(this);
            MarkLoaded();
        }
    }
    public delegate void Loader(DomainList list);
    public Loader RunLoader;
```


The loader itself has properties to specify the SQL for the load and mapper to use for mapping the time records.

```csharp
class ListLoader...
    public void Load (DomainList list) {
        list.IsLoaded = true;
        IDbCommand comm = new OleDbCommand(Sql, DB.connection);
        // ... execute query ...
        while (reader.Read()) {
            DomainObject obj = GhostForLine(reader);
            Mapper.LoadLine(reader, obj);
            list.Add (obj);
        }
        reader.Close();
    }
```

# Chapter 12

Here is the content extracted from **Chapter 10: Data Source Architectural Patterns** of *Patterns of Enterprise Application Architecture*, formatted in Markdown.

# Chapter 10: Data Source Architectural Patterns

## Table Data Gateway

**An object that acts as a Gateway (466) to a database table. One instance handles all the rows in the table.**

Mixing SQL in application logic can cause several problems. Many developers aren’t comfortable with SQL, and many who are comfortable may not write it well. Database administrators need to be able to find SQL easily so they can figure out how to tune and evolve the database.

A **Table Data Gateway** holds all the SQL for accessing a single table or view: selects, inserts, updates, and deletes. Other code calls its methods for all interaction with the database.

### How It Works

A Table Data Gateway has a simple interface, usually consisting of several find methods to get data from the database and update, insert, and delete methods. Each method maps the input parameters into a SQL call and executes the SQL against a database connection. The Table Data Gateway is usually stateless, as its role is to push data back and forth.

The trickiest thing about a Table Data Gateway is how it returns information from a query. Even a simple find-by-ID query will return multiple data items. In environments where you can return multiple items you can use that for a single row, but many languages give you only a single return value and many queries return multiple rows.

One alternative is to return some simple data structure, such as a map. A map works, but it forces data to be copied out of the record set that comes from the database into the map. I think that using maps to pass data around is bad form because it defeats compile time checking and isn’t a very explicit interface, leading to bugs as people misspell what’s in the map. A better alternative is to use a **Data Transfer Object (401)**. It’s another object to create but one that may well be used elsewhere.

To save all this you can return the **Record Set (508)** that comes from the SQL query. This is conceptually messy, as ideally the in-memory object doesn’t have to know anything about the SQL interface. It may also make it difficult to substitute the database for a file if you can’t easily create record sets in your own code. Nevertheless, in many environments that use Record Set (508) widely, such as .NET, it’s a very effective approach. A Table Data Gateway thus goes very well with **Table Module (125)**. If all of your updates are done through the Table Data Gateway, the returned data can be based on views rather than on the actual tables, which reduces the coupling between your code and the database.

If you’re using a **Domain Model (116)**, you can have the Table Data Gateway return the appropriate domain object. The problem with this is that you then have bidirectional dependencies between the domain objects and the gateway. The two are closely connected, so that isn’t necessarily a terrible thing, but it’s something I’m always reluctant to do.

Most times when you use Table Data Gateway, you’ll have one for each table in the database. For very simple cases, however, you can have a single Table Data Gateway that handles all methods for all tables. You can also have one for views or even for interesting queries that aren’t kept in the database as views. Obviously, view-based Table Data Gateways often can’t update and so won’t have update behavior. However, if you can make updates to the underlying tables, then encapsulating the updates behind update operations on the Table Data Gateway is a very good technique.

### When to Use It

As with **Row Data Gateway (152)** the decision regarding Table Data Gateway is first whether to use a **Gateway (466)** approach at all and then which one.

I find that Table Data Gateway is probably the simplest database interface pattern to use, as it maps so nicely onto a database table or record type. It also makes a natural point to encapsulate the precise access logic of the data source. I use it least with **Domain Model (116)** because I find that **Data Mapper (165)** gives a better isolation between the Domain Model (116) and the database.

Table Data Gateway works particularly well with **Table Module (125)**, where it produces a record set data structure for the Table Module (125) to work on. Indeed, I can’t really imagine any other database-mapping approach for Table Module (125).

Just like Row Data Gateway (152), Table Data Gateway is very suitable for **Transaction Scripts (110)**. The choice between the two really boils down to how they deal with multiple rows of data. Many people like using a **Data Transfer Object (401)**, but that seems to me like more work than is worthwhile, unless the same Data Transfer Object (401) is used elsewhere. I prefer Table Data Gateway when the result set representation is convenient for the Transaction Script (110) to work with.

Interestingly, it often makes sense to have the **Data Mappers (165)** talk to the database via Table Data Gateways. Although this isn’t useful when everything is handcoded, it can be very effective if you want to use metadata for the Table Data Gateways but prefer handcoding for the actual mapping to the domain objects.

One of the benefits of using a Table Data Gateway to encapsulate database access is that the same interface can work both for using SQL to manipulate the database and for using stored procedures. Indeed, stored procedures themselves are often organized as Table Data Gateways. That way the insert and update stored procedures encapsulate the actual table structure. [cite\_start]The find procedures in this case can return views, which helps to hide the underlying table structure [cite: 974-976].

### Example: Person Gateway (C\#)

Table Data Gateway is the usual form of database access in the windows world, so it makes sense to illustrate one with C\#. I have to stress, however, that this classic form of Table Data Gateway doesn’t quite fit in the .NET environment since it doesn’t take advantage of the ADO.NET data set; instead, it uses the data reader, which is a cursor-like interface to database records. The data reader is the right choice for manipulating larger amounts of information when you don’t want to bring everything into memory in one go.

For the example I’m using a Person Gateway class that connects to a person table in a database. The Person Gateway contains the finder code, returning ADO.NET’s data reader to access the returned data.

```csharp
class PersonGateway...
    public IDataReader FindAll() {
        String sql = "select * from person";
        return new OleDbCommand(sql, DB.Connection).ExecuteReader();
    }
    public IDataReader FindWithLastName(String lastName) {
        String sql = "SELECT * FROM person WHERE lastname = ?";
        IDbCommand comm = new OleDbCommand(sql, DB.Connection);
        comm.Parameters.Add(new OleDbParameter("lastname", lastName));
        return comm.ExecuteReader();
    }
    public IDataReader FindWhere(String whereClause) {
        String sql = String.Format("select * from person where {0}", whereClause);
        return new OleDbCommand(sql, DB.Connection).ExecuteReader();
    }
```

The update and insert methods receive the necessary data in arguments and invoke the appropriate SQL routines.

```csharp
class PersonGateway...
    public void Update (long key, String lastname, String firstname, long numberOfDependents){
        String sql = @"
            UPDATE person 
            SET lastname = ?, firstname = ?, numberOfDependents = ? 
            WHERE id = ?";
        IDbCommand comm = new OleDbCommand(sql, DB.Connection);
        comm.Parameters.Add(new OleDbParameter ("last", lastname));
        comm.Parameters.Add(new OleDbParameter ("first", firstname));
        comm.Parameters.Add(new OleDbParameter ("numDep", numberOfDependents));
        comm.Parameters.Add(new OleDbParameter ("key", key));
        comm.ExecuteNonQuery();
    }
    
    public long Insert(String lastName, String firstName, long numberOfDependents) {
        String sql = "INSERT INTO person VALUES (?,?,?,?)";
        long key = GetNextID();
        IDbCommand comm = new OleDbCommand(sql, DB.Connection);
        comm.Parameters.Add(new OleDbParameter ("key", key));
        comm.Parameters.Add(new OleDbParameter ("last", lastName));
        comm.Parameters.Add(new OleDbParameter ("first", firstName));
        comm.Parameters.Add(new OleDbParameter ("numDep", numberOfDependents));
        comm.ExecuteNonQuery();
        return key;
    }
```

The deletion method just needs a key.

```csharp
class PersonGateway...
    public void Delete (long key) {
        String sql = "DELETE FROM person WHERE id = ?";
        IDbCommand comm = new OleDbCommand(sql, DB.Connection);
        comm.Parameters.Add(new OleDbParameter ("key", key));
        comm.ExecuteNonQuery();
    }
```

-----

## Row Data Gateway

**An object that acts as a Gateway (466) to a single record in a data source. There is one instance per row.**

Embedding database access code in in-memory objects can leave you with a few disadvantages. For a start, if your in-memory objects have business logic of their own, adding the database manipulation code increases complexity. Testing is awkward too since, if your in-memory objects are tied to a database, tests are slower to run because of all the database access. You may have to access multiple databases with all those annoying little variations on their SQL.

A **Row Data Gateway** gives you objects that look exactly like the record in your record structure but can be accessed with the regular mechanisms of your programming language. All details of data source access are hidden behind this interface.

### How It Works

A Row Data Gateway acts as an object that exactly mimics a single record, such as one database row. In it each column in the database becomes one field. The Row Data Gateway will usually do any type conversion from the data source types to the in-memory types, but this conversion is pretty simple. This pattern holds the data about a row so that a client can then access the Row Data Gateway directly. The gateway acts as a good interface for each row of data. This approach works particularly well for **Transaction Scripts (110)**.

With a Row Data Gateway you’re faced with the questions of where to put the find operations that generate this pattern. You can use static find methods, but they preclude polymorphism should you want to substitute different finder methods for different data sources. In this case it often makes sense to have separate finder objects so that each table in a relational database will have one finder class and one gateway class for the results.

It’s often hard to tell the difference between a Row Data Gateway and an **Active Record (160)**. The crux of the matter is whether there’s any domain logic present; if there is, you have an Active Record (160). A Row Data Gateway should contain only database access logic and no domain logic.

Row Data Gateways tend to be somewhat tedious to write, but they’re a very good candidate for code generation based on a **Metadata Mapping (306)**. [cite\_start]This way all your database access code can be automatically built for you during your automated build process [cite: 982-983].

### When to Use It

The choice of Row Data Gateway often takes two steps: first whether to use a gateway at all and second whether to use Row Data Gateway or **Table Data Gateway (144)**.

I use Row Data Gateway most often when I’m using a **Transaction Script (110)**. In this case it nicely factors out the database access code and allows it to be reused easily by different Transaction Scripts (110).

I don’t use a Row Data Gateway when I’m using a **Domain Model (116)**. If the mapping is simple, **Active Record (160)** does the same job without an additional layer of code. If the mapping is complex, **Data Mapper (165)** works better, as it’s better at decoupling the data structure from the domain objects because the domain objects don’t need to know the layout of the database.

Interestingly, I’ve seen Row Data Gateway used very nicely with Data Mapper (165). Although this seems like extra work, it can be effective if the Row Data Gateways are automatically generated from metadata while the Data Mappers (165) are done by hand.

If you use Transaction Script (110) with Row Data Gateway, you may notice that you have business logic that’s repeated across multiple scripts; logic that would make sense in the Row Data Gateway. [cite\_start]Moving that logic will gradually turn your Row Data Gateway into an Active Record (160), which is often good as it reduces duplication in the business logic[cite: 983].

### Example: A Person Record (Java)

Here’s an example for Row Data Gateway. It’s a simple person table.

```sql
create table people (ID int primary key, lastname varchar, firstname varchar, number_of_dependents int)
```

`PersonGateway` is a gateway for the table. It starts with data fields and accessors. The gateway class itself can handle updates and inserts.

```java
class PersonGateway...
    public void update() {
        PreparedStatement updateStatement = null;
        try {
            updateStatement = DB.prepare(updateStatementString);
            updateStatement.setString(1, lastName);
            updateStatement.setString(2, firstName);
            updateStatement.setInt(3, numberOfDependents);
            updateStatement.setInt(4, getID().intValue());
            updateStatement.execute();
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {DB.cleanUp(updateStatement);
        }
    }
```

To pull people out of the database, we have a separate `PersonFinder`. This works with the gateway to create new gateway objects.

```java
class PersonFinder...
    public PersonGateway find(Long id) {
        PersonGateway result = (PersonGateway) Registry.getPerson(id);
        if (result != null) return result;
        PreparedStatement findStatement = null;
        ResultSet rs = null;
        try {
            findStatement = DB.prepare(findStatementString);
            findStatement.setLong(1, id.longValue());
            rs = findStatement.executeQuery();
            rs.next();
            result = PersonGateway.load(rs);
            return result;
        } catch (SQLException e) {
            throw new ApplicationException(e);
        } finally {DB.cleanUp(findStatement, rs);
        }
    }
```

-----

## Active Record

**An object that wraps a row in a database table or view, encapsulates the database access, and adds domain logic on that data.**

An object carries both data and behavior. Much of this data is persistent and needs to be stored in a database. Active Record uses the most obvious approach, putting data access logic in the domain object. This way all people know how to read and write their data to and from the database.

### How It Works

The essence of an Active Record is a **Domain Model (116)** in which the classes match very closely the record structure of an underlying database. Each Active Record is responsible for saving and loading to the database and also for any domain logic that acts on the data. This may be all the domain logic in the application, or you may find that some domain logic is held in **Transaction Scripts (110)** with common and data-oriented code in the Active Record.

The data structure of the Active Record should exactly match that of the database: one field in the class for each column in the table. Type the fields the way the SQL interface gives you the data—don’t do any conversion at this stage. You may consider **Foreign Key Mapping (236)**, but you may also leave the foreign keys as they are. You can use views or tables with Active Record, although updates through views are obviously harder. Views are particularly useful for reporting purposes.

The Active Record class typically has methods that do the following:

  * Construct an instance of the Active Record from a SQL result set row
  * Construct a new instance for later insertion into the table
  * Static finder methods to wrap commonly used SQL queries and return Active Record objects
  * Update the database and insert into it the data in the Active Record
  * Get and set the fields
  * Implement some pieces of business logic

Active Record is very similar to **Row Data Gateway (152)**. The principal difference is that a Row Data Gateway (152) contains only database access logic while an Active Record contains both data source and domain logic. [cite\_start]Like most boundaries in software, the line between the two isn’t terribly sharp, but it’s useful [cite: 990-991].

### When to Use It

Active Record is a good choice for domain logic that isn’t too complex, such as creates, reads, updates, and deletes. Derivations and validations based on a single record work well in this structure.

In an initial design for a **Domain Model (116)** the main choice is between Active Record and **Data Mapper (165)**. Active Record has the primary advantage of simplicity. It’s easy to build Active Records, and they are easy to understand. Their primary problem is that they work well only if the Active Record objects correspond directly to the database tables: an isomorphic schema. If your business logic is complex, you’ll soon want to use your object’s direct relationships, collections, inheritance, and so forth. These don’t map easily onto Active Record, and adding them piecemeal gets very messy. That’s what will lead you to use Data Mapper (165) instead.

Active Record is a good pattern to consider if you’re using **Transaction Script (110)** and are beginning to feel the pain of code duplication and the difficulty in updating scripts and tables that Transaction Script (110) often brings. [cite\_start]In this case you can gradually start creating Active Records and then slowly refactor behavior into them [cite: 991-992].

### Example: A Simple Person (Java)

This is a simple, even simplistic, example to show how the bones of Active Record work. We begin with a basic Person class.

```java
class Person...
    private String lastName;
    private String firstName;
    private int numberOfDependents;
```

To load an object, the person class acts as the finder and also performs the load. It uses static methods on the person class.

```java
class Person...
    public static Person find(Long id) {
        Person result = (Person) Registry.getPerson(id);
        if (result != null) return result;
        PreparedStatement findStatement = null;
        ResultSet rs = null;
        try {
            findStatement = DB.prepare(findStatementString);
            findStatement.setLong(1, id.longValue());
            rs = findStatement.executeQuery();
            rs.next();
            result = load(rs);
            return result;
        } catch (SQLException e) {
            throw new ApplicationException(e);
        } finally {
            DB.cleanUp(findStatement, rs);
        }
    }
```

Updating an object takes a simple instance method.

```java
class Person...
    public void update() {
        PreparedStatement updateStatement = null;
        try {
            updateStatement = DB.prepare(updateStatementString);
            updateStatement.setString(1, lastName);
            updateStatement.setString(2, firstName);
            updateStatement.setInt(3, numberOfDependents);
            updateStatement.setInt(4, getID().intValue());
            updateStatement.execute();
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            DB.cleanUp(updateStatement);
        }
    }
```

Any business logic, such as calculating the exemption, sits directly in the Person class.

```java
class Person...
    public Money getExemption() {
        Money baseExemption = Money.dollars(1500);
        Money dependentExemption = Money.dollars(750);
        return baseExemption.add(dependentExemption.multiply(this.getNumberOfDependents()));
    }
```

-----

## Data Mapper

**A layer of Mappers (473) that moves data between objects and a database while keeping them independent of each other and the mapper itself.**

[Image of Data Mapper pattern diagram]

Objects and relational databases have different mechanisms for structuring data. Many parts of an object, such as collections and inheritance, aren’t present in relational databases. When you build an object model with a lot of business logic it’s valuable to use these mechanisms to better organize the data and the behavior that goes with it. Doing so leads to variant schemas; that is, the object schema and the relational schema don’t match up.

You still need to transfer data between the two schemas, and this data transfer becomes a complexity in its own right. If the in-memory objects know about the relational database structure, changes in one tend to ripple to the other.

The **Data Mapper** is a layer of software that separates the in-memory objects from the database. Its responsibility is to transfer data between the two and also to isolate them from each other. With Data Mapper the in-memory objects needn’t know even that there’s a database present; they need no SQL interface code, and certainly no knowledge of the database schema. Since it’s a form of **Mapper (473)**, Data Mapper itself is even unknown to the domain layer.

### How It Works

The separation between domain and data source is the main function of a Data Mapper, but there are plenty of details that have to be addressed to make this happen.

A simple case would have a Person and Person Mapper class. To load a person from the database, a client would call a find method on the mapper. The mapper uses an **Identity Map (195)** to see if the person is already loaded; if not, it loads it. A client asks the mapper to save a domain object. The mapper pulls the data out of the domain object and shuttles it to the database.

A simple Data Mapper would just map a database table to an equivalent in-memory class on a field-to-field basis. Mappers need a variety of strategies to handle classes that turn into multiple fields, classes that have multiple tables, classes with inheritance, and the joys of connecting together objects once they’ve been sorted out.

When it comes to inserts and updates, the database mapping layer needs to understand what objects have changed, which new ones have been created, and which ones have been destroyed. It also has to fit the whole workload into a transactional framework. The **Unit of Work (184)** pattern is a good way to organize this.

**Handling Finders:** In order to work with an object, you have to load it from the database. Usually the presentation layer will initiate things by loading some initial objects. Then control moves into the domain layer, at which point the code will mainly move from object to object using associations between them. On occasion you may need the domain objects to invoke find methods on the Data Mapper. However, I’ve found that with a good **Lazy Load (200)** you can completely avoid this.

**Mapping Data to Domain Fields:** Mappers need access to the fields in the domain objects. Often this can be a problem because you need public methods to support the mappers you don’t want for domain logic. You can use reflection, which can often bypass the visibility rules of the language.

### When to Use It

The primary occasion for using Data Mapper is when you want the database schema and the object model to evolve independently. The most common case for this is with a **Domain Model (116)**. Data Mapper’s primary benefit is that when working on the domain model you can ignore the database, both in design and in the build and testing process.

The price, of course, is the extra layer that you don’t get with **Active Record (160)**, so the test for using these patterns is the complexity of the business logic. If you have fairly simple business logic, you probably won’t need a Domain Model (116) or a Data Mapper. More complicated logic leads you to Domain Model (116) and therefore to Data Mapper.

I wouldn’t choose Data Mapper without Domain Model (116).

Remember that you don’t have to build a full-featured database-mapping layer. It’s a complicated beast to build, and there are products available that do this for you. [cite\_start]For most cases I recommend buying a database-mapping layer rather than building one yourself [cite: 995-1001].

### Example: A Simple Database Mapper (Java)

Here’s an absurdly simple use of Data Mapper to give you a feel for the basic structure. Our example is a person with an isomorphic people table. We’ll use the simple case here, where the Person Mapper class also implements the finder and **Identity Map (195)**. However, I’ve added an abstract mapper **Layer Supertype (475)** to indicate where I can pull out some common behavior.

The find behavior starts in the Person Mapper, which wraps calls to an abstract find method to find by ID.

```java
class PersonMapper...
    protected String findStatement() {
        return "SELECT " + COLUMNS + " FROM people" + " WHERE id = ?";
    }
    public Person find(Long id) {
        return (Person) abstractFind(id);
    }
```

The find method calls the load method, which is split between the abstract and person mappers. The abstract mapper checks the ID, pulling it from the data and registering the new object in the **Identity Map (195)**.

```java
class AbstractMapper...
    protected DomainObject load(ResultSet rs) throws SQLException {
        Long id = new Long(rs.getLong(1));
        if (loadedMap.containsKey(id)) return (DomainObject) loadedMap.get(id);
        DomainObject result = doLoad(id, rs);
        loadedMap.put(id, result);
        return result;
    }
```

Notice that the Identity Map (195) is checked twice, once by abstractFind and once by load. I need to check the map in the finder because, if the object is already there, I can save myself a trip to the database. But I also need to check in the load because I may have queries that I can’t be sure of resolving in the Identity Map (195).

With the update the JDBC code is specific to the subtype.

```java
class PersonMapper...
    public void update(Person subject) {
        PreparedStatement updateStatement = null;
        try {
            updateStatement = DB.prepare(updateStatementString);
            updateStatement.setString(1, subject.getLastName());
            updateStatement.setString(2, subject.getFirstName());
            updateStatement.setInt(3, subject.getNumberOfDependents());
            updateStatement.setInt(4, subject.getID().intValue());
            updateStatement.execute();
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            DB.cleanUp(updateStatement);
        }
    }
```

# Chpater 13, 14

Here is the content extracted from **Chapter 13: Object-Relational Metadata Mapping Patterns** and **Chapter 14: Web Presentation Patterns** of *Patterns of Enterprise Application Architecture*, formatted in Markdown.

# Chapter 13: Object-Relational Metadata Mapping Patterns

## Metadata Mapping

**Holds details of object-relational mapping in metadata.**

Much of the code that deals with object-relational mapping describes how fields in the database correspond to fields in in-memory objects. The resulting code tends to be tedious and repetitive to write. A **Metadata Mapping** allows developers to define the mappings in a simple tabular form, which can then be processed by generic code to carry out the details of reading, inserting, and updating the data.

### How It Works

The biggest decision in using Metadata Mapping is how the information in the metadata manifests itself in terms of running code. There are two main routes to take: code generation and reflective programming.

  * **Code Generation:** You write a program whose input is the metadata and whose output is the source code of classes that do the mapping. These classes look as though they’re hand-written, but they’re entirely generated during the build process, usually just prior to compilation. The resulting mapper classes are deployed with the server code.
  * **Reflective Programming:** A reflective program may ask an object for a method named `setName`, and then run an `invoke` method on the `setName` method passing in the appropriate argument. By treating methods (and fields) as data the reflective program can read in field and method names from a metadata file and use them to carry out the mapping.

Code generation is a less dynamic approach since any changes to the mapping require recompiling and redeploying at least that part of the software. With a reflective approach, you can just change the mapping data file and the existing classes will use the new metadata. You can even do this during runtime.

Reflective programming often suffers in speed, although the problem here depends very much on the actual environment you’re using. Remember, though, that the reflection is being done in the context of an SQL call, so its slower speed may not make that much difference considering the slow speed of the remote call.

On most occasions you keep the metadata in a separate file format. These days XML is a popular choice as it provides hierarchic structuring while freeing you from writing your own parsers and other tools. [cite\_start]In simpler cases you can skip the external file format and create the metadata representation directly in source code [cite: 1136-1137].

One of the challenges of metadata is that although a simple metadata scheme often works well 90 percent of the time, there are often special cases that make life much more tricky. A useful alternative is to override the generic code with subclasses where the special code is handwritten.

### When to Use It

Metadata Mapping can greatly reduce the amount of work needed to handle database mapping. However, some setup work is required to prepare the Metadata Mapping framework. Also, while it’s often easy to handle most cases with Metadata Mapping, you can find exceptions that really tangle the metadata.

If you’re building your own system, you should evaluate the trade-offs yourself. Compare adding new mappings using handwritten code with using Metadata Mapping. If you use reflection, look into its consequences for performance. The extra work of hand-coding can be greatly reduced by creating a good **Layer Supertype (475)** that handles all the common behavior.

Metadata Mapping can make refactoring the database easier, since the metadata represents a statement of the interface of your database schema. Thus, alterations to the database can be contained by changes in the Metadata Mapping.

### Example: Using Metadata and Reflection (Java)

Most examples in this book use explicit code because it’s the easiest to understand. However, it does lead to pretty tedious programming. You can remove a lot of tedious programming by using metadata.

**Holding the Metadata:**
First, how is the metadata kept? Here I’m keeping it in two classes. The data map corresponds to the mapping of one class to one table.

```java
class DataMap...
    private Class domainClass;
    private String tableName;
    private List columnMaps = new ArrayList();
```

The data map contains a collection of column maps that map columns in the table to fields.

```java
class ColumnMap...
    private String columnName;
    private String fieldName;
    private Field field;
    private DataMap dataMap;
```

I’m using the default Java type mappings and forcing a one-to-one relationship between tables and classes. I populate them with Java code in specific mapper classes.

```java
class PersonMapper...
    protected void loadDataMap(){
        dataMap = new DataMap (Person.class, "people");
        dataMap.addColumn ("lastname", "varchar", "lastName");
        dataMap.addColumn ("firstname", "varchar", "firstName");
        dataMap.addColumn ("number_of_dependents", "int", "numberOfDependents");
    }
```

**Find by ID:**
The strength of the metadata approach is that all of the code that actually manipulates things is in a superclass.

```java
class Mapper...
    public Object findObject (Long key) {
        if (uow.isLoaded(key)) return uow.getObject(key);
        String sql = "SELECT" + dataMap.columnList() + " FROM " + dataMap.getTableName() + " WHERE ID = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DomainObject result = null;
        try {
            stmt = DB.prepare(sql);
            stmt.setLong(1, key.longValue());
            rs = stmt.executeQuery();
            rs.next();
            result = load(rs);
        } catch (Exception e) {throw new ApplicationException (e);
        } finally {DB.cleanUp(stmt, rs);
        }
        return result;
    }
```

The `load` method uses reflection to load the fields.

```java
class Mapper...
    public DomainObject load(ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException {
        Long key = new Long(rs.getLong("ID"));
        if (uow.isLoaded(key)) return uow.getObject(key);
        DomainObject result = (DomainObject) dataMap.getDomainClass().newInstance();
        result.setID(key);
        uow.registerClean(result);
        loadFields(rs, result);
        return result;
    }

    private void loadFields(ResultSet rs, DomainObject result) throws SQLException {
        for (Iterator it = dataMap.getColumns(); it.hasNext();) {
            ColumnMap columnMap = (ColumnMap)it.next();
            Object columnValue = rs.getObject(columnMap.getColumnName());
            columnMap.setField(result, columnValue);
        }
    }
```

**Writing to the Database:**
For updates I have a single update routine.

```java
class Mapper...
    public void update (DomainObject obj) {
        String sql = "UPDATE " + dataMap.getTableName() + dataMap.updateList() + " WHERE ID = ?";
        PreparedStatement stmt = null;
        try {
            stmt = DB.prepare(sql);
            int argCount = 1;
            for (Iterator it = dataMap.getColumns(); it.hasNext();) {
                ColumnMap col = (ColumnMap) it.next();
                stmt.setObject(argCount++, col.getValue(obj));
            }
            stmt.setLong(argCount, obj.getID().longValue());
            stmt.executeUpdate();
        } catch (SQLException e) {throw new ApplicationException (e);
        } finally {DB.cleanUp(stmt);
        }
    }
```

Inserts use a similar scheme.

-----

## Query Object

**An object that represents a database query.**

SQL can be an involved language, and many developers aren’t particularly familiar with it. Furthermore, you need to know what the database schema looks like to form queries. You can avoid this by creating specialized finder methods, but that makes it difficult to form more ad hoc queries. It also leads to duplication in the SQL statements should the database schema change.

A **Query Object** is an interpreter [Gang of Four], that is, a structure of objects that can form itself into a SQL query. You can create this query by referring to classes and fields rather than tables and columns. In this way those who write the queries can do so independently of the database schema and changes to the schema can be localized in a single place.

### How It Works

A Query Object is an application of the Interpreter pattern geared to represent a SQL query. Its primary roles are to allow a client to form queries of various kinds and to turn those object structures into the appropriate SQL string.

A common feature of Query Object is that it can represent queries in the language of the in-memory objects rather than the database schema. That means that, instead of using table and column names, you can use object and field names. In order to perform this change of view, the Query Object needs to know how the database structure maps to the object structure, a capability that really needs **Metadata Mapping (306)**.

For multiple databases you can design your Query Object so that it produces different SQL depending on which database the query is running against. A particularly sophisticated use of Query Object is to eliminate redundant queries against a database.

A variation on the Query Object is to allow a query to be specified by an example domain object (Query by Example). [cite\_start]This is very simple and convenient to use, but it breaks down for complex queries [cite: 1146-1147].

### When to Use It

Query Objects are a pretty sophisticated pattern to put together, so most projects don’t use them if they have a handbuilt data source layer. You only really need them when you’re using **Domain Model (116)** and **Data Mapper (165)**; you also really need **Metadata Mapping (306)** to make serious use of them.

Even then Query Objects aren’t always necessary, as many developers are comfortable with SQL. You can hide many of the details of the database schema behind specific finder methods. The advantages of Query Object come with more sophisticated needs: keeping database schemas encapsulated, supporting multiple databases, supporting multiple schemas, and optimizing to avoid multiple queries.

### Example: A Simple Query Object (Java)

This example can query a single table based on a set of criteria "AND’ed" together. The Query Object is set up using the language of domain objects.

```java
class QueryObject...
    private Class klass;
    private List criteria = new ArrayList();
```

A simple criterion takes a field, a value, and an SQL operator.

```java
class Criteria...
    private String sqlOperator;
    protected String field;
    protected Object value;
    
    public static Criteria greaterThan(String fieldName, int value) {
        return Criteria.greaterThan(fieldName, new Integer(value));
    }
```

This allows me to find everyone with dependents by forming a query such as:

```java
QueryObject query = new QueryObject(Person.class);
query.addCriteria(Criteria.greaterThan("numberOfDependents", 0));
```

The query executes by turning itself into a SQL select. It uses a **Unit of Work (184)** and **Metadata Mapping (306)**.

```java
class QueryObject...
    public Set execute(UnitOfWork uow) {
        this.uow = uow;
        return uow.getMapper(klass).findObjectsWhere(generateWhereClause());
    }

    private String generateWhereClause() {
        StringBuffer result = new StringBuffer();
        for (Iterator it = criteria.iterator(); it.hasNext();) {
            Criteria c = (Criteria)it.next();
            if (result.length() != 0)
                result.append(" AND ");
            result.append(c.generateSql(uow.getMapper(klass).getDataMap()));
        }
        return result.toString();
    }
```

-----

## Repository

*by Edward Hieatt and Rob Mee*

**Mediates between the domain and data mapping layers using a collection-like interface for accessing domain objects.**

A system with a complex domain model often benefits from a layer, such as the one provided by **Data Mapper (165)**, that isolates domain objects from details of the database access code. In such systems it can be worthwhile to build another layer of abstraction over the mapping layer where query construction code is concentrated.

A **Repository** mediates between the domain and data mapping layers, acting like an in-memory domain object collection. Client objects construct query specifications declaratively and submit them to Repository for satisfaction. Objects can be added to and removed from the Repository, as they can from a simple collection of objects, and the mapping code encapsulated by the Repository will carry out the appropriate operations behind the scenes.

### How It Works

Repository is a sophisticated pattern that makes use of a fair number of the other patterns described in this book. In fact, it looks like a small piece of an object-oriented database and in that way it’s similar to **Query Object (316)**. However, if a team has taken the leap and built Query Object (316), it isn’t a huge step to add a Repository capability.

Repository presents a simple interface. Clients create a criteria object specifying the characteristics of the objects they want returned from a query. For example: `criteria.equals(Person.LAST_NAME, "Fowler")`. Then we invoke `repository.matching(criteria)` to return a list of domain objects.

To code that uses a Repository, it appears as a simple in-memory collection of domain objects. The fact that the domain objects themselves typically aren’t stored directly in the Repository is not exposed to the client code.

Repository replaces specialized finder methods on **Data Mapper (165)** classes with a specification-based approach to object selection. Under the covers, Repository combines **Metadata Mapping (306)** with a **Query Object (316)** to automatically generate SQL code from the criteria.

The object source for the Repository may not be a relational database at all. Repository lends itself quite readily to the replacement of the data-mapping component via specialized strategy objects. [cite\_start]For this reason it can be especially useful in systems with multiple database schemas or sources for domain objects, as well as during testing when use of exclusively in-memory objects is desirable for speed [cite: 1153-1154].

### When to Use It

In a large system with many domain object types and many possible queries, Repository reduces the amount of code needed to deal with all the querying that goes on. Clients need never think in SQL and can write code purely in terms of objects.

However, situations with multiple data sources are where we really see Repository coming into its own. Suppose we’re interested in using a simple in-memory data store, commonly when we want to run a suite of unit tests entirely in memory for better performance. It’s also conceivable that certain types of domain objects should always be stored in memory (e.g., immutable domain objects).

### Example: Finding a Person’s Dependents (Java)

From the client object’s perspective, using a Repository is simple.

```java
public class Person {
    public List dependents() {
        Repository repository = Registry.personRepository();
        Criteria criteria = new Criteria();
        criteria.equal(Person.BENEFACTOR, this);
        return repository.matching(criteria);
    }
}
```

Common queries can be accommodated with specialized subclasses of Repository.

```java
public class PersonRepository extends Repository {
    public List list dependentsOf(Person aPerson) {
        Criteria criteria = new Criteria();
        criteria.equal(Person.BENEFACTOR, aPerson);
        return matching(criteria);
    }
}
```

### Example: Swapping Repository Strategies (Java)

Because Repository’s interface shields the domain layer from awareness of the data source, we can refactor the implementation of the querying code inside the Repository without changing any calls from clients. We can delegate to a strategy object that does the querying.

```java
abstract class Repository {
    private RepositoryStrategy strategy;
    protected List matching(Criteria aCriteria) {
        return strategy.matching(aCriteria);
    }
}
```

A `RelationalStrategy` implements `matching()` by creating a Query Object from the criteria and then querying the database. An `InMemoryStrategy` implements `matching()` by iterating over a collection of domain objects and asking the criteria at each domain object if it’s satisfied by it.

-----

# Chapter 14: Web Presentation Patterns

## Model View Controller

**Splits user interface interaction into three distinct roles.**

[Image of MVC Pattern Structure]

Model View Controller (MVC) is one of the most quoted (and most misquoted) patterns around. It started as a framework developed by Trygve Reenskaug for the Smalltalk platform in the late 1970s.

### How It Works

MVC considers three roles.

  * **Model:** An object that represents some information about the domain. It’s a nonvisual object containing all the data and behavior other than that used for the UI.
  * **View:** Represents the display of the model in the UI. The view is only about display of information.
  * **Controller:** Takes user input, manipulates the model, and causes the view to update appropriately. In this way UI is a combination of the view and the controller.

I see two principal separations: separating the presentation from the model and separating the controller from the view.

**Separating Presentation from Model:** This is one of the most fundamental heuristics of good software design. Presentation and model are about different concerns. Users may want to see the same basic model information in different ways. Nonvisual objects are usually easier to test than visual ones. A key point is that the presentation depends on the model but the model doesn’t depend on the presentation. This implies that if a user changes the model from one presentation, others need to change as well, typically requiring an implementation of the Observer pattern.

**Separating View and Controller:** The separation of view and controller is less important, so I’d only recommend doing it when it is really helpful. The classic example is to support editable and noneditable behavior with one view and two controllers. In Web interfaces, it becomes useful for separating the controller and view again.

### When to Use It

The separation of presentation and model is one of the most important design principles in software, and the only time you shouldn’t follow it is in very simple systems where the model has no real behavior anyway. The separation of view and controller is less important, although it’s common in Web front ends where the controller is separated out.

-----

## Page Controller

**An object that handles a request for a specific page or action on a Web site.**

Most people’s basic Web experience is with static HTML pages. With dynamic pages things can get much more interesting. **Page Controller** has one input controller for each logical page of the Web site. That controller may be the page itself, or it may be a separate object that corresponds to that page.

### How It Works

The basic idea behind a Page Controller is to have one module on the Web server act as the controller for each page on the Web site. The Page Controller can be structured either as a script (CGI script, servlet) or as a server page (ASP, PHP, JSP). Using a server page usually combines the Page Controller and a **Template View (350)** in the same file. This works well for the Template View but less well for the Page Controller because it’s more awkward to properly structure the module.

One way of dealing with scriptlet code in server pages is to use a helper object. The first thing the server page does is call the helper object to handle all the logic.

The basic responsibilities of a Page Controller are:

  * Decode the URL and extract any form data to figure out all the data for the action.
  * Create and invoke any model objects to process the data.
  * Determine which view should display the result page and forward the model information to it.

### When to Use It

The main decision point is whether to use Page Controller or **Front Controller (344)**. Page Controller is the most familiar to work with and leads to a natural structuring mechanism. However, Front Controller allows for centralization of common controller logic. Page Controller works particularly well in a site where most of the controller logic is pretty simple.

### Example: Simple Display with a Servlet Controller and a JSP View (Java)

A simple example displays some information about a recording artist. The Web server is configured to recognize `/artist` as a call to `ArtistController`.

```java
class ArtistController...
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Artist artist = Artist.findNamed(request.getParameter("name"));
        if (artist == null)
            forward("/MissingArtistError.jsp", request, response);
        else {
            request.setAttribute("helper", new ArtistHelper(artist));
            forward("/artist.jsp", request, response);
        }
    }
```

### Example: Using a JSP as a Handler (Java)

You can make a server page as the request handler while delegating control to the helper to actually carry out the controller function. The handler JSP is also the default view.

```jsp
<jsp:useBean id="helper" class="actionController.AlbumConHelper"/>
<%helper.init(request, response);%>
```

The call to init sets the helper up to carry out the controller behavior.

```java
class AlbumConHelper extends HelperController...
    public void init(HttpServletRequest request, HttpServletResponse response) {
        super.init(request, response);
        if (getAlbum() == null) forward("missingAlbumError.jsp", request, response);
        if (getAlbum() instanceof ClassicalAlbum) {
            request.setAttribute("helper", getAlbum());
            forward("/classicalAlbum.jsp", request, response);
        }
    }
```

-----

## Front Controller

**A controller that handles all requests for a Web site.**

In a complex Web site there are many similar things you need to do when handling a request, such as security, internationalization, and providing particular views for certain users. The **Front Controller** consolidates all request handling by channeling requests through a single handler object. This object can carry out common behavior, which can be modified at runtime with decorators. The handler then dispatches to command objects for behavior particular to a request.

### How It Works

A Front Controller handles all calls for a Web site, and is usually structured in two parts: a Web handler and a command hierarchy. The Web handler is the object that actually receives post or get requests from the Web server. It pulls just enough information from the URL and the request to decide what kind of action to initiate and then delegates to a command to carry out the action.

The Web handler can decide which command to run either statically (parsing URL) or dynamically (standard piece of URL to instantiate command class). A particularly useful pattern to use in conjunction with Front Controller is Intercepting Filter, which allows you to build a filter chain to handle issues such as authentication and logging.

### When to Use It

The Front Controller is a more complicated design than Page Controller. It is worth the effort if you need to centralize common logic. Only one Front Controller has to be configured into the Web server. With dynamic commands you can add new commands without changing anything.

### Example: Simple Display (Java)

Here’s a simple case of using Front Controller. We’ll use dynamic commands.

```java
class FrontServlet...
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        FrontCommand command = getCommand(request);
        command.init(getServletContext(), request, response);
        command.process();
    }

    private FrontCommand getCommand(HttpServletRequest request) {
        try {
            return (FrontCommand) getCommandClass(request).newInstance();
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }
```

The command object just implements the process method.

```java
class ArtistCommand...
    public void process() throws ServletException, IOException {
        Artist artist = Artist.findNamed(request.getParameter("name"));
        request.setAttribute("helper", new ArtistHelper(artist));
        forward("/artist.jsp");
    }
```

-----

## Template View

**Renders information into HTML by embedding markers in an HTML page.**

Writing a program that spits out HTML is often more difficult than you might imagine. The best way to work is to compose the dynamic Web page as you do a static page but put in markers that can be resolved into calls to gather dynamic information. Since the static part of the page acts as a template for the particular response, I call this a **Template View**.

### How It Works

The basic idea is to embed markers into a static HTML page. When the page is used to service a request, the markers are replaced by the results of some computation. One of the most popular forms of Template View is a server page such as ASP, JSP, or PHP. These allow you to embed arbitrary programming logic (scriptlets), but this can lead to messy code. The key to avoiding scriptlets is to provide a regular object as a **Helper** to each page. The helper has all the real programming logic.

**Conditional Display:** A knotty issue is conditional page behavior. Purely conditional tags are a bad smell. If you’re displaying some text conditionally, one option is to move the condition into the helper.

### When to Use It

The strength of Template View is that it allows you to compose the content of the page by looking at the page structure. It nicely supports the idea of a graphic designer laying out a page with a programmer working on the helper. Template View has two significant weaknesses: it's easy to put too much logic in the page, and it's hard to test without a web server. The alternative is **Transform View (361)**.

### Example: Using a JSP as a View with a Separate Controller (Java)

When using a JSP as a view only, it’s always invoked from a controller. The controller creates a helper object and passes it to the JSP.

```java
request.setAttribute("helper", new ArtistHelper(artist));
forward("/artist.jsp", request, response);
```

The server page uses the helper to access information.

```jsp
<jsp:useBean id="helper" type="actionController.ArtistHelper" scope="request"/>
<B><jsp:getProperty name="helper" property="name"/></B>
```

-----

## Transform View

**A view that processes domain data element by element and transforms it into HTML.**

Using **Transform View** means thinking of the view as a transformation where you have the model’s data as input and its HTML as output.

### How It Works

The basic notion is writing a program that looks at domain-oriented data and converts it to HTML. The program walks the structure of the domain data and writes out the particular piece of HTML for each element. The dominant choice for this is XSLT.

To carry out an XSLT transform we need to begin with some XML data. The simplest way is if the domain logic returns XML. Failing that, we need to produce the XML ourselves, perhaps by populating a **Data Transfer Object (401)** that can serialize itself into XML.

### When to Use It

The choice between Transform View and Template View mostly comes down to which environment the team prefers. XSLT is portable and fits naturally in an XML world. Transform View avoids the problems of logic in the page and is easier to test. However, XSLT can be awkward to learn.

### Example: Simple Transform (Java)

The command object invokes methods on the model to obtain an XML input document and then passes that through the XML processor.

```java
class AlbumCommand...
    public void process() {
        // ... get album ...
        PrintWriter out = response.getWriter();
        XsltProcessor processor = new SingleStepXsltProcessor("album.xsl");
        out.print(processor.getTransformation(album.toXmlDocument()));
    }
```

The translation is done by an XSLT program.

```xml
<xsl:template match="album">
    <HTML><BODY bgcolor="white">
        <xsl:apply-templates/>
    </BODY></HTML>
</xsl:template>
```

-----

## Two Step View

**Turns domain data into HTML in two steps: first by forming some kind of logical page, then rendering the logical page into HTML.**

If you have a Web application with many pages, you often want a consistent look and organization. Global changes can be difficult with Template View or Transform View because presentation decisions are duplicated. **Two Step View** splits the transformation into two stages. The first transforms the model data into a logical presentation without specific formatting; the second converts that logical presentation with the actual formatting needed.

### How It Works

The first stage assembles the information in a logical screen structure (e.g., fields, headers, tables) that contains no HTML. The second stage takes this presentation-oriented structure and renders it into HTML. Thus, a system with many screens can be rendered as HTML by a single second stage so that all the HTML formatting decisions are made in one place.

This can be implemented with two-step XSLT or with classes.

### When to Use It

Two Step View’s key value comes from the separation of first and second stages, allowing global changes more easily. This is particularly useful for multi-appearance Web applications (same functionality, different looks for different organizations) or supporting different devices (browser vs PDA).

### Example: Two Stage XSLT (XSLT)

The first stage XSLT transforms domain XML into screen-oriented XML.

```xml
<xsl:template match="album">
    <screen><xsl:apply-templates/></screen>
</xsl:template>
<xsl:template match="artist">
    <field label="Artist"><xsl:apply-templates/></field>
</xsl:template>
```

The second stage XSLT transforms screen XML into HTML.

```xml
<xsl:template match="screen">
    <HTML><BODY bgcolor="white"><xsl:apply-templates/></BODY></HTML>
</xsl:template>
<xsl:template match="field">
    <P><B><xsl:value-of select = "@label"/>: </B><xsl:apply-templates/></P>
</xsl:template>
```

-----

## Application Controller

**A centralized point for handling screen navigation and the flow of an application.**

Some applications contain a significant amount of logic about the screens to use at different points (wizard style, conditional screens). To avoid duplicating this flow logic across multiple input controllers, you can use an **Application Controller**. Input controllers then ask the Application Controller for the appropriate commands and view.

### How It Works

An Application Controller has two main responsibilities: deciding which domain logic to run and deciding the view with which to display the response. It typically holds two structured collections of class references, one for domain commands and one for views.

The Application Controller should not have dependencies to the UI machinery so it can be tested independently. An application can have multiple Application Controllers for different parts.

### When to Use It

If the flow and navigation of your application are simple enough so that anyone can visit any screen in any order, there’s little value in an Application Controller. Its strength comes from definite rules about the order in which pages should be visited.

### Example: State Model Application Controller (Java)

We use a simple state model for commands on an asset (e.g., return, damage). The input controller is a Front Controller that delegates to the Application Controller.

```java
class FrontServlet...
    public void service(HttpServletRequest request, HttpServletResponse response) {
        ApplicationController appController = getApplicationController(request);
        String commandString = (String) request.getParameter("command");
        DomainCommand comm = appController.getDomainCommand(commandString, getParameterMap(request));
        comm.run(getParameterMap(request));
        String viewPage = "/" + appController.getView(commandString, getParameterMap(request)) + ".jsp";
        forward(viewPage, request, response);
    }
```

The application controller holds on to the responses using a map of maps indexed by command string and asset status.

```java
class AssetApplicationController...
    public DomainCommand getDomainCommand (String commandString, Map params) {
        Response reponse = getResponse(commandString, getAssetStatus(params));
        return reponse.getDomainCommand();
    }
```

# Chapter 15, 16

Based on **Chapter 15: Distribution Patterns** and **Chapter 16: Offline Concurrency Patterns** from *Patterns of Enterprise Application Architecture*, here is a comprehensive extraction of the content, patterns, and examples.

-----

# Chapter 15: Distribution Patterns

## Remote Facade

**Provides a coarse-grained facade on fine-grained objects to improve efficiency over a network.**

In an object-oriented model, fine-grained objects with small methods work well within a single address space. However, when making calls between processes, fine-grained interactions kill performance due to the latency of remote calls. A **Remote Facade** is a coarse-grained facade over a web of fine-grained objects. [cite\_start]None of the fine-grained objects have a remote interface, and the Remote Facade contains no domain logic [cite: 1218-1219].

### How It Works

The Remote Facade acts as a thin skin that translates coarse-grained methods onto the underlying fine-grained objects.

  * **Bulk Accessors:** Instead of individual getters/setters, use methods like `getAddressData` that return all data for an object in one call.
  * **Data Transfer:** Data must be serializable. Often, **Data Transfer Objects (401)** are used to bundle data from multiple domain objects.
  * **Granularity:** You might have one Remote Facade per use case or family of screens.
  * **State:** Remote Facade can be stateful (holding session state) or stateless. Stateless is usually preferred for scalability, but stateful can improve performance for specific sessions.
  * **Responsibilities:** It is a natural point for security checks and transaction control (starting and committing a transaction per call). [cite\_start]It should **not** contain domain logic [cite: 1219-1221].

### When to Use It

Use it whenever you need remote access to a fine-grained object model. It gives you the best of both worlds: a fine-grained domain model for complex logic and a coarse-grained interface for network efficiency. [cite\_start]It is most common between a presentation layer and a domain model running on different processes[cite: 1222].

### Example: Using a Java Session Bean as a Remote Facade (Java)

A Java Session Bean is a good choice for a distributed facade. This example uses a session bean to wrap a POJO domain model.

The interface (`AlbumService`) declares coarse-grained methods:

```java
interface AlbumService...
    AlbumDTO getAlbum(String id) throws RemoteException;
    void createAlbum(String id, AlbumDTO dto) throws RemoteException;
    void updateAlbum(String id, AlbumDTO dto) throws RemoteException;
```

The implementation (`AlbumServiceBean`) delegates to the domain objects and assemblers:

```java
class AlbumServiceBean...
    public AlbumDTO getAlbum(String id) throws RemoteException {
        return new AlbumAssembler().writeDTO(Registry.findAlbum(id));
    }
    public void createAlbum(String id, AlbumDTO dto) throws RemoteException {
        new AlbumAssembler().createAlbum(id, dto);
    }
```

[cite\_start][cite: 1223-1225]

### Example: Web Service (C\#)

A Web Service is essentially an interface for remote usage. This example shows an `AlbumService` exposed as a Web Service.

```csharp
class AlbumService...
    [ WebMethod ]
    public AlbumDTO GetAlbum(String key) {
        Album result = new AlbumFinder()[key];
        if (result == null) 
            throw new SoapException ("unable to find album", SoapException.ClientFaultCode);
        else return new AlbumAssembler().WriteDTO(result);
    }
```

The data structures (DTOs) are defined in WSDL automatically by the framework. [cite\_start]The key takeaway is layering the distribution ability (Web Service) on top of a fine-grained domain model using Remote Facades and DTOs [cite: 1226-1230].

-----

## Data Transfer Object

**An object that carries data between processes in order to reduce the number of method calls.**

When working with a remote interface, you need to transfer large amounts of data in a single call. A **Data Transfer Object (DTO)** holds all the data for the call. It needs to be serializable. [cite\_start]Usually, an **Assembler** is used on the server side to transfer data between the DTO and the domain objects[cite: 1231].

### How It Works

A DTO is often just a bunch of fields and getters/setters. It aggregates data from multiple server objects (e.g., an Order, Customer, and Line Items) into one structure.

  * **Structure:** Simple fields (primitives, strings, dates) or other DTOs. It should not contain domain logic.
  * **Serialization:** It must be serializable (binary, XML, JSON).
  * **Assembler:** A separate object responsible for creating the DTO from the domain model and updating the domain model from the DTO. [cite\_start]This keeps the domain model independent of the DTO structure [cite: 1232-1235].

### When to Use It

Use a DTO whenever you need to transfer multiple items of data between two processes in a single method call. [cite\_start]It is also useful for communicating via XML or between layers (e.g., passing data to a UI)[cite: 1236].

### Example: Transferring Information About Albums (Java)

We transfer data about an `Album`, its `Artist`, and its `Tracks`. The DTO collapses this structure: Artist data is moved into the Album DTO, and Tracks are an array.

**The Assembler (Writing DTO):**

```java
class AlbumAssembler...
    public AlbumDTO writeDTO(Album subject) {
        AlbumDTO result = new AlbumDTO();
        result.setTitle(subject.getTitle());
        result.setArtist(subject.getArtist().getName());
        writeTracks(result, subject);
        return result;
    }
```

**The Assembler (Updating Domain from DTO):**

```java
class AlbumAssembler...
    public void updateAlbum(String id, AlbumDTO source) {
        Album current = Registry.findAlbum(id);
        if (source.getTitle() != current.getTitle()) current.setTitle(source.getTitle());
        if (source.getArtist() != current.getArtist().getName()) {
            // Logic to find new artist object...
            current.setArtist(artist);
        }
        updateTracks(source, current);
    }
```

[cite\_start][cite: 1238-1240]

-----

# Chapter 16: Offline Concurrency Patterns

## Optimistic Offline Lock

*by David Rice*

**Prevents conflicts between concurrent business transactions by detecting a conflict and rolling back the transaction.**

A business transaction often spans multiple system transactions. **Optimistic Offline Lock** validates that the changes about to be committed by one session don't conflict with the changes of another session. It assumes conflict is low.

### How It Works

  * **Versioning:** Associate a version number (or timestamp) with each record.
  * **Validation:** When updating, check if the version in the database matches the version read at the start of the session.
  * **SQL:** `UPDATE table SET ... version = version + 1 WHERE id = ? AND version = ?`
  * **Failure:** If the row count returned is 0, someone else modified the record. Roll back and throw a `ConcurrencyException`.
  * [cite\_start]**Inconsistent Reads:** You must also check the version of objects you read (but didn't update) if your calculations depended on them [cite: 1246-1249].

### When to Use It

Use when the chance of conflict is low. [cite\_start]It provides higher concurrency than pessimistic locking but forces users to redo work if a conflict occurs[cite: 1250].

### Example: Domain Layer with Data Mappers (Java)

**Abstract Mapper:** Handles the version check logic.

```java
class AbstractMapper...
    public void update(DomainObject object) {
        // ... Connection setup ...
        stmt = conn.prepareStatement(updateSQL); // Includes "WHERE id=? AND version=?"
        // ... bind parameters ...
        int rowCount = stmt.executeUpdate();
        if (rowCount == 0) {
            throwConcurrencyException(object);
        }
    }
```

**Unit of Work:** Can perform consistent read checks during commit.

```java
class UnitOfWork...
    public void commit() {
        try {
            checkConsistentReads(); // Verifies versions of read-only objects
            insertNew();
            deleteRemoved();
            updateDirty();
        } catch (ConcurrencyException e) {
            rollbackSystemTransaction();
            throw e;
        }
    }
```

[cite\_start][cite: 1252-1255]

-----

## Pessimistic Offline Lock

*by David Rice*

**Prevents conflicts between concurrent business transactions by allowing only one business transaction at a time to access data.**

If conflicts are high or expensive, **Pessimistic Offline Lock** prevents them by forcing a business transaction to acquire a lock on a piece of data before using it.

### How It Works

  * **Lock Manager:** A custom table (or service) tracking locks (`LockID`, `OwnerID`, `Table/Type`).
  * **Lock Types:**
      * *Exclusive Write:* Only one writer, many readers.
      * *Exclusive Read:* Only one reader or writer.
      * *Read/Write:* Multiple readers, one writer (upgrades to exclusive write).
  * **Protocol:** Acquire lock before loading data. Release lock when the business transaction completes.
  * **Deadlock:** Since this spans multiple requests, waiting for a lock is bad. Fail immediately if a lock isn't available.
  * [cite\_start]**Timeouts:** Implementing a timeout mechanism is mandatory to clean up locks from abandoned sessions [cite: 1256-1260].

### When to Use It

Use when the chance of conflict is high or the cost of a conflict (redoing work) is unacceptable. [cite\_start]Complementary to Optimistic Offline Lock[cite: 1261].

### Example: Simple Lock Manager (Java)

**Lock Manager Interface:**

```java
interface ExclusiveReadLockManager...
    void acquireLock(Long lockable, String owner) throws ConcurrencyException;
    void releaseLock(Long lockable, String owner);
    void releaseAllLocks(String owner);
```

**DB Implementation:**

```java
class ExclusiveReadLockManagerDBImpl...
    public void acquireLock(Long lockable, String owner) throws ConcurrencyException {
        if (!hasLock(lockable, owner)) {
            // INSERT into lock table. Fails if PK exists.
            try {
                // ... INSERT SQL ...
            } catch (SQLException e) {
                throw new ConcurrencyException("Unable to lock " + lockable);
            }
        }
    }
```

[cite\_start]The Controller/Command must start a business transaction (creating an application session ID) and acquire locks before processing [cite: 1261-1267].

-----

## Coarse-Grained Lock

*by David Rice and Matt Foemmel*

**Locks a set of related objects with a single lock.**

Locking individual objects leads to high contention and complexity. [cite\_start]A **Coarse-Grained Lock** covers a set of related objects (an Aggregate) with a single lock[cite: 1268].

### How It Works

  * **Shared Version:** All objects in a group share a single version number (e.g., `Customer` and `Address` share the `Customer`'s version). Updating an address increments the customer's version.
  * **Root Lock:** To lock any object in the group, you navigate to the root (e.g., `Customer`) and lock that. [cite\_start]This requires the ability to navigate from child to parent [cite: 1269-1271].

### When to Use It

[cite\_start]Use to satisfy business requirements (e.g., "Editing a lease locks all assets") or to reduce lock management overhead[cite: 1271].

### Example: Shared Optimistic Offline Lock (Java)

**Shared Version Class:**

```java
class Version...
    public void increment() throws ConcurrencyException {
        if (!isLocked()) {
            // UPDATE version SET value = value + 1 WHERE id = ? AND value = ?
            // Throw exception if row count is 0
        }
    }
```

**Domain Object:**
Uses a shared version object passed down from the root during creation.

```java
class Customer extends DomainObject...
    public Address addAddress(...) {
        // Pass the Customer's version to the new Address
        return Address.create(this, getVersion(), ...); 
    }
```

[cite\_start][cite: 1272-1276]

-----

## Implicit Lock

*by David Rice*

**Allows framework or layer supertype code to acquire offline locks.**

[cite\_start]To prevent developers from forgetting to acquire locks (rendering the scheme useless), **Implicit Lock** moves the locking logic into the framework (Layer Supertypes, Data Mappers, etc.)[cite: 1279].

### How It Works

Identify mandatory locking tasks (e.g., version checks, acquiring locks on load) and hide them in the infrastructure.

  * **Optimistic:** The Data Mapper automatically checks row counts on updates.
  * **Pessimistic:** The Data Mapper automatically calls the Lock Manager inside the `find()` method.

### When to Use It

[cite\_start]Use in almost all applications to enforce the locking strategy reliably[cite: 1281].

### Example: Implicit Pessimistic Offline Lock (Java)

We use a **Decorating Mapper** that wraps the standard mapper and adds locking logic.

```java
class LockingMapper implements Mapper...
    private Mapper impl;
    
    public DomainObject find(Long id) {
        // Implicitly acquire lock before finding
        ExclusiveReadLockManager.INSTANCE.acquireLock(
            id, AppSessionManager.getSession().getId());
        return impl.find(id);
    }
```

[cite\_start]We register this decorating mapper in the Registry so domain code uses it automatically without knowing about the locking [cite: 1281-1283].

# Chapter 17, 18

Based on **Chapter 17: Session State Patterns** and **Chapter 18: Base Patterns** from *Patterns of Enterprise Application Architecture*, here is a comprehensive extraction of the key concepts, patterns, and examples.

-----

# Chapter 17: Session State Patterns

[cite\_start]A fundamental decision in building a web application is where to store the **Session State**—the data that is specific to a user's current interaction (like a shopping cart) and needs to persist between requests but is not yet committed as record data[cite: 1286].

## Client Session State

**Stores session state on the client.**

### How It Works

Even server-oriented designs need a little client state (usually just a Session ID). In this pattern, the *entire* session state is stored on the client. The client sends the full state with every request, and the server sends it back with every response. [cite\_start]This allows the server to remain completely stateless[cite: 1286].

There are three common mechanisms:

1.  **URL Parameters:** Good for tiny amounts of data (like a Session ID). Limited size.
2.  **Hidden Fields:** `<INPUT type="hidden">`. Data is serialized into the field and read back. XML is a common format, though verbose. Security risk: anyone can view source to see the data.
3.  **Cookies:** Sent back and forth automatically. Limited size. [cite\_start]Security risk: users can turn them off or tamper with them [cite: 1286-1287].

### When to Use It

  * **Pros:** Supports stateless server objects, enabling maximal clustering and failover resiliency. [cite\_start]If the server crashes, the state is safe on the client[cite: 1287].
  * **Cons:** Costly if data volume is large (bandwidth usage increases exponentially). Security risks (data can be inspected or altered by the user). [cite\_start]Requires encryption for sensitive data[cite: 1287].

-----

## Server Session State

**Keeps the session state on a server system in a serialized form.**

### How It Works

In the simplest form, a session object is held in memory (e.g., a Map) on the application server, keyed by a Session ID.

  * [cite\_start]**Resource Management:** To save memory, session objects can be serialized (passivated) to a memento (binary or text/XML) and stored on a local disk or shared server [cite: 1288-1289].
  * **Clustering:** Storing state on a specific server creates **Server Affinity** (Sticky Sessions). [cite\_start]To support failover, the state must be accessible to other servers, often leading to storing serialized state in a shared database (crossing the line into Database Session State)[cite: 1289].

**Java Implementation:**

  * `HttpSession`: Standard interface. Can be backed by memory or database.
  * **Stateful Session Beans:** EJB container handles persistence.
  * [cite\_start]**Entity Beans:** Storing a **Serialized LOB (272)** of session data[cite: 1290].

**.NET Implementation:**

  * [cite\_start]Has built-in support for switching between "In-Process" (Memory) and a "State Service" (Separate process/machine) via configuration[cite: 1290].

### When to Use It

  * **Pros:** Simplicity. Often requires no programming (handled by the app server).
  * [cite\_start]**Cons:** Complexity arises when handling clustering and failover manually[cite: 1291].

-----

## Database Session State

**Stores session data as committed data in the database.**

### How It Works

The server pulls data from the database upon every request using the Session ID (and potentially keys for the specific business transaction).

  * **Separating Session Data:** You must distinguish "pending" session data from "record" (committed) data.
    1.  **Pending Field:** Add an `isPending` or `sessionID` column to tables. [cite\_start]Invasive, requires filtering all queries[cite: 1292].
    2.  **Pending Tables:** Separate tables for pending data (e.g., `PendingOrders`). When the session commits, data is moved to the real tables.
  * [cite\_start]**Cleanup:** Requires a daemon to delete old session rows (e.g., sessions older than 20 minutes) since users rarely log out explicitly[cite: 1293].

### When to Use It

  * **Pros:** Reliability. Session data survives server crashes. Easier to support clustering/failover than Server Session State.
  * **Cons:** Performance. Every request incurs database read/write costs.
  * [cite\_start]**Trade-off:** Use if you have strict failover requirements or massive data per session that exceeds memory limits[cite: 1294].

-----

# Chapter 18: Base Patterns

## Gateway

**An object that encapsulates access to an external system or resource.**

### How It Works

This is a simple wrapper pattern. It wraps specific, often complex APIs (like JDBC, XML parsing, or CICS transactions) into a class with an interface designed for the application's specific needs.

  * [cite\_start]**Service Stub:** A key use of Gateway is providing a point to swap in a **Service Stub (504)** for testing [cite: 1296-1297].
  * [cite\_start]**Code Generation:** Gateways are often good candidates for code generation (e.g., generating a class based on an XML schema)[cite: 1297].

### When to Use It

Use it whenever you have an awkward interface to an external resource. [cite\_start]It simplifies client code and makes the system easier to test and adaptable to resource changes [cite: 1297-1298].

### Example: A Gateway to a Proprietary Messaging Service (Java)

A messaging system has a generic interface: `int send(String type, Object[] args)`.
The Gateway wraps this with a clean, typed interface.

```java
class MessageGateway...
    public void sendConfirmation(String orderID, int amount, String symbol) {
        Object[] args = new Object[]{orderID, new Integer(amount), symbol};
        send("CNFRM", args);
    }
    
    // Translates return codes to Exceptions
    private void send(String msg, Object[] args) {
        int returnCode = doSend(msg, args);
        if (returnCode == MessageSender.NULL_PARAMETER)
            throw new NullPointerException("Null Parameter");
        if (returnCode != MessageSender.SUCCESS)
            throw new IllegalStateException("Error #" + returnCode);
    }
```

[cite\_start][cite: 1299-1300]

-----

## Mapper

**An object that sets up a communication between two independent objects.**

### How It Works

A Mapper acts as an insulating layer that shuffles data between two subsystems (e.g., Domain Objects and Database) so neither knows about the other. [cite\_start]It is most commonly seen in **Data Mapper (165)**[cite: 1303].

### When to Use It

Use it when you need to ensure complete decoupling between subsystems. [cite\_start]If simplicity is preferred, **Gateway (466)** is usually a better choice[cite: 1304].

-----

## Layer Supertype

**A type that acts as the supertype for all types in its layer.**

### How It Works

[cite\_start]Create a common superclass (e.g., `DomainObject`) for all objects in a layer to hold common behavior, such as Identity Field handling[cite: 1305].

### Example: Domain Object (Java)

```java
class DomainObject...
    private Long ID;
    public Long getID() { return ID; }
    public void setID(Long ID) { this.ID = ID; }
```

[cite\_start][cite: 1305]

-----

## Separated Interface

**Defines an interface in a separate package from its implementation.**

### How It Works

You place the interface in the client's package (or a third common package) and the implementation in a separate package. The client depends on the interface, not the implementation.

  * **Dependency Inversion:** This breaks dependency cycles (e.g., Domain needs Data Mapper, but Data Mapper needs Domain).
  * [cite\_start]**Instantiation:** Uses a factory or **Plugin (499)** to link the implementation at runtime [cite: 1306-1308].

### When to Use It

[cite\_start]Use it to break dependencies between parts of the system, or when you need multiple independent implementations (e.g., a Test Stub and a Real Service)[cite: 1308].

-----

## Registry

**A well-known object that other objects can use to find common objects and services.**

### How It Works

A Registry is essentially a global object.

  * **Interface:** Usually static methods (e.g., `Registry.personFinder()`).
  * **Scope:**
      * **Process-Scoped:** Global to the JVM. Often a Singleton.
      * **Thread-Scoped:** Unique per thread. Uses `ThreadLocal`. [cite\_start]Good for Database Connections or Unit of Work [cite: 1310-1311].

### When to Use It

Use as a last resort to avoid passing parameters around everywhere. It is global data, which should be treated with suspicion. [cite\_start]Useful for locating finder objects or current sessions[cite: 1312].

-----

## Value Object

**A small simple object, like money or a date range, whose equality isn’t based on identity.**

### How It Works

  * **Equality:** Two Value Objects are equal if their fields are equal (e.g., two `Date` objects representing "Jan 1"). Reference objects are equal only if they are the same object in memory.
  * [cite\_start]**Immutability:** Value Objects should be immutable to avoid aliasing bugs (where changing a date in one object accidentally changes it in another that shares the reference)[cite: 1316].

### When to Use It

Use for small concepts like Money, Date, or Address. [cite\_start]They should usually be persisted using **Embedded Value (268)**[cite: 1317].

-----

## Money

**Represents a monetary value.**

### How It Works

A class with fields for `amount` and `currency`.

  * **Math:** encapsulates arithmetic. `money.add(money)` checks currencies match.
  * **Rounding:** Handling allocation without losing pennies (e.g., splitting $0.05 three ways). [cite\_start]Using an `allocate` method that returns an array of monies is preferred over simple division [cite: 1318-1320].

### Example: A Money Class (Java)

```java
class Money...
    private long amount; // Integral amount (cents)
    private Currency currency;
    
    public Money add(Money other) {
        assertSameCurrencyAs(other);
        return newMoney(amount + other.amount);
    }
    
    // Allocation without losing cents
    public Money[] allocate(int n) {
        Money lowResult = newMoney(amount / n);
        Money highResult = newMoney(lowResult.amount + 1);
        Money[] results = new Money[n];
        int remainder = (int) amount % n;
        for (int i = 0; i < remainder; i++) results[i] = highResult;
        for (int i = remainder; i < n; i++) results[i] = lowResult;
        return results;
    }
```

[cite\_start][cite: 1321-1324]

-----

## Special Case

**A subclass that provides special behavior for particular cases.**

### How It Works

Instead of returning `null` (which requires checks everywhere), return a specific subclass like `UnknownCustomer` or `MissingProduct` that implements harmless behavior.

  * [cite\_start]**Null Object:** A specific type of Special Case where the object does nothing [cite: 1326-1327].

### Example: A Simple Null Object (C\#)

```csharp
class NullEmployee : Employee, INull...
    public override String Name {
        get {return "Null Employee";}
    }
    public override Decimal GrossToDate {
        get {return 0m;} // Returns safe default values
    }
```

[cite\_start][cite: 1328]

-----

## Plugin

**Links classes during configuration rather than compilation.**

### How It Works

Uses a **Separated Interface (476)**. The implementation class is not known at compile time.

  * **Configuration:** A text file maps interfaces to implementation class names.
  * [cite\_start]**Factory:** Reads the config file, loads the class (using Reflection), and instantiates it [cite: 1329-1330].

### When to Use It

[cite\_start]When behavior differs based on runtime environment (e.g., using a Test ID Generator vs an Oracle ID Generator)[cite: 1330].

### Example: An Id Generator (Java)

```java
// Factory reads properties file to find impl class name
class PluginFactory...
    public static Object getPlugin(Class iface) {
        String implName = props.getProperty(iface.getName());
        return Class.forName(implName).newInstance();
    }
```

[cite\_start][cite: 1331-1332]

-----

## Service Stub

**Removes dependence upon problematic services during testing.**

### How It Works

Replaces a real external service (accessed via a **Gateway (466)**) with a lightweight, in-memory implementation for testing.

  * [cite\_start]**Implementation:** Can be a simple hardcoded return, or a dynamic object that allows tests to setup return values [cite: 1334-1335].

### When to Use It

[cite\_start]To speed up testing and remove dependencies on third-party services, credit checks, or slow resources[cite: 1335].

-----

## Record Set

**An in-memory representation of tabular data.**

### How It Works

A structure that looks exactly like the result of a SQL query (rows and columns) but is disconnected from the database.

  * **Examples:** ADO.NET DataSet, JDBC RowSet.
  * [cite\_start]**Usage:** Can be passed to the UI layer (which often has tools to display them easily) or manipulated by **Table Modules (125)** [cite: 1338-1339].

### When to Use It

Valuable when using environments (like .NET) that provide strong tool support for tabular data in the UI. [cite\_start]Often used as a **Data Transfer Object (401)**[cite: 1340].