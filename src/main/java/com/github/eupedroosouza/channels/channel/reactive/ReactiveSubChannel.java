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

package com.github.eupedroosouza.channels.channel.reactive;

import com.github.eupedroosouza.channels.channel.SubChannel;
import com.github.eupedroosouza.channels.reactive.SimpleObservable;
import com.github.eupedroosouza.channels.util.Builder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.RedisCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executor;

public class ReactiveSubChannel<T> implements Closeable {

    public static <S> ReactiveSubChannelBuilder<S> builder() {
        return new ReactiveSubChannelBuilder<>();
    }

    private final SimpleObservable<ReactiveMessage<T>> observable;
    private final SubChannel<T> subChannel;

    private ReactiveSubChannel(ReactiveSubChannelBuilder<T> builder) {
        SubChannel.SubChannelBuilder<T> subChannelBuilder = SubChannel.<T>builder()
                .client(builder.client())
                .codec(builder.codec());
        final Executor executor = builder.executor();
        if (executor != null) {
            subChannelBuilder.executor(executor);
        }
        this.subChannel = subChannelBuilder.build();
        this.observable = builder.observable() != null ? builder.observable() : new SimpleObservable<>();
    }

    public void connect() {
        subChannel.connect();
    }

    @Override
    public void close() throws IOException {
        subChannel.close();
    }

    public void sub(String channel) {
        subChannel.sub(channel, this::onReceive);
    }

    public void unsub(String channel) {
        subChannel.unsub(channel);
    }

    protected void onReceive(String channel, T message) {
        observable.emit(new ReactiveMessage<>(channel, message));
    }

    public SimpleObservable<ReactiveMessage<T>> getObservable() {
        return observable;
    }

    public static class ReactiveSubChannelBuilder<T> implements Builder<ReactiveSubChannel<T>> {

        private RedisClient client;
        private RedisCodec<?, T> codec;
        private @Nullable Executor executor;

        private SimpleObservable<ReactiveMessage<T>> observable;

        public ReactiveSubChannelBuilder<T> client(@NotNull RedisClient client) {
            this.client = client;
            return this;
        }

        public RedisClient client() {
            return client;
        }

        public ReactiveSubChannelBuilder<T> codec(@NotNull RedisCodec<?, T> codec) {
            this.codec = codec;
            return this;
        }

        public RedisCodec<?, T> codec() {
            return codec;
        }

        public ReactiveSubChannelBuilder<T> executor(@NotNull Executor executor) {
            this.executor = executor;
            return this;
        }

        public @Nullable Executor executor() {
            return executor;
        }

        public ReactiveSubChannelBuilder<T> observable(@NotNull SimpleObservable<ReactiveMessage<T>> observable) {
            this.observable = observable;
            return this;
        }

        public SimpleObservable<ReactiveMessage<T>> observable() {
            return observable;
        }

        @Override
        public ReactiveSubChannel<T> build() {
            return new ReactiveSubChannel<>(this);
        }
    }

}
