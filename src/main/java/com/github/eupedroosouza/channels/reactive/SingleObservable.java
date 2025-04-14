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

package com.github.eupedroosouza.channels.reactive;

public class SingleObservable<T> implements Observable<T> {

    public static <T> SingleObservable<T> create(ObservableSubscriber<T> subscriber) {
        return new SingleObservable<>(subscriber);
    }

    private final ObservableSubscription<T> subscription;

    public SingleObservable(ObservableSubscriber<T> subscriber) {
        this.subscription = new ObservableSubscription<>(this, subscriber);
    }

    @Override
    public void emit(T value) {
        try {
            subscription.getSubscriber().onNext(value);
        } catch (Throwable t) {
            subscription.getSubscriber().onError(t);
        }
    }

    @Override
    public ObservableSubscription<T> subscribe(ObservableSubscriber<T> subscriber) {
        return subscription;
    }

    @Override
    public boolean unsubscribe(ObservableSubscription<T> subscription) {
        return false;
    }

}
