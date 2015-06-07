package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by plohmann on 23.04.2015.
 */
//@Ignore
public class PipelineTest extends AbstractTest {

    @Test
    public void generateSequence() throws ExecutionException, InterruptedException, TimeoutException {
        // modeled after this sample in Go: https://blog.golang.org/pipelines
        List<Integer> ints = new Vector<>();

        Channel<Integer> out = sequence(sequence(sequence(generate(1, 2, 3, 4))));
        CompletableFuture<Integer> closeFuture = out.onReceive(n -> ints.add(n));
        closeFuture.get(500, TimeUnit.SECONDS);

        Assert.assertArrayEquals(new Integer[]{1, 256, 6561, 65536}, ints.toArray());
    }

    @Test
    public void generateSequenceOneLevel() throws ExecutionException, InterruptedException, TimeoutException {
        // modeled after this sample in Go: https://blog.golang.org/pipelines
        List<Integer> ints = new Vector<>();

        Channel<Integer> out = sequence(generate(1, 2, 3, 4));
        CompletableFuture<Integer> closeFuture = out.onReceive(n -> ints.add(n));
        closeFuture.get(5000, TimeUnit.SECONDS);

        Assert.assertArrayEquals(new Integer[]{1, 256, 6561, 65536}, ints.toArray());
    }

    private Channel<Integer> sequence(Channel<Integer> in) throws InterruptedException {
        Channel<Integer> out = wilco.createChannel();
        in.onReceive(n -> out.send(n * n));
        in.onClose(() -> out.close(0));
        return out;
    }

    private Channel<Integer> generate(int ... ints) {
        Channel<Integer> out = wilco.createChannel();
        wilco.execute(() -> {
            for (int i : ints) {
                out.send(i);
            }
            out.close(0);
        });
        return out;
    }

    @Test
    public void generateSequenceMonkey() throws InterruptedException, ExecutionException, TimeoutException {
        // some monkey testing to detect problems out of async calls
        for (int i = 0; i < 50; i++) {
            generateSequence();
        }
    }

    protected int shutdownTimeoutInSecs() {
        return 5000;
    }

}
