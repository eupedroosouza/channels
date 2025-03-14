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

import com.github.eupedroosouza.channels.util.Builder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public class PubChannel<T> implements Closeable {

    public static <E> PubChannelBuilder<E> builder() {
        return new PubChannelBuilder<>();
    }

    private final Logger logger = LoggerFactory.getLogger(PubChannel.class);
    private final RedisClient client;
    private final RedisCodec<String, T> codec;

    private StatefulRedisPubSubConnection<String, T> connection;

    public PubChannel(PubChannelBuilder<T> builder) {
        this.client = builder.client();
        this.codec = RedisCodec.of(StringCodec.UTF8, builder.codec());
    }

    public void connect() {
        connection = client.connectPubSub(codec);
    }

    public RedisFuture<Long> pub(@NotNull String channel, @NotNull T value) {
        assert connection != null : "connection is null";
        return connection.async().publish(channel, value);
    }

    @Override
    public void close() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }

    public static class PubChannelBuilder<T> implements Builder<PubChannel<T>> {

        private RedisClient client;
        private RedisCodec<?, T> codec;

        public PubChannelBuilder<T> client(@NotNull RedisClient client) {
            this.client = client;
            return this;
        }

        public RedisClient client() {
            return client;
        }

        public PubChannelBuilder<T> codec(@NotNull RedisCodec<?, T> codec) {
            this.codec = codec;
            return this;
        }

        public RedisCodec<?, T> codec() {
            return codec;
        }

        @Override
        public PubChannel<T> build() {
            assert client != null : "client is null";
            if (codec == null) {
                codec = (RedisCodec<?, T>) ByteArrayCodec.INSTANCE;
            }
            return new PubChannel<>(this);
        }
    }

}
