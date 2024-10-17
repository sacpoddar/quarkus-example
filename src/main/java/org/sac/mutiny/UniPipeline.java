package org.sac.mutiny;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class UniPipeline {
    public static void main(String[] args) {
        /*
        - A Uni represents a stream that can only emit either an item or a failure event
              Note that the item can be null.
        - Unis are lazy by nature. To trigger the computation, you must subscribe.
        - You can consider the item event as a completion signal indicating the success of the operation.
        - Uni<T> provides many operators that create, transform, and orchestrate Uni sequences
        */

        Cancellable cancellable =
            Uni.createFrom().item(1)
                .onItem().transform(i -> {
                    System.out.println("t1 " + Thread.currentThread());
                    return "hello" + i;
                })
                .onItem().delayIt().by(Duration.ofMillis(1000))   // blocking call. Main thread will skip to code after pipeline
                .onItem().invoke( () -> {                         // Remaining pipeline will be executed in worker pool
                    System.out.println("t2 " + Thread.currentThread());
                })
                .subscribe().with(
                        item -> System.out.println("finished: " + item + " " + Thread.currentThread()),
                        failure -> System.out.println("finished: failed with " + failure));
        // .subscribeAsCompletionStage();
        // .subscribe().asCompletionStage();

        // pipeline is materialized for each subscription.
        // When subscribing to a Uni, you can pass an item callback (invoked when the item is emitted),
        // or two callbacks (one receiving the item and one receiving the failure):
        // cancellable object allows canceling the operation if need to be.

        System.out.println("main finished " + Thread.currentThread());

        // Creating Unis from items
        Uni<Integer> uni = Uni.createFrom().item(1);
        // Every subscriber receives the item 1 just after the subscription.

        // Creating Unis from supplier
        AtomicInteger counter = new AtomicInteger();
        Uni<Integer> uni2 = Uni.createFrom().item(() -> counter.getAndIncrement());
        // The Supplier is called for every subscriber. So, each of them will get a different value.

        // Creating failing Unis
        // Pass an exception directly:
        Uni<Integer> failed1 = Uni.createFrom().failure(new Exception("boom"));
        // Pass a supplier called for every subscriber:
        Uni<Integer> failed2 = Uni.createFrom().failure(() -> new Exception("boom"));

        // Creating Uni<Void>
        // indicates operation that does not produce a result
        Uni<Void> uni3 = Uni.createFrom().nullItem();

        // Creating Unis using an emitter
        Uni<String> uni4 = Uni.createFrom().emitter(em -> {
            // When the result is available, emit it
            // em.complete(result);
        });

        // Creating Unis from a CompletionStage
        // Uni<String> uni5 = Uni.createFrom().completionStage(stage);

        // You can also create a CompletionStage from a Uni using
        uni3.subscribe().asCompletionStage();

    }

}
