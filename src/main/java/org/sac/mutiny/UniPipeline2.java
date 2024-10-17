package org.sac.mutiny;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;

import java.time.Duration;

public class UniPipeline2 {
    public static void main(String[] args) {

        // pipeline is materialized for each subscription.
        // When subscribing to a Uni, you can pass an item callback (invoked when the item is emitted),
        // or two callbacks (one receiving the item and one receiving the failure):
        Uni<String> result = method1();

        result.subscribe().with( item -> System.out.println("finished1: " + item + " " + Thread.currentThread()));
        System.out.println("subscribed1");

        // cancellable object allows canceling the operation if need to be.
        Cancellable cancellable = result.subscribe().with(
                item -> System.out.println("finished2: " + item + " " + Thread.currentThread()),
                failure -> System.out.println("finished2: failed with " + failure)
        );
        System.out.println("subscribed2");

        System.out.println("main finished " + Thread.currentThread());
    }

    public static Uni<String> method1() {
        Uni<String> result = Uni.createFrom().item(1)
                .onItem().transform(i -> {
                    System.out.println("t1 " + Thread.currentThread());
                    return "hello" + i;
                })
                .onItem().delayIt().by(Duration.ofMillis(1000))
                .onItem().invoke( () -> {
                    System.out.println("t2 " + Thread.currentThread());
                });

        System.out.println("method1 returning");

        return result;
    }
}
