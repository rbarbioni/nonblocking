package com.example.noblocking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.RequestPredicates;
import org.springframework.web.reactive.function.RouterFunction;
import org.springframework.web.reactive.function.RouterFunctions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServer;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@SpringBootApplication
@EnableAutoConfiguration
public class NoBlockingApplication {

	public static void main(String[] args) {
		SpringApplication.run(NoBlockingApplication.class, args);
	}

	@Bean
	public RouterFunction<?> routerFunction(PersonHandler personHandler) {
		return RouterFunctions.route
				(RequestPredicates.GET("/persons"), request -> null).and(
				RouterFunctions.route(RequestPredicates.GET("/persons/{id}"), request -> null)
		);
	}

	@Bean
	public HttpServer server (RouterFunction<?> routerFunction){
		HttpServer httpServer = HttpServer.create(8080);
		httpServer.newHandler(new ReactorHttpHandlerAdapter(RouterFunctions.toHttpHandler(routerFunction)));
		return httpServer;
	}


	@Component
	public class PersonHandler{

		private final PersonRepository personRepository;

		@Autowired
		public PersonHandler(PersonRepository personRepository) {
			this.personRepository = personRepository;
		}

		Flux<Person> all(){
			return Flux.fromStream(this.personRepository.all());
		}

		Mono<Person> byId(String id){
			return Mono.fromFuture(this.personRepository.findById(id));
		}
	}

	@Component
	public class SampleDataCLR implements CommandLineRunner{

		private final PersonRepository personRepository;

		@Autowired
		public SampleDataCLR(PersonRepository personRepository) {
			this.personRepository = personRepository;
		}

		@Override
		public void run(String... args) throws Exception {
			Stream.of( "Mark", "Tim", "Bill", "Steve", "James" )
					.forEach(
							name -> personRepository.save(new Person(name, new Random().nextInt(50))));

			personRepository.all().forEach(System.out::println);
		}
	}

	interface PersonRepository extends MongoRepository<Person, String>{

		CompletableFuture<Person> findById(String id);

		@Query("{}")
		Stream<Person> all();
	}


	@Document
	class Person{

		@Id
		private String id;

		private String  name;

		private int age;

		public Person() {
		}

		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		@Override
		public String toString() {
			return super.toString();
		}
	}
}
