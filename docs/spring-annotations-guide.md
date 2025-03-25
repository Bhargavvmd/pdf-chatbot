# Spring Annotations Guide

## Core Spring Framework Annotations

### Component Annotations
1. `@Component`
   - Base annotation for any Spring-managed component
   - Parent of @Service, @Repository, and @Controller
   - Usage: `@Component public class GenericComponent {}`

2. `@Service`
   - Marks a class as service layer component
   - Used in: PdfProcessingService, ChatService
   - Usage: `@Service public class MyService {}`

3. `@Repository`
   - Marks a class as data access layer component
   - Translates persistence exceptions
   - Usage: `@Repository public class UserRepository {}`

4. `@Controller`
   - Marks a class as Spring MVC controller
   - Usage: `@Controller public class ViewController {}`

5. `@RestController`
   - Combines @Controller and @ResponseBody
   - Used in: PdfController, ChatController
   - Usage: `@RestController public class ApiController {}`

### Configuration Annotations
1. `@Configuration`
   - Marks a class as source of bean definitions
   - Used in: WebConfig
   - Usage: `@Configuration public class AppConfig {}`

2. `@Bean`
   - Declares a method as a bean producer
   - Used in: AsyncConfig (for executors)
   - Usage: 
   ```java
   @Bean
   public MyService myService() {
       return new MyService();
   }
   ```

3. `@ComponentScan`
   - Configures component scanning directives
   - Part of @SpringBootApplication
   - Usage: `@ComponentScan(basePackages = "com.chatbot")`

4. `@EnableAsync`
   - Enables Spring's asynchronous method execution
   - Used in: DemoApplication
   - Usage: `@EnableAsync public class AsyncConfig {}`

### Dependency Injection
1. `@Autowired`
   - Injects dependencies automatically
   - Can be replaced by constructor injection with @RequiredArgsConstructor
   - Usage: `@Autowired private MyService myService;`

2. `@Qualifier`
   - Specifies which bean to inject when multiple exist
   - Usage: `@Autowired @Qualifier("specificBean") private MyService myService;`

3. `@Value`
   - Injects values from properties files
   - Usage: `@Value("${server.port}") private int serverPort;`

### Request Handling
1. `@RequestMapping`
   - Maps web requests to handler methods
   - Used in: PdfController, ChatController
   - Usage: `@RequestMapping("/api/pdf")`

2. `@GetMapping`
   - Shortcut for @RequestMapping(method = GET)
   - Usage: `@GetMapping("/status")`

3. `@PostMapping`
   - Shortcut for @RequestMapping(method = POST)
   - Used in: PdfController ("/upload")
   - Usage: `@PostMapping("/upload")`

4. `@PutMapping`
   - Shortcut for @RequestMapping(method = PUT)
   - Usage: `@PutMapping("/update")`

5. `@DeleteMapping`
   - Shortcut for @RequestMapping(method = DELETE)
   - Usage: `@DeleteMapping("/delete/{id}")`

### Request Parameters
1. `@RequestParam`
   - Binds request parameters to method parameters
   - Used in: PdfController
   - Usage: `@RequestParam("file") MultipartFile file`

2. `@PathVariable`
   - Binds URL path segments to method parameters
   - Usage: `@PathVariable("id") Long id`

3. `@RequestBody`
   - Binds request body to method parameter
   - Used in: ChatController
   - Usage: `@RequestBody String question`

### Cross-Origin Resource Sharing (CORS)
1. `@CrossOrigin`
   - Enables cross-origin requests
   - Used in: PdfController
   - Usage: `@CrossOrigin(origins = "http://localhost:8080")`

### Exception Handling
1. `@ExceptionHandler`
   - Handles exceptions in controller methods
   - Used in: PdfController
   - Usage: 
   ```java
   @ExceptionHandler(Exception.class)
   public ResponseEntity<String> handleException(Exception e)
   ```

2. `@ControllerAdvice`
   - Enables global exception handling
   - Usage: `@ControllerAdvice public class GlobalExceptionHandler {}`

### Async Processing
1. `@Async`
   - Marks a method for asynchronous execution
   - Used in: PdfProcessingService
   - Usage: `@Async("pdfProcessingExecutor")`

2. `@Scheduled`
   - Configures a method to run on a schedule
   - Usage: `@Scheduled(fixedRate = 1000)`

### Spring Boot Specific
1. `@SpringBootApplication`
   - Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan
   - Used in: DemoApplication
   - Usage: `@SpringBootApplication public class Application {}`

2. `@EnableAutoConfiguration`
   - Enables Spring Boot's auto-configuration
   - Part of @SpringBootApplication
   - Usage: `@EnableAutoConfiguration public class Config {}`

### Lombok Annotations (Working with Spring)
1. `@Slf4j`
   - Creates a logger field
   - Used in: PdfProcessingService, ChatService
   - Usage: `@Slf4j public class MyService {}`

2. `@RequiredArgsConstructor`
   - Generates constructor for final fields
   - Used in: PdfProcessingService, ChatService
   - Usage: `@RequiredArgsConstructor public class MyService {}`

3. `@Data`
   - Generates getters, setters, equals, hashCode, and toString
   - Used in: QAPair
   - Usage: `@Data public class MyDTO {}`

### Testing Annotations
1. `@SpringBootTest`
   - Configures test class for Spring Boot
   - Usage: `@SpringBootTest class MyServiceTest {}`

2. `@MockBean`
   - Creates mock of a bean in test context
   - Usage: `@MockBean private MyService myService;`

3. `@Test`
   - Marks a method as a test case
   - Usage: `@Test void testMethod() {}`

### JPA Annotations (If using Spring Data JPA)
1. `@Entity`
   - Marks a class as JPA entity
   - Usage: `@Entity public class User {}`

2. `@Table`
   - Specifies database table details
   - Usage: `@Table(name = "users")`

3. `@Id`
   - Marks a field as primary key
   - Usage: `@Id private Long id;`

4. `@GeneratedValue`
   - Configures primary key generation
   - Usage: `@GeneratedValue(strategy = GenerationType.IDENTITY)`

## Best Practices

1. **Constructor Injection**
   - Prefer constructor injection (@RequiredArgsConstructor) over field injection (@Autowired)
   - Makes dependencies explicit and supports immutability

2. **Specific Annotations**
   - Use most specific annotation (@Service, @Repository, @Controller) instead of @Component
   - Helps in understanding component's role

3. **Exception Handling**
   - Use @ExceptionHandler for specific exceptions
   - Use @ControllerAdvice for global exception handling

4. **CORS Configuration**
   - Prefer global CORS configuration in WebConfig over @CrossOrigin when possible
   - More maintainable for multiple endpoints

5. **Async Processing**
   - Always specify executor when using @Async
   - Configure timeout and error handling 

### Swagger/OpenAPI Annotations
1. `@Tag`
   - Adds metadata to describe API groups/controllers
   - Usage: 
   ```java
   @Tag(name = "PDF Management", description = "APIs for managing PDF documents")
   @RestController
   public class PdfController {}
   ```

2. `@Operation`
   - Describes an API endpoint
   - Usage:
   ```java
   @Operation(
       summary = "Upload PDF file",
       description = "Uploads and processes a PDF file to extract Q&A pairs",
       responses = {
           @ApiResponse(responseCode = "200", description = "PDF processed successfully"),
           @ApiResponse(responseCode = "400", description = "Invalid file")
       }
   )
   @PostMapping("/upload")
   public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {}
   ```

3. `@Parameter`
   - Documents API parameters
   - Usage:
   ```java
   @PostMapping("/upload")
   public ResponseEntity<String> uploadPdf(
       @Parameter(description = "PDF file to process", required = true)
       @RequestParam("file") MultipartFile file
   ) {}
   ```

4. `@Schema`
   - Describes model properties
   - Usage:
   ```java
   @Data
   public class QAPair {
       @Schema(description = "The question text", example = "What is Spring Boot?")
       private String question;

       @Schema(description = "The answer text", example = "Spring Boot is a framework...")
       private String answer;
   }
   ```

### Complete Swagger Example
Here's a complete example showing how to document a REST API:

```java
@Tag(name = "PDF Management", description = "APIs for managing PDF documents")
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfProcessingService pdfProcessingService;

    @Operation(
        summary = "Upload PDF file",
        description = "Uploads and processes a PDF file to extract Q&A pairs",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "PDF processed successfully",
                content = @Content(mediaType = "text/plain")
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid file",
                content = @Content(mediaType = "text/plain")
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(mediaType = "text/plain")
            )
        }
    )
    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<String>> uploadPdf(
        @Parameter(
            description = "PDF file to process",
            required = true,
            content = @Content(mediaType = "multipart/form-data")
        )
        @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body("Please select a file to upload")
            );
        }

        if (!file.getContentType().equals("application/pdf")) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body("Only PDF files are allowed")
            );
        }

        return pdfProcessingService.processPdf(file)
            .thenApply(v -> ResponseEntity.ok("PDF processed successfully"))
            .exceptionally(e -> ResponseEntity.internalServerError()
                .body("Error processing PDF: " + e.getMessage()));
    }

    @Operation(
        summary = "Process PDF directory",
        description = "Processes all PDF files in a specified directory"
    )
    @PostMapping("/process-directory")
    public CompletableFuture<ResponseEntity<String>> processDirectory(
        @Parameter(description = "Path to directory containing PDF files")
        @RequestParam String directoryPath
    ) {
        return CompletableFuture.completedFuture(
            ResponseEntity.ok("Directory processing is not supported in this version")
        );
    }

    @Operation(
        summary = "Handle exceptions",
        description = "Global exception handler for PDF operations"
    )
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError()
            .body("An error occurred: " + e.getMessage());
    }
}
```

### Setting Up Swagger in Spring Boot

1. Add dependencies to `build.gradle`:
```gradle
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
}
```

2. Configure Swagger in `application.properties`:
```properties
# Swagger UI path
springdoc.swagger-ui.path=/swagger-ui.html

# API docs path
springdoc.api-docs.path=/api-docs

# Enable or disable Swagger UI
springdoc.swagger-ui.enabled=true

# Configure API info
springdoc.info.title=PDF Chatbot API
springdoc.info.description=API for processing PDFs and extracting Q&A pairs
springdoc.info.version=1.0.0
```

3. Access Swagger UI:
   - Open browser at: http://localhost:8080/swagger-ui.html
   - View API documentation
   - Test endpoints directly from the UI

### Best Practices for Swagger Documentation

1. **Consistent Naming**
   - Use clear, consistent names for tags and operations
   - Group related operations under the same tag

2. **Detailed Descriptions**
   - Provide comprehensive descriptions for endpoints
   - Include example requests and responses

3. **Response Documentation**
   - Document all possible response codes
   - Include response schemas and examples

4. **Parameter Documentation**
   - Document all parameters thoroughly
   - Include validation constraints and examples

5. **Security Documentation**
   - Document authentication requirements
   - Specify required scopes or roles 