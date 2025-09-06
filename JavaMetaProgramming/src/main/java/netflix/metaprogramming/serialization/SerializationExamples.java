package netflix.metaprogramming.serialization;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netflix Production-Grade Serialization Examples
 * 
 * This class demonstrates comprehensive serialization concepts including:
 * - Java Native Serialization
 * - JSON Serialization with Jackson
 * - Custom Serializers and Deserializers
 * - Polymorphic Serialization
 * - Versioning and Migration
 * - Performance Optimization and Caching
 * - Security Considerations and Best Practices
 * - Netflix-specific Serialization Patterns
 * - Error Handling and Exception Management
 * - Integration with Spring Framework
 * 
 * @author Netflix Java Meta Programming Team
 * @version 1.0.0
 * @since 2024
 */
@Slf4j
@Component
public class SerializationExamples {

    private final ObjectMapper objectMapper;
    private final Map<String, Object> serializationCache = new ConcurrentHashMap<>();

    public SerializationExamples() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Demonstrates Java Native Serialization
     * 
     * Shows how to use Java's built-in serialization mechanism.
     */
    public void demonstrateJavaNativeSerialization() {
        log.info("=== Demonstrating Java Native Serialization ===");
        
        try {
            // Create a serializable object
            SerializableObject obj = new SerializableObject("John Doe", 30, LocalDateTime.now());
            log.debug("Original object: {}", obj);
            
            // Serialize to byte array
            byte[] serializedData = serializeToBytes(obj);
            log.debug("Serialized data size: {} bytes", serializedData.length);
            
            // Deserialize from byte array
            SerializableObject deserializedObj = deserializeFromBytes(serializedData);
            log.debug("Deserialized object: {}", deserializedObj);
            
            // Verify objects are equal
            boolean isEqual = obj.equals(deserializedObj);
            log.debug("Objects are equal: {}", isEqual);
            
        } catch (Exception e) {
            log.error("Error in Java native serialization demonstration", e);
        }
    }

    /**
     * Demonstrates JSON Serialization with Jackson
     * 
     * Shows how to use Jackson for JSON serialization and deserialization.
     */
    public void demonstrateJsonSerialization() {
        log.info("=== Demonstrating JSON Serialization ===");
        
        try {
            // Create a JSON serializable object
            JsonSerializableObject obj = new JsonSerializableObject("Alice", 25, LocalDateTime.now());
            log.debug("Original object: {}", obj);
            
            // Serialize to JSON
            String json = serializeToJson(obj);
            log.debug("JSON: {}", json);
            
            // Deserialize from JSON
            JsonSerializableObject deserializedObj = deserializeFromJson(json, JsonSerializableObject.class);
            log.debug("Deserialized object: {}", deserializedObj);
            
            // Verify objects are equal
            boolean isEqual = obj.equals(deserializedObj);
            log.debug("Objects are equal: {}", isEqual);
            
        } catch (Exception e) {
            log.error("Error in JSON serialization demonstration", e);
        }
    }

    /**
     * Demonstrates Custom Serializers and Deserializers
     * 
     * Shows how to create custom serializers and deserializers.
     */
    public void demonstrateCustomSerializers() {
        log.info("=== Demonstrating Custom Serializers ===");
        
        try {
            // Create object with custom serialization
            CustomSerializableObject obj = new CustomSerializableObject("Bob", 35, LocalDateTime.now());
            log.debug("Original object: {}", obj);
            
            // Serialize with custom serializer
            String json = serializeWithCustomSerializer(obj);
            log.debug("Custom JSON: {}", json);
            
            // Deserialize with custom deserializer
            CustomSerializableObject deserializedObj = deserializeWithCustomDeserializer(json);
            log.debug("Deserialized object: {}", deserializedObj);
            
        } catch (Exception e) {
            log.error("Error in custom serializers demonstration", e);
        }
    }

    /**
     * Demonstrates Polymorphic Serialization
     * 
     * Shows how to handle polymorphic serialization with type information.
     */
    public void demonstratePolymorphicSerialization() {
        log.info("=== Demonstrating Polymorphic Serialization ===");
        
        try {
            // Create polymorphic objects
            List<Animal> animals = Arrays.asList(
                new Dog("Rex", 3, "Golden Retriever"),
                new Cat("Whiskers", 2, "Persian"),
                new Bird("Tweety", 1, "Canary")
            );
            
            log.debug("Original animals: {}", animals);
            
            // Serialize polymorphic objects
            String json = serializePolymorphic(animals);
            log.debug("Polymorphic JSON: {}", json);
            
            // Deserialize polymorphic objects
            List<Animal> deserializedAnimals = deserializePolymorphic(json);
            log.debug("Deserialized animals: {}", deserializedAnimals);
            
        } catch (Exception e) {
            log.error("Error in polymorphic serialization demonstration", e);
        }
    }

    /**
     * Demonstrates Versioning and Migration
     * 
     * Shows how to handle versioning and migration in serialization.
     */
    public void demonstrateVersioningAndMigration() {
        log.info("=== Demonstrating Versioning and Migration ===");
        
        try {
            // Create versioned object
            VersionedObject obj = new VersionedObject("Test", 1, "v1.0");
            log.debug("Original object: {}", obj);
            
            // Serialize with version
            String json = serializeWithVersion(obj);
            log.debug("Versioned JSON: {}", json);
            
            // Deserialize with version handling
            VersionedObject deserializedObj = deserializeWithVersion(json);
            log.debug("Deserialized object: {}", deserializedObj);
            
        } catch (Exception e) {
            log.error("Error in versioning demonstration", e);
        }
    }

    /**
     * Demonstrates Performance Optimization and Caching
     * 
     * Shows how to optimize serialization performance using caching.
     */
    @Cacheable(value = "serialization-cache", key = "#obj.getClass().name + ':' + #obj.hashCode()")
    public String getCachedSerialization(Object obj) {
        log.debug("Serializing object with caching: {}", obj.getClass().getSimpleName());
        
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object", e);
            return null;
        }
    }

    /**
     * Demonstrates Security Considerations and Best Practices
     * 
     * Shows how to handle security in serialization.
     */
    public void demonstrateSecurityConsiderations() {
        log.info("=== Demonstrating Security Considerations ===");
        
        try {
            // Validate serialized data
            String json = "{\"name\":\"Test\",\"age\":30}";
            boolean isValid = validateSerializedData(json);
            log.debug("Serialized data is valid: {}", isValid);
            
            // Check for malicious content
            String maliciousJson = "{\"name\":\"<script>alert('xss')</script>\",\"age\":30}";
            boolean isMalicious = checkForMaliciousContent(maliciousJson);
            log.debug("Contains malicious content: {}", isMalicious);
            
            // Sanitize serialized data
            String sanitizedJson = sanitizeSerializedData(maliciousJson);
            log.debug("Sanitized JSON: {}", sanitizedJson);
            
        } catch (Exception e) {
            log.error("Error in security considerations demonstration", e);
        }
    }

    /**
     * Demonstrates Netflix-specific Serialization Patterns
     * 
     * Shows how to implement Netflix-specific serialization patterns.
     */
    public void demonstrateNetflixSerializationPatterns() {
        log.info("=== Demonstrating Netflix Serialization Patterns ===");
        
        try {
            // Create Netflix-specific object
            NetflixObject obj = new NetflixObject("Netflix Service", "v1.0", LocalDateTime.now());
            log.debug("Original Netflix object: {}", obj);
            
            // Serialize with Netflix patterns
            String json = serializeWithNetflixPatterns(obj);
            log.debug("Netflix JSON: {}", json);
            
            // Deserialize with Netflix patterns
            NetflixObject deserializedObj = deserializeWithNetflixPatterns(json);
            log.debug("Deserialized Netflix object: {}", deserializedObj);
            
        } catch (Exception e) {
            log.error("Error in Netflix serialization patterns demonstration", e);
        }
    }

    /**
     * Demonstrates Error Handling and Exception Management
     * 
     * Shows how to handle errors in serialization.
     */
    public void demonstrateErrorHandling() {
        log.info("=== Demonstrating Error Handling ===");
        
        try {
            // Test with invalid JSON
            String invalidJson = "{\"name\":\"Test\",\"age\":\"invalid\"}";
            try {
                deserializeFromJson(invalidJson, JsonSerializableObject.class);
            } catch (Exception e) {
                log.debug("Caught expected exception: {}", e.getMessage());
            }
            
            // Test with missing fields
            String incompleteJson = "{\"name\":\"Test\"}";
            try {
                deserializeFromJson(incompleteJson, JsonSerializableObject.class);
            } catch (Exception e) {
                log.debug("Caught expected exception: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error in error handling demonstration", e);
        }
    }

    // Helper methods

    private byte[] serializeToBytes(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    private <T extends Serializable> T deserializeFromBytes(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        }
    }

    private String serializeToJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    private <T> T deserializeFromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    private String serializeWithCustomSerializer(Object obj) throws JsonProcessingException {
        // This would use custom serializers in a real implementation
        return objectMapper.writeValueAsString(obj);
    }

    private CustomSerializableObject deserializeWithCustomDeserializer(String json) throws JsonProcessingException {
        // This would use custom deserializers in a real implementation
        return objectMapper.readValue(json, CustomSerializableObject.class);
    }

    private String serializePolymorphic(List<Animal> animals) throws JsonProcessingException {
        return objectMapper.writeValueAsString(animals);
    }

    private List<Animal> deserializePolymorphic(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Animal.class));
    }

    private String serializeWithVersion(VersionedObject obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    private VersionedObject deserializeWithVersion(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, VersionedObject.class);
    }

    private boolean validateSerializedData(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    private boolean checkForMaliciousContent(String json) {
        // Simple check for script tags - in real implementation, use proper sanitization
        return json.contains("<script>") || json.contains("javascript:");
    }

    private String sanitizeSerializedData(String json) {
        // Simple sanitization - in real implementation, use proper sanitization library
        return json.replaceAll("<script>.*?</script>", "")
                  .replaceAll("javascript:", "");
    }

    private String serializeWithNetflixPatterns(NetflixObject obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    private NetflixObject deserializeWithNetflixPatterns(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, NetflixObject.class);
    }

    // Sample classes for demonstration

    public static class SerializableObject implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String name;
        private int age;
        private LocalDateTime timestamp;
        
        public SerializableObject() {}
        
        public SerializableObject(String name, int age, LocalDateTime timestamp) {
            this.name = name;
            this.age = age;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SerializableObject that = (SerializableObject) o;
            return age == that.age && Objects.equals(name, that.name) && Objects.equals(timestamp, that.timestamp);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, age, timestamp);
        }
        
        @Override
        public String toString() {
            return "SerializableObject{name='" + name + "', age=" + age + ", timestamp=" + timestamp + "}";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JsonSerializableObject {
        private String name;
        private int age;
        private LocalDateTime timestamp;
        
        public JsonSerializableObject() {}
        
        public JsonSerializableObject(String name, int age, LocalDateTime timestamp) {
            this.name = name;
            this.age = age;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonSerializableObject that = (JsonSerializableObject) o;
            return age == that.age && Objects.equals(name, that.name) && Objects.equals(timestamp, that.timestamp);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, age, timestamp);
        }
        
        @Override
        public String toString() {
            return "JsonSerializableObject{name='" + name + "', age=" + age + ", timestamp=" + timestamp + "}";
        }
    }

    @JsonSerialize(using = CustomSerializer.class)
    @JsonDeserialize(using = CustomDeserializer.class)
    public static class CustomSerializableObject {
        private String name;
        private int age;
        private LocalDateTime timestamp;
        
        public CustomSerializableObject() {}
        
        public CustomSerializableObject(String name, int age, LocalDateTime timestamp) {
            this.name = name;
            this.age = age;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        @Override
        public String toString() {
            return "CustomSerializableObject{name='" + name + "', age=" + age + ", timestamp=" + timestamp + "}";
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Dog.class, name = "dog"),
        @JsonSubTypes.Type(value = Cat.class, name = "cat"),
        @JsonSubTypes.Type(value = Bird.class, name = "bird")
    })
    public static abstract class Animal {
        protected String name;
        protected int age;
        
        public Animal() {}
        
        public Animal(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        public abstract String makeSound();
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + "{name='" + name + "', age=" + age + "}";
        }
    }

    public static class Dog extends Animal {
        private String breed;
        
        public Dog() {}
        
        public Dog(String name, int age, String breed) {
            super(name, age);
            this.breed = breed;
        }
        
        public String getBreed() { return breed; }
        public void setBreed(String breed) { this.breed = breed; }
        
        @Override
        public String makeSound() {
            return "Woof!";
        }
    }

    public static class Cat extends Animal {
        private String breed;
        
        public Cat() {}
        
        public Cat(String name, int age, String breed) {
            super(name, age);
            this.breed = breed;
        }
        
        public String getBreed() { return breed; }
        public void setBreed(String breed) { this.breed = breed; }
        
        @Override
        public String makeSound() {
            return "Meow!";
        }
    }

    public static class Bird extends Animal {
        private String species;
        
        public Bird() {}
        
        public Bird(String name, int age, String species) {
            super(name, age);
            this.species = species;
        }
        
        public String getSpecies() { return species; }
        public void setSpecies(String species) { this.species = species; }
        
        @Override
        public String makeSound() {
            return "Tweet!";
        }
    }

    public static class VersionedObject {
        private String name;
        private int version;
        private String versionString;
        
        public VersionedObject() {}
        
        public VersionedObject(String name, int version, String versionString) {
            this.name = name;
            this.version = version;
            this.versionString = versionString;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getVersion() { return version; }
        public void setVersion(int version) { this.version = version; }
        public String getVersionString() { return versionString; }
        public void setVersionString(String versionString) { this.versionString = versionString; }
        
        @Override
        public String toString() {
            return "VersionedObject{name='" + name + "', version=" + version + ", versionString='" + versionString + "'}";
        }
    }

    public static class NetflixObject {
        private String serviceName;
        private String version;
        private LocalDateTime timestamp;
        
        public NetflixObject() {}
        
        public NetflixObject(String serviceName, String version, LocalDateTime timestamp) {
            this.serviceName = serviceName;
            this.version = version;
            this.timestamp = timestamp;
        }
        
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        @Override
        public String toString() {
            return "NetflixObject{serviceName='" + serviceName + "', version='" + version + "', timestamp=" + timestamp + "}";
        }
    }

    // Custom serializer and deserializer
    public static class CustomSerializer extends JsonSerializer<CustomSerializableObject> {
        @Override
        public void serialize(CustomSerializableObject value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("custom_name", value.getName());
            gen.writeNumberField("custom_age", value.getAge());
            gen.writeStringField("custom_timestamp", value.getTimestamp().toString());
            gen.writeEndObject();
        }
    }

    public static class CustomDeserializer extends JsonDeserializer<CustomSerializableObject> {
        @Override
        public CustomSerializableObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            String name = node.get("custom_name").asText();
            int age = node.get("custom_age").asInt();
            LocalDateTime timestamp = LocalDateTime.parse(node.get("custom_timestamp").asText());
            return new CustomSerializableObject(name, age, timestamp);
        }
    }
}
