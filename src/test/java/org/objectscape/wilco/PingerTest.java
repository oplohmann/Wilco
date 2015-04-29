package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Vector;
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

    @Test
    public void pinging() throws InterruptedException, ExecutionException {
        Channel<String> channel = wilco.createChannel();
        List<String> output = new Vector<>();
        int iters = 3;

        async(() -> pinger(channel, iters, false));
        async(() -> printer(channel, output));
        async(() -> pinger(channel, iters, true));

        channel.waitTillClosed();

        Assert.assertEquals(iters * 2, output.size());
    }

    private void printer(Channel<String> channel, List<String> outpout) {
        channel.onReceive(message -> outpout.add(message));
    }

    private void pinger(Channel<String> channel, int iters, boolean closeWhenDone) {
        for (int i = 0; i < iters; i++) {
            channel.send("ping");
        }
        if(closeWhenDone) {
            channel.close();
        }
    }
}