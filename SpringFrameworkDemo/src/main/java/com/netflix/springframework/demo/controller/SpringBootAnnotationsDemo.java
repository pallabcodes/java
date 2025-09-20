package com.netflix.springframework.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.Valid;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * SpringBootAnnotationsDemo - Comprehensive Spring Boot Web Annotations Demo
 * 
 * This controller demonstrates all major Spring Boot web annotations
 * with detailed explanations for C/C++ engineers.
 * 
 * For C/C++ engineers:
 * - These annotations are like decorators or attributes in C++
 * - They provide metadata about how methods should be handled
 * - Similar to function attributes or decorators in modern C++
 * - They configure web server behavior automatically
 * 
 * @author Netflix SDE-2 Team
 */
@RestController
@RequestMapping("/api/v1/annotations")
@CrossOrigin(origins = "*")
@SessionAttributes("user")
public class SpringBootAnnotationsDemo {
    
    /**
     * @RestController - Marks this class as a REST controller
     * 
     * For C/C++ engineers:
     * - This is like marking a class as a web service handler
     * - Similar to marking a class as a controller in web frameworks
     * - Automatically handles JSON serialization/deserialization
     * - Combines @Controller and @ResponseBody
     */
    
    /**
     * @RequestMapping - Base URL mapping for all methods in this controller
     * 
     * For C/C++ engineers:
     * - This is like URL routing in web frameworks
     * - Similar to route prefixes in web servers
     * - All methods in this class will be prefixed with /api/v1/annotations
     */
    
    /**
     * @CrossOrigin - Enables CORS (Cross-Origin Resource Sharing)
     * 
     * For C/C++ engineers:
     * - This allows requests from different domains
     * - Similar to CORS configuration in web servers
     * - Enables frontend-backend communication
     */
    
    /**
     * @SessionAttributes - Declares session attributes
     * 
     * For C/C++ engineers:
     * - This is like session management in web applications
     * - Similar to session variables in web frameworks
     * - Stores data across multiple requests
     */
    
    /**
     * @GetMapping - Maps GET requests to this method
     * 
     * For C/C++ engineers:
     * - This is like a GET endpoint handler
     * - Similar to handling GET requests in web frameworks
     * - URL: GET /api/v1/annotations/get-example
     */
    @GetMapping("/get-example")
    public ResponseEntity<Map<String, Object>> getExample() {
        System.out.println("GET /api/v1/annotations/get-example called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET");
        response.put("message", "This is a GET request example");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * @PostMapping - Maps POST requests to this method
     * 
     * For C/C++ engineers:
     * - This is like a POST endpoint handler
     * - Similar to handling POST requests in web frameworks
     * - URL: POST /api/v1/annotations/post-example
     */
    @PostMapping("/post-example")
    public ResponseEntity<Map<String, Object>> postExample(@RequestBody Map<String, Object> requestBody) {
        System.out.println("POST /api/v1/annotations/post-example called");
        System.out.println("Request body: " + requestBody);
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "POST");
        response.put("message", "This is a POST request example");
        response.put("receivedData", requestBody);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * @PutMapping - Maps PUT requests to this method
     * 
     * For C/C++ engineers:
     * - This is like a PUT endpoint handler
     * - Similar to handling PUT requests in web frameworks
     * - URL: PUT /api/v1/annotations/put-example
     */
    @PutMapping("/put-example")
    public ResponseEntity<Map<String, Object>> putExample(@RequestBody Map<String, Object> requestBody) {
        System.out.println("PUT /api/v1/annotations/put-example called");
        System.out.println("Request body: " + requestBody);
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "PUT");
        response.put("message", "This is a PUT request example");
        response.put("receivedData", requestBody);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * @DeleteMapping - Maps DELETE requests to this method
     * 
     * For C/C++ engineers:
     * - This is like a DELETE endpoint handler
     * - Similar to handling DELETE requests in web frameworks
     * - URL: DELETE /api/v1/annotations/delete-example
     */
    @DeleteMapping("/delete-example")
    public ResponseEntity<Map<String, Object>> deleteExample() {
        System.out.println("DELETE /api/v1/annotations/delete-example called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "DELETE");
        response.put("message", "This is a DELETE request example");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
    
    /**
     * @PathVariable - Extracts path variables from URL
     * 
     * For C/C++ engineers:
     * - This extracts variables from the URL path
     * - Similar to URL parameter extraction in web frameworks
     * - URL: GET /api/v1/annotations/path-variable/{id}
     */
    @GetMapping("/path-variable/{id}")
    public ResponseEntity<Map<String, Object>> pathVariableExample(@PathVariable Long id) {
        System.out.println("GET /api/v1/annotations/path-variable/" + id + " called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET with PathVariable");
        response.put("pathVariable", id);
        response.put("message", "Path variable extracted: " + id);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * @RequestParam - Extracts query parameters from URL
     * 
     * For C/C++ engineers:
     * - This extracts query parameters from URL
     * - Similar to query string parsing in web frameworks
     * - URL: GET /api/v1/annotations/request-param?name=value&age=25
     */
    @GetMapping("/request-param")
    public ResponseEntity<Map<String, Object>> requestParamExample(
            @RequestParam String name,
            @RequestParam(required = false) Integer age,
            @RequestParam(defaultValue = "unknown") String city) {
        System.out.println("GET /api/v1/annotations/request-param called");
        System.out.println("Name: " + name + ", Age: " + age + ", City: " + city);
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET with RequestParam");
        response.put("name", name);
        response.put("age", age);
        response.put("city", city);
        response.put("message", "Query parameters extracted successfully");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * @RequestBody - Extracts request body as Java object
     * 
     * For C/C++ engineers:
     * - This deserializes JSON request body to Java object
     * - Similar to JSON parsing in C++ libraries
     * - URL: POST /api/v1/annotations/request-body
     */
    @PostMapping("/request-body")
    public ResponseEntity<Map<String, Object>> requestBodyExample(@RequestBody Map<String, Object> requestBody) {
        System.out.println("POST /api/v1/annotations/request-body called");
        System.out.println("Request body: " + requestBody);
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "POST with RequestBody");
        response.put("receivedData", requestBody);
        response.put("message", "Request body deserialized successfully");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * @RequestHeader - Extracts HTTP headers
     * 
     * For C/C++ engineers:
     * - This extracts HTTP headers from request
     * - Similar to header parsing in web frameworks
     * - URL: GET /api/v1/annotations/request-header
     */
    @GetMapping("/request-header")
    public ResponseEntity<Map<String, Object>> requestHeaderExample(
            @RequestHeader("User-Agent") String userAgent,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        System.out.println("GET /api/v1/annotations/request-header called");
        System.out.println("User-Agent: " + userAgent);
        System.out.println("Authorization: " + authorization);
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET with RequestHeader");
        response.put("userAgent", userAgent);
        response.put("authorization", authorization);
        response.put("message", "HTTP headers extracted successfully");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * @CookieValue - Extracts cookie values
     * 
     * For C/C++ engineers:
     * - This extracts cookie values from request
     * - Similar to cookie parsing in web frameworks
     * - URL: GET /api/v1/annotations/cookie-value
     */
    @GetMapping("/cookie-value")
    public ResponseEntity<Map<String, Object>> cookieValueExample(
            @CookieValue(value = "sessionId", required = false) String sessionId) {
        System.out.println("GET /api/v1/annotations/cookie-value called");
        System.out.println("Session ID: " + sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET with CookieValue");
        response.put("sessionId", sessionId);
        response.put("message", "Cookie value extracted successfully");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * @ResponseStatus - Sets HTTP status code
     * 
     * For C/C++ engineers:
     * - This sets the HTTP response status code
     * - Similar to setting response status in web frameworks
     * - URL: GET /api/v1/annotations/response-status
     */
    @GetMapping("/response-status")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> responseStatusExample() {
        System.out.println("GET /api/v1/annotations/response-status called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET with ResponseStatus");
        response.put("status", "202 Accepted");
        response.put("message", "Response status set to 202 Accepted");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * @ModelAttribute - Adds model attributes
     * 
     * For C/C++ engineers:
     * - This adds data to the model for view rendering
     * - Similar to adding data to view context in web frameworks
     * - URL: GET /api/v1/annotations/model-attribute
     */
    @GetMapping("/model-attribute")
    public ResponseEntity<Map<String, Object>> modelAttributeExample(@ModelAttribute("user") Map<String, Object> user) {
        System.out.println("GET /api/v1/annotations/model-attribute called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET with ModelAttribute");
        response.put("user", user);
        response.put("message", "Model attribute accessed successfully");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * @InitBinder - Initializes data binding
     * 
     * For C/C++ engineers:
     * - This initializes data binding for form data
     * - Similar to form data binding in web frameworks
     * - URL: POST /api/v1/annotations/init-binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        System.out.println("InitBinder called for data binding initialization");
        // Configure data binding here
    }
    
    /**
     * @ExceptionHandler - Handles exceptions
     * 
     * For C/C++ engineers:
     * - This handles exceptions thrown by controller methods
     * - Similar to exception handling in web frameworks
     * - Provides centralized error handling
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        System.out.println("Exception handled: " + e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", "An error occurred: " + e.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * @ResponseBody - Indicates return value should be serialized
     * 
     * For C/C++ engineers:
     * - This indicates the return value should be serialized to response body
     * - Similar to JSON serialization in web frameworks
     * - URL: GET /api/v1/annotations/response-body
     */
    @GetMapping("/response-body")
    @ResponseBody
    public Map<String, Object> responseBodyExample() {
        System.out.println("GET /api/v1/annotations/response-body called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET with ResponseBody");
        response.put("message", "Response body serialized automatically");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * @RequestPart - Handles multipart form data
     * 
     * For C/C++ engineers:
     * - This handles file uploads and multipart form data
     * - Similar to file upload handling in web frameworks
     * - URL: POST /api/v1/annotations/request-part
     */
    @PostMapping(value = "/request-part", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> requestPartExample(
            @RequestPart("file") MultipartFile file,
            @RequestPart("data") String data) {
        System.out.println("POST /api/v1/annotations/request-part called");
        System.out.println("File: " + file.getOriginalFilename() + ", Size: " + file.getSize());
        System.out.println("Data: " + data);
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", "POST with RequestPart");
        response.put("fileName", file.getOriginalFilename());
        response.put("fileSize", file.getSize());
        response.put("data", data);
        response.put("message", "Multipart form data processed successfully");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * @RequestMapping with multiple HTTP methods
     * 
     * For C/C++ engineers:
     * - This maps multiple HTTP methods to the same method
     * - Similar to handling multiple HTTP methods in web frameworks
     * - URL: GET/POST /api/v1/annotations/multiple-methods
     */
    @RequestMapping(value = "/multiple-methods", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> multipleMethodsExample(HttpServletRequest request) {
        System.out.println("Multiple methods endpoint called with: " + request.getMethod());
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", request.getMethod());
        response.put("message", "This endpoint handles both GET and POST requests");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
