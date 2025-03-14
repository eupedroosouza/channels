/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Pedro Souza
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.gitub.eupedroosouza.channels.tests;

import com.github.eupedroosouza.channels.channel.PubChannel;
import com.github.eupedroosouza.channels.channel.SubChannel;
import com.github.fppt.jedismock.RedisServer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.StringCodec;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChannelTests {

    private static final String CHANNEL_NAME = "test:channel";
    private static final String MESSAGE = "PING";

    private static final CountDownLatch latch = new CountDownLatch(1);
    private static RedisServer server;
    private static RedisClient client;

    private static PubChannel<String> pubChannel;
    private static SubChannel<String> subChannel;

    private static final AtomicReference<String> receivedChannel = new AtomicReference<>();
    private static final AtomicReference<String> receivedMessage = new AtomicReference<>();

    @BeforeAll
    public static void setup() throws IOException {
        server = RedisServer.newRedisServer(6379);
        server.start();
        Assertions.assertNotNull(server, "server is null");
        Assertions.assertTrue(server.isRunning(), "server is not running");

        client = RedisClient.create("redis://127.0.0.1:6379");
        Assertions.assertNotNull(client, "client is null");
        Assertions.assertEquals("PONG", client.connect().sync().ping());

        pubChannel = PubChannel.<String>builder()
                .client(client)
                .codec(StringCodec.UTF8)
                .build();
        pubChannel.connect();

        subChannel = SubChannel.<String>builder()
                .client(client)
                .codec(StringCodec.UTF8)
                .build();
        subChannel.connect();
        subChannel.sub(CHANNEL_NAME, (channel, message) -> {
            receivedChannel.set(channel);
            receivedMessage.set(message);
            System.out.println();
            latch.countDown();
        });
    }

    @Test
    @Order(1)
    public void pubTest() throws ExecutionException, InterruptedException {
        long publishedChannels = pubChannel.pub(CHANNEL_NAME, MESSAGE).get();
        Assertions.assertEquals(1, publishedChannels);
        latch.await();
    }

    @Test
    @Order(2)
    public void subTest() {
        Assertions.assertEquals(CHANNEL_NAME, receivedChannel.get());
        Assertions.assertEquals(MESSAGE, receivedMessage.get());
    }

    @Test
    @Order(3)
    public void unsubTest() {
        Assertions.assertDoesNotThrow(() -> subChannel.unsub(CHANNEL_NAME));
    }

    @AfterAll
    public static void shutdown() throws IOException {
        if (pubChannel != null) {
            pubChannel.close();
        }
        if (subChannel != null) {
            subChannel.close();
        }
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.stop();
        }
    }

}
