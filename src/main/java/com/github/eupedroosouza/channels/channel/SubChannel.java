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

package com.github.eupedroosouza.channels.channel;

import com.github.eupedroosouza.channels.listener.OneChannelRedisPubSubListener;
import com.github.eupedroosouza.channels.listener.ReceiveListener;
import com.github.eupedroosouza.channels.util.Builder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SubChannel<T> implements Closeable {

    public static <E> SubChannelBuilder<E> builder() {
        return new SubChannelBuilder<>();
    }

    private final Logger logger = LoggerFactory.getLogger(SubChannel.class);
    private final RedisClient client;
    private final RedisCodec<String, T> codec;
    @Nullable
    private final Executor executor;

    private StatefulRedisPubSubConnection<String, T> connection;
    private final HashMap<String, ReceiveListener<T>> channels = new HashMap<>();

    public SubChannel(SubChannelBuilder<T> builder) {
        this.client = builder.client();
        this.codec = RedisCodec.of(StringCodec.UTF8, builder.codec());
        this.executor = builder.executor();
    }

    public void connect() {
        connection = client.connectPubSub(codec);
        connection.addListener(new OneChannelRedisPubSubListener<String, T>() {
            @Override
            public void message(String channel, T message) {
                if (channels.containsKey(channel)) {
                    try {
                        runAsync(() -> channels.get(channel).onReceive(channel, message));
                    } catch (Exception ex) {
                        logger.error("Error while receiving channel [{}]", channel, ex);
                    }
                } else {
                    logger.warn("Received unknown channel: {}", channel);
                }
            }

            @Override
            public void subscribed(String channel, long count) {
                logger.debug("Subscribed to channel [{}] with [{}] subscribed channels ", channel, count);
            }

            @Override
            public void unsubscribed(String channel, long count) {
                logger.debug("Unsubscribe to channel [{}] with [{}] subscribed channels ", channel, count);
            }
        });
    }

    private void unsubscribe() {
        assert connection != null : "connection is null";
        String[] channelsArray = channels.keySet().toArray(new String[0]);
        if (channelsArray.length > 0) {
            logger.debug("Unsubscribing channels...");
            connection.sync().unsubscribe(channelsArray);
        }

    }

    private void subscribe() {
        assert connection != null : "connection is null";
        String[] channelsArray = channels.keySet().toArray(new String[0]);
        if (channelsArray.length > 0) {
            logger.debug("Subscribing channels...");
            connection.sync().subscribe(channelsArray);
        }
    }

    @Override
    public void close() throws IOException {
        unsubscribe();
        channels.clear();
        if (connection != null) {
            connection.close();
        }
    }

    public void sub(String channel, ReceiveListener<T> listener) {
        assert !channels.containsKey(channel) : "channel " + channel + " already exists";
        this.unsubscribe();
        channels.put(channel, listener);
        this.subscribe();
        logger.info("Subscribed channel [{}].", channel);
    }

    public void unsub(String channel) {
        if (!channels.containsKey(channel)) {
            return;
        }
        this.unsubscribe();
        channels.remove(channel);
        this.subscribe();
        logger.info("Unsubscribed channel [{}].", channel);
    }

    private CompletableFuture<Void> runAsync(Runnable runnable) {
        if (executor != null) {
            return CompletableFuture.runAsync(runnable, executor);
        }
        return CompletableFuture.runAsync(runnable);
    }

    public static class SubChannelBuilder<T> implements Builder<SubChannel<T>> {

        private RedisClient client;
        private RedisCodec<?, T> codec;
        private @Nullable Executor executor;

        public SubChannelBuilder<T> client(@NotNull RedisClient client) {
            this.client = client;
            return this;
        }

        public RedisClient client() {
            return client;
        }

        public SubChannelBuilder<T> codec(@NotNull RedisCodec<?, T> codec) {
            this.codec = codec;
            return this;
        }

        public RedisCodec<?, T> codec() {
            return codec;
        }

        public SubChannelBuilder<T> executor(@NotNull Executor executor) {
            this.executor = executor;
            return this;
        }

        public @Nullable Executor executor() {
            return executor;
        }

        @Override
        public SubChannel<T> build() {
            assert client != null : "client is null";
            if (codec == null) {
                codec = (RedisCodec<?, T>) ByteArrayCodec.INSTANCE;
            }
            return new SubChannel<>(this);
        }
    }

}
