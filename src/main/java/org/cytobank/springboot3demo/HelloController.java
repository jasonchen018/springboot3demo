package org.cytobank.springboot3demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {

    @GetMapping("hello")
    public GreetResponse hello(){
        GreetResponse response = new GreetResponse("hello",
                List.of("Java", "Golang", "Rust"),
                new Person("Jason", 36, 30_000));

        return response;
    }

    record Person(String name, int age, double savings){}

    record GreetResponse(String greet,
                         List<String> favProgramingLanguages,
                         Person person){
    }
}
