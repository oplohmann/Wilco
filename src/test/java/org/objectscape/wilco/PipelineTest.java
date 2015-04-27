package org.objectscape.wilco;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by plohmann on 23.04.2015.
 */
public class PipelineTest {

    private Wilco wilco = Wilco.newInstance(new Config());
    private Queue globalQueue = wilco.createQueue();

    @Test
    public void generateSequence() throws ExecutionException, InterruptedException {
        // modeled after this sample in Go: https://blog.golang.org/pipelines

        Channel<Integer> channel = generate(1, 2, 3, 4);
        Channel<Integer> out = sequence(sequence(sequence(channel)));
        CompletableFuture<Integer> future = out.onReceive(n -> {
            System.out.println(n);
        });

        future.get();
    }

    private Channel<Integer> sequence(Channel<Integer> channel) throws InterruptedException {
        Channel<Integer> out = wilco.createChannel();
        // FIXME - stuff breaks if Thread.sleep(1000) is done here
        channel.onReceive(n -> out.send(n * n));
        // FIXME - stuff breaks if Thread.sleep(1000) is done here
        channel.onClose(() -> out.close(0));
        return out;
    }

    private Channel<Integer> generate(int ... ints) {
        Channel<Integer> out = wilco.createChannel();
        globalQueue.execute(() -> {
            for (int i : ints) {
                out.send(i);
            }
            out.close(0);
        });
        return out;
    }

}
