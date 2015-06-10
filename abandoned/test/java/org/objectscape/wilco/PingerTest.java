package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by plohmann on 29.04.2015.
 */

/*

Test case models the Go sample taken from "An Introduction to Programming in Go" by Caleb Doxsey, p. 112
This book is freely available from http://www.golang-book.com or as PDF from https://www.golang-book.com/assets/pdf/gobook.pdf

package main

import (
	"fmt"
	"time"
)

func pinger(c chan string) {
	for i := 0; ; i++ {
		c <- "ping"
	}
}

func printer(c chan string) {
	for {
		msg := <-c
		fmt.Println(msg)
		time.Sleep(time.Second * 1)
	}
}

func main() {
	var c chan string = make(chan string)
	go pinger(c)
	go printer(c)
	var input string
	fmt.Scanln(&input)
}

 */
public class PingerTest extends AbstractTest {

    @Test
    public void pinging() throws InterruptedException, ExecutionException {
        List<String> output = new Vector<>();
        Channel<String> channel = wilco.createChannel();

        int iters = 100;
        int pingers = 2;
        int expectedMessages = iters * pingers;

        printer(output, channel, expectedMessages);

        wilco.execute(() -> pinger(channel, iters));
        wilco.execute(() -> pinger(channel, iters));

        channel.waitTillClosed();
        Assert.assertEquals(expectedMessages, output.size());
    }

    @Test
    public void pingingMonkey() throws InterruptedException, ExecutionException {
        // some monkey testing to see whether it runs very often without race conditions
        CountDownLatch mainLatch = new CountDownLatch(1);
        int loops = 1000;
        for (int i = 0; i < loops; i++) {
            pinging();
            if(i + 1 == loops) {
                mainLatch.countDown();
            }
        }

        mainLatch.await();
    }

    @Test
    public void pingingDeferred() throws InterruptedException, ExecutionException {
        List<String> output = new Vector<>();
        Channel<String> channel = wilco.createChannel();

        int iters = 3;
        int pingers = 2;
        int expectedMessages = iters * pingers;

        for (int i = 0; i < pingers; i++) {
            wilco.execute(() -> pinger(channel, iters));
        }

        // called after pinger was started asynchronously --> deferred
        printer(output, channel, expectedMessages);

        channel.waitTillClosed();
        Assert.assertEquals(expectedMessages, output.size());
    }

    private void printer(List<String> output, Channel<String> channel, int expectedMessages) {
        AtomicInteger messageCount = new AtomicInteger();
        channel.onReceive(message -> {
            output.add(message);
            if (messageCount.incrementAndGet() == expectedMessages) {
                channel.close();
            }
        });
    }

    @Test
    public void pingingDeferredMonkey() throws InterruptedException, ExecutionException {
        // some monkey testing to see whether it runs very often without race conditions
        CountDownLatch mainLatch = new CountDownLatch(1);
        int loops = 1000;
        for (int i = 0; i < loops; i++) {
            pingingDeferred();
            if(i + 1 == loops) {
                mainLatch.countDown();
            }
        }
        mainLatch.await();
    }

    private void pinger(Channel<String> channel, int iters) {
        for (int i = 0; i < iters; i++) {
            channel.send("ping");
        }
    }

    protected int shutdownTimeoutInSecs() {
        return 30;
    }

}
