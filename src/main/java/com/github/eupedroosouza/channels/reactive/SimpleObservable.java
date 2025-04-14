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

import java.util.ArrayList;
import java.util.List;

public class SimpleObservable<T> implements Observable<T> {

    private final List<ObservableSubscription<T>> subscriptions = new ArrayList<>();

    @Override
    public ObservableSubscription<T> subscribe(ObservableSubscriber<T> subscriber) {
        ObservableSubscription<T> subscription = new ObservableSubscription<>(this, subscriber);
        subscriptions.add(subscription);
        return subscription;
    }

    @Override
    public boolean unsubscribe(ObservableSubscription<T> subscription) {
        return subscriptions.remove(subscription);
    }

    @Override
    public void emit(T value) {
        if (value == null || subscriptions.isEmpty()) {
            return;
        }
        for (ObservableSubscription<T> subscription : subscriptions) {
            try {
                subscription.getSubscriber().onNext(value);
            } catch (Throwable t) {
                subscription.getSubscriber().onError(t);
            }
        }
    }
}
