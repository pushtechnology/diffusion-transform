/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.pushtechnology.diffusion.transform.messaging.receive;

import java.util.concurrent.CompletableFuture;

import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * Builder for {@link TransformedRequestStream} that has been bound to a session.
 *
 * @param <T> the type of request understood by Diffusion
 * @param <U> the type of request
 * @param <V> the type of response
 * @author Push Technology Limited
 */
public interface BoundRequestReceiverBuilder<T, U, V> extends RequestReceiverBuilder<U, V> {

    @Override
    <R> BoundRequestReceiverBuilder<T, R, V> unsafeTransformRequest(UnsafeTransformer<U, R> newTransformer);

    @Override
    <R> BoundRequestReceiverBuilder<T, U, R> unsafeTransformResponse(UnsafeTransformer<R, V> newTransformer);

    /**
     * Register a request stream.
     *
     * @param selector the topic selector to match the stream
     * @param stream the request stream
     */
    void setStream(String selector, TransformedRequestStream<T, U, V> stream);

    /**
     * Register a request handler.
     *
     * @param selector the topic selector to match the stream
     * @param handler the request handler
     * @param properties the session properties to receive with the request
     * @return the registration of the handler
     */
    CompletableFuture<Registration> addRequestHandler(
        String selector,
        TransformedRequestHandler<T, U, V> handler,
        String... properties);
}
