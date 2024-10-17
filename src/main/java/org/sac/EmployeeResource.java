package org.sac;

import io.quarkus.logging.Log;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Path("/employee")
public class EmployeeResource {

    public static class Employee {
        private String name;
        private Integer age;

        public Employee(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }
        public Integer getAge() {
            return age;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

    // http://localhost:8080/api/employee/sachin?age=5
    @GET
    @Path("/{name}")
    public String allParams(@RestPath String name,
                            @RestQuery Integer age
                            ) {
        return name + "/" + age;
    }

    // use regular expressions in your path
    // http://localhost:8080/api/employee/sachin/5  - will work
    // http://localhost:8080/api/employee/sachin/0x5 - will ignore
    // using @RestPath and @RestQuery annotation is optional
    @Path("{name}/{age:\\d+}")
    @GET
    public String personalisedHello(String name, int age) {
        return "Hello " + name + " is your age really " + age + "?";
    }


    // returning custom class. Requires dependency 'io.quarkus:quarkus-resteasy-reactive-jackson'
    // http://localhost:8080/api/employee/employee
    @GET
    @Path("/employee")
    @Produces(MediaType.APPLICATION_JSON)
    public Employee getEmployee() {
        return new Employee("sachin", 30);
    }

    // Any method parameter with no annotation will receive the method body
    @POST
    @Path("/employee")
    @Consumes(MediaType.APPLICATION_JSON)
    public Employee addEmployee(Employee requestBody) {
        Log.info("Added " + requestBody);
        return requestBody;
    }

    // Setting other response properties
    // http://localhost:8080/api/employee/hello
    @GET
    @Path("/hello")
    public RestResponse<String> hello() {
        // HTTP OK status with text/plain content type
        return ResponseBuilder.ok("Hello, World!", MediaType.TEXT_PLAIN_TYPE)
                // set a response header
                .header("X-Cheese", "Camembert")
                // set the Expires response header to two days from now
                .expires(Date.from(Instant.now().plus(Duration.ofDays(2))))
                // send a new cookie
                .cookie(new NewCookie("Flavour", "chocolate"))
                // end of builder API
                .build();
    }

    // Setting other response properties
    // http://localhost:8080/api/employee/hello-status
    @GET
    @Path("/hello-status")
    public RestResponse<Employee> helloStatus() {
        // HTTP OK status with text/plain content type
        return ResponseBuilder.ok(new Employee("sac", 30), MediaType.APPLICATION_JSON)
                .status(Response.Status.CONFLICT)
                .build();
    }

    // Setting other response properties using annotation
    // http://localhost:8080/api/employee/hello-annotation
    @ResponseStatus(201)
    @ResponseHeader(name = "X-Cheese", value = "Camembert")
    @GET
    @Path("/hello-annotation")
    public String helloAnnotation() {
        return "Hello, World!";
    }

    // Async/reactive support
    // - to accomplish an asynchronous or reactive task before being able to answer
    // - current HTTP request will be automatically suspended after your method,
    //   until the returned Uni instance resolves to a value
    // - This allows you to not block the event-loop thread while method is processing
    //   and allows Quarkus to serve more requests
    // http://localhost:8080/api/employee/hello-async
    @GET
    @Path("/hello-async")
    public Uni<Employee> helloAsync() {
        return Uni.createFrom().item( () -> new Employee("sac", 30));
    }

    // Streaming support
    // useful for streaming text or binary data.
    @GET
    @Path("/hello-streaming")
    public Multi<Employee> streamExample() {
        List<Employee> list = Arrays.asList(
                new Employee("sac1", 30),
                new Employee("sac2", 31) );
        Stream<Employee> stream = list.stream();
        return Multi.createFrom().items(stream);
    }

    // Accessing context objects
    // framework will give you these, if your endpoint method takes parameters of these type
    // you can also inject those context objects using @Inject on fields of the same type:
    /*
    HttpHeaders     All the request headers
    ResourceInfo    Information about the current endpoint method and class (requires reflection)
    SecurityContext Access to the current user and roles
    UriInfo         Provides information about the current endpoint and application URI
    Request         Advanced: Access to the current HTTP method and Preconditions
    HttpServerRequest   Advanced: Vert.x HTTP Request
    HttpServerResponse  Advanced: Vert.x HTTP Response
     */
    //    @Inject
    //    SecurityContext security;
    @GET
    @Path("/hello-context")
    public String contextExample(SecurityContext security) {

        Principal user = security.getUserPrincipal();
        return user != null ? user.getName() : "<NOT LOGGED IN>";
    }

    // JSON serialisation
    // Instead of importing io.quarkus:quarkus-rest, you can import
    // io.quarkus:quarkus-rest-jackson
    // importing those modules will allow HTTP message bodies to be read from JSON and serialised to JSON


    // Exception mapping
    // see ExceptionMappers.java
    // http://localhost:8080/api/employee/cheeses/salty
    @GET
    @Path("/cheeses/{cheese}")
    public String findCheese(String cheese) {
        Log.info("calling cheese service");
        if(!cheese.equals("salty"))
            // send a 404
            throw new NotFoundException("Unknown cheese: " + cheese);
        return "Salty cheese";
    }

    @GET
    @Path("/cheeses")
    public String searchCheese(
            @RestQuery
            @NotBlank
            String cheese) {
        if(!cheese.equals("salty"))
            // send a 404
            throw new NotFoundException("Unknown cheese: " + cheese);
        return "Salty cheese";
    }

}