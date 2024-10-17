package org.sac.mutiny;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;

public class ObservingEvents {
    public static void main(String[] args) {

        Multi multiExample = Multi.createFrom().items(1, 2, 3);
        multiExample = multiExample
                .onSubscription()
                .invoke(() -> System.out.println("⬇️ Subscribed"))
                .onItem()
                .invoke(i -> System.out.println("⬇️ Received item: " + i))
                .onFailure()
                .invoke(f -> System.out.println("⬇️ Failed with " + f))
                .onCompletion()
                .invoke(() -> System.out.println("⬇️ Completed"))
                .onCancellation()
                .invoke(() -> System.out.println("⬆️ Cancelled"))
                .onRequest()
                .call(l -> {
                    System.out.println("⬆️ Requested: " + l);
                    return Uni.createFrom().voidItem();
                });

        System.out.println("Subscribing");
        Cancellable cancellable = multiExample.subscribe().with(item -> System.out.println("finshed "+  item));
        System.out.println("main finished");
    }
}
