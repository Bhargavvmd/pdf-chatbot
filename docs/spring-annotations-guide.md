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

### Bean Lifecycle Annotations
1. `@PostConstruct`
   - Method is called after dependency injection is complete
   - Used for initialization code
   - Usage:
   ```java
   @PostConstruct
   public void init() {
       // initialization code
   }
   ```

2. `@PreDestroy`
   - Method is called before bean is destroyed
   - Used for cleanup code
   - Usage:
   ```java
   @PreDestroy
   public void cleanup() {
       // cleanup code
   }
   ```

### Validation Annotations
1. `@Valid`
   - Triggers validation of an object
   - Usage: `public void saveUser(@Valid @RequestBody User user)`

2. `@Validated`
   - Enables validation at class level
   - Usage: `@Validated public class UserService`

3. `@NotNull`
   - Validates that field is not null
   - Usage: `@NotNull private String name;`

4. `@Size`
   - Validates size of string, collection, or array
   - Usage: `@Size(min = 2, max = 30) private String name;`

5. `@Min` / `@Max`
   - Validates numeric value bounds
   - Usage: `@Min(18) @Max(100) private int age;`

6. `@Pattern`
   - Validates string against regex pattern
   - Usage: `@Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$") private String email;`

7. `@Email`
   - Validates email format
   - Usage: `@Email private String email;`

### Cache Annotations
1. `@Cacheable`
   - Caches method result
   - Usage: `@Cacheable("users") public User getUser(String id)`

2. `@CacheEvict`
   - Removes entries from cache
   - Usage: `@CacheEvict(value = "users", allEntries = true)`

3. `@CachePut`
   - Updates cache without affecting method execution
   - Usage: `@CachePut(value = "users", key = "#user.id")`

### Transaction Annotations
1. `@Transactional`
   - Defines transaction boundaries
   - Usage: 
   ```java
   @Transactional(readOnly = true)
   public User getUser(String id)
   ```

2. `@EnableTransactionManagement`
   - Enables Spring's transaction management
   - Usage: `@EnableTransactionManagement public class Config`

### Security Annotations
1. `@Secured`
   - Defines access control based on roles
   - Usage: `@Secured("ROLE_ADMIN")`

2. `@PreAuthorize`
   - More flexible security expression
   - Usage: `@PreAuthorize("hasRole('ADMIN') and #user.id == authentication.principal.id")`

3. `@PostAuthorize`
   - Checks after method execution
   - Usage: `@PostAuthorize("returnObject.userId == authentication.principal.id")`

### Scheduling Annotations
1. `@Scheduled`
   - Configures method for scheduling
   - Usage:
   ```java
   @Scheduled(fixedRate = 1000)
   public void scheduleTask() {}
   ```

2. `@EnableScheduling`
   - Enables scheduling capabilities
   - Usage: `@EnableScheduling public class Config`

### Event Handling Annotations
1. `@EventListener`
   - Marks method as event listener
   - Usage:
   ```java
   @EventListener
   public void handleUserCreatedEvent(UserCreatedEvent event) {}
   ```

2. `@TransactionalEventListener`
   - Listens for events within transaction
   - Usage:
   ```java
   @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
   public void handleUserCreatedEvent(UserCreatedEvent event) {}
   ```

### Additional JPA Annotations
1. `@Column`
   - Specifies column mapping
   - Usage: `@Column(name = "user_name", length = 50)`

2. `@JoinColumn`
   - Specifies foreign key column
   - Usage: `@JoinColumn(name = "user_id")`

3. `@OneToMany` / `@ManyToOne`
   - Defines relationship mapping
   - Usage:
   ```java
   @OneToMany(mappedBy = "user")
   private List<Order> orders;
   ```

4. `@Enumerated`
   - Specifies how to persist enum
   - Usage: `@Enumerated(EnumType.STRING)`

### Aspect-Oriented Programming (AOP) Annotations
1. `@Aspect`
   - Declares class as aspect
   - Usage: `@Aspect public class LoggingAspect`

2. `@Around`
   - Defines around advice
   - Usage:
   ```java
   @Around("execution(* com.example.service.*.*(..))")
   public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable
   ```

3. `@Before` / `@After`
   - Defines before/after advice
   - Usage: `@Before("execution(* com.example.service.*.*(..))")`

### Testing Additional Annotations
1. `@DataJpaTest`
   - Configures JPA test slice
   - Usage: `@DataJpaTest class UserRepositoryTest`

2. `@WebMvcTest`
   - Configures MVC test slice
   - Usage: `@WebMvcTest(UserController.class)`

3. `@AutoConfigureMockMvc`
   - Configures MockMvc
   - Usage: `@AutoConfigureMockMvc class ControllerTest`

### Conditional Annotations
1. `@ConditionalOnProperty`
   - Condition based on property value
   - Usage: `@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")`

2. `@Profile`
   - Condition based on active profile
   - Usage: `@Profile("dev")`

3. `@ConditionalOnBean`
   - Condition based on bean existence
   - Usage: `@ConditionalOnBean(DataSource.class)`

### Best Practices for Additional Annotations

1. **Lifecycle Management**
   - Use @PostConstruct for initialization instead of constructor
   - Use @PreDestroy for cleanup instead of finalize()

2. **Validation**
   - Combine multiple constraints when needed
   - Use custom validation for complex rules

3. **Caching**
   - Use appropriate cache names and keys
   - Consider cache eviction strategy

4. **Transactions**
   - Define appropriate transaction boundaries
   - Use readOnly when possible

5. **Security**
   - Use method security over URL security when possible
   - Prefer @PreAuthorize over @Secured for flexibility 