package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

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

    final private static Random RAND = new Random(System.currentTimeMillis());

    @Test
    public void pinging() throws InterruptedException, ExecutionException {
        CountDownLatch mainLatch = new CountDownLatch(1);
        int loops = 500;
        for (int i = 0; i < loops; i++) {
            List<String> output = new Vector<>();
            Channel<String> channel = wilco.createChannel();
            channel.onReceive(message -> output.add(message));

            CountDownLatch latch = new CountDownLatch(2);
            int iters = 3;

            wilco.execute(() -> pinger(channel, iters, latch));
            Thread.sleep(RAND.nextInt(20) + 1);
            wilco.execute(() -> pinger(channel, iters, latch));

            channel.waitTillClosed();
            Assert.assertEquals(iters * 2, output.size());

            System.out.println(i);
            if(i + 1 == loops) {
                mainLatch.countDown();
            }
        }

        mainLatch.await();
    }

    private void printer(Channel<String> channel, List<String> outpout) {
        channel.onReceive(message -> outpout.add(message));
    }

    private void pinger(Channel<String> channel, int iters, CountDownLatch latch) {
        for (int i = 0; i < iters; i++) {
            channel.send("ping");
        }
        latch.countDown();
        try {
            latch.await();
        } catch (InterruptedException e) { }
        channel.close();
    }
}
