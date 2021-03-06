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

package com.pushtechnology.diffusion.transform.stream;

import java.util.function.Function;

import com.pushtechnology.diffusion.client.features.TimeSeries.Event;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.features.Topics.ValueStream;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.TopicSelector;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * An immutable builder for streams. The builder can be used to chain the {@link UnsafeTransformer}s that will be
 * applied to a stream. The builder can be used as a template to register multiple streams that apply the same
 * transformation. Once the transformations have been chained the {@link #register(Topics, String, ValueStream)}
 * method can be used to register the stream.
 *
 * @param <S> the type of the source values
 * @param <T> the type of the transformed values
 * @param <V> the type of the value stream to be built
 * @param <U> the type of the time series value stream to be built
 * @author Push Technology Limited
 */
public interface StreamBuilder<S, T, V extends ValueStream<T>, U extends ValueStream<Event<T>>> {

    /**
     * Transform the stream that will be built.
     *
     * @param newTransformer the new transformer
     * @param <R> the new type of the transformed values
     * @return a new stream builder
     */
    <R> StreamBuilder<S, R, TransformedStream<S, R>, TransformedStream<Event<S>, Event<R>>>
        unsafeTransform(UnsafeTransformer<T, R> newTransformer);

    /**
     * Transform the stream that will be built.
     *
     * @param newTransformer the new transformer
     * @param <R> the new type of the transformed values
     * @return a new stream builder
     */
    <R> StreamBuilder<S, R, ? extends ValueStream<R>, ? extends ValueStream<Event<R>>>
        transform(Function<T, R> newTransformer);

    /**
     * Create the stream.
     *
     * @param topicsFeature the topics feature
     * @param topicSelector the topic selector to match the stream
     * @param stream the stream handler
     * @return a handle to the stream
     */
    StreamHandle register(Topics topicsFeature, String topicSelector, V stream);

    /**
     * Create the stream.
     *
     * @param session the session
     * @param topicSelector the topic selector to match the stream
     * @param stream the stream handler
     * @return a handle to the stream
     */
    StreamHandle register(Session session, String topicSelector, V stream);

    /**
     * Create the stream.
     *
     * @param topicsFeature the topics feature
     * @param topicSelector the topic selector to match the stream
     * @param stream the stream handler
     * @return a handle to the stream
     */
    StreamHandle register(Topics topicsFeature, TopicSelector topicSelector, V stream);

    /**
     * Create the stream.
     *
     * @param session the session
     * @param topicSelector the topic selector to match the stream
     * @param stream the stream handler
     * @return a handle to the stream
     */
    StreamHandle register(Session session, TopicSelector topicSelector, V stream);

    /**
     * Create the fallback stream.
     *
     * @param topicsFeature the topics feature
     * @param stream the stream handler
     * @return a handle to the stream
     */
    StreamHandle createFallback(Topics topicsFeature, V stream);

    /**
     * Create the fallback stream.
     *
     * @param session the session
     * @param stream the stream handler
     * @return a handle to the stream
     */
    StreamHandle createFallback(Session session, V stream);

    /**
     * Create the stream.
     *
     * @param session the session
     * @param topicSelector the topic selector to match the stream
     * @param stream the stream handler
     * @return a handle to the stream
     */
    StreamHandle createTimeSeries(Session session, String topicSelector, U stream);
}
