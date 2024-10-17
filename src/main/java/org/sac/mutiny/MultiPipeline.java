package org.sac.mutiny;

import io.smallrye.mutiny.Multi;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class MultiPipeline {
    public static void main(String[] args) {

        /*
         - A Multi represents a stream of data. A stream can emit 0, 1, n, or an infinite number of items.
         - A Multi<T> is a data stream that:
                emits 0..n item events
                emits a failure event
                emits a completion event for bounded streams
         - Multi<T> provides many operators that create, transform, and orchestrate Multi sequences.
         - The operators can be used to define a processing pipeline.
         - The events flow in this pipeline, and each operator can process or transform the events.
         - Multis are lazy by nature. To trigger the computation, you must subscribe.
         - Also, the pipeline is materialized for each subscription.
         */
        Multi multiExample = Multi.createFrom().items(1, 2, 3, 4, 5)
                .onItem().transform(i -> {
                    System.out.println("transform: " + i);
                    return i * 2;
                })
                .select().first(3)
                .onFailure().recoverWithItem(0);
        System.out.println("Subscribing multiEx");
        multiExample.subscribe().with(System.out::println);

        // Creating Multi from items
        Multi<Integer> multiFromItems = Multi.createFrom().items(1, 2, 3, 4);
        Multi<Integer> multiFromIterable = Multi.createFrom().iterable(Arrays.asList(1, 2, 3, 4, 5));
        // Every subscriber receives the same set of items (1, 2… 5) just after the subscription.

        // using Suppliers:
        // The Supplier is called for every subscriber, so each of them will get different values.
        AtomicInteger counter = new AtomicInteger();
        Multi<Integer> multiFromSupplier = Multi.createFrom().items(() ->
                IntStream.range(counter.getAndIncrement(), counter.get() * 2).boxed());

        multiFromSupplier.onSubscription();

        // Creating failing Multis
        // indicate to the downstream subscribers that the source encountered a terrible error and cannot continue emitting items
        // Pass an exception directly:
        Multi<Integer> failed1 = Multi.createFrom().failure(new Exception("boom"));
        // Pass a supplier called for every subscriber:
        Multi<Integer> failed2 = Multi.createFrom().failure(() -> new Exception("boom"));

        // Creating empty Multis
        // Unlike Uni, Multi streams don’t send null items
        // Multi streams send completion events indicating that there are no more items to consume
        // Completion event can happen even if there are no items, creating an empty stream.
        Multi<String> multiEmpty = Multi.createFrom().empty();

        // Creating Multis using an emitter
        // This approach is useful when integrating callback-based APIs:
        Multi<Integer> multi = Multi.createFrom().emitter(em -> {
            em.emit(1);
            em.emit(2);
            em.emit(3);
            em.complete();
        });
        // The emitter can also send a failure. It can also get notified of cancellation to,
        // for example, stop the work in progress.

        // Creating Multis from ticks
        Multi<Long> ticks = Multi.createFrom().ticks().every(Duration.ofMillis(100));

        // Creating Multis from a generator
        Multi<Object> sequence = Multi.createFrom().generator(() -> 1, (n, emitter) -> {
            int next = n + (n / 2) + 1;
            if (n < 50) {
                emitter.emit(next);

            } else {
                emitter.complete();
            }
            return next;
        });
        sequence.subscribe().with( n -> System.out.print(n + " "));
        // The initial state is given through a supplier (here () -> 1)
        // generator function accepts 2 arguments:
        // 1. the current state,
        // 2. an emitter that can emit a new item, emit a failure, or emit a completion
        // The generator function return value is the next current state
        // o/p: 2 4 7 11 17 26 40 61

    }
}
