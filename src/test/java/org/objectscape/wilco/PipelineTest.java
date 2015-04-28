package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Vector;
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
        List<Integer> ints = new Vector<>();

        Channel<Integer> out = sequence(sequence(sequence(generate(1, 2, 3, 4))));
        CompletableFuture<Integer> future = out.onReceive(n -> ints.add(n));
        future.get();

        Assert.assertArrayEquals(ints.toArray(), new Integer[] {1, 256, 6561, 65536});
    }

    private Channel<Integer> sequence(Channel<Integer> channel) throws InterruptedException {
        Channel<Integer> out = wilco.createChannel();
        channel.onReceive(n -> out.send(n * n));
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
