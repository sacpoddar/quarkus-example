package org.sac.mutiny;

import io.smallrye.mutiny.Uni;

public class UniExample {

    public static void main(String[] args) {
        Uni.createFrom().item("hello")     // create a Uni emitting the "hello" item. This is the input of our pipeline
                .onItem().transform(item -> item + " mutiny")       // pipeline: append " mutiny"
                .onItem().transform(String::toUpperCase)            // pipeline: make it an uppercase string
                .subscribe().with(item -> System.out.println(">> " + item));       // subscribe to the pipeline
        // NOTE: If you don’t have a final subscriber, nothing is going to happen

        // Mutiny uses a builder API!
        // Appending a new stage to a pipeline returns a new Uni.
        // The previous program is equivalent to
        Uni<String> uni1 = Uni.createFrom().item("hello");
        Uni<String> uni2 = uni1.onItem().transform(item -> item + " mutiny");
        Uni<String> uni3 = uni2.onItem().transform(String::toUpperCase);

        uni3.subscribe().with(item -> System.out.println(">> " + item));

        // not equivalent to:
        Uni<String> uni = Uni.createFrom().item("hello");
        uni.onItem().transform(item -> item + " mutiny");
        uni.onItem().transform(String::toUpperCase);
        uni.subscribe().with(item -> System.out.println(">> " + item));     // prints ">> hello"

        // Mutiny APIs are not fluent and each computation stage returns a new object.

    }
}
