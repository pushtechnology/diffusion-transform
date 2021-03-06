/*******************************************************************************
 * Copyright (C) 2018 Push Technology Ltd.
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

package com.pushtechnology.diffusion.transform.updater;

import com.pushtechnology.diffusion.client.features.TimeSeries;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * Implementation of {@link BoundTransformedUpdaterBuilder}.
 *
 * @param <S> The type of value understood by the topic
 * @param <T> The type of value updates are provided as
 * @author Push Technology Limited
 */
/*package*/ final class BoundTransformedUpdaterBuilderImpl<S, T> implements BoundTransformedUpdaterBuilder<S, T> {
    private final Session session;
    private final Class<S> valueType;
    private final UnsafeTransformer<T, S> transformer;

    BoundTransformedUpdaterBuilderImpl(
            Session session,
            Class<S> valueType,
            UnsafeTransformer<T, S> transformer) {
        this.session = session;
        this.valueType = valueType;
        this.transformer = transformer;
    }

    @Override
    public <R> BoundTransformedUpdaterBuilder<S, R> unsafeTransform(UnsafeTransformer<R, T> newTransformer) {
        return new BoundTransformedUpdaterBuilderImpl<>(
            session,
            valueType,
            newTransformer.chainUnsafe(transformer));
    }

    @Override
    public <R> BoundTransformedUpdaterBuilder<S, R> unsafeTransform(
        UnsafeTransformer<R, T> newTransformer, Class<R> type) {
        return new BoundTransformedUpdaterBuilderImpl<>(
            session,
            valueType,
            newTransformer.chainUnsafe(transformer));
    }

    @Override
    public TransformedUpdater<S, T> create() {
        final TopicUpdateControl updateControl = session.feature(TopicUpdateControl.class);
        return new TransformedUpdaterImpl<>(updateControl.updater().valueUpdater(valueType), transformer);
    }

    @Override
    public TimeSeriesUpdater<T> createTimeSeries() {
        return new TransformedTimeSeriesUpdater<>(session.feature(TimeSeries.class), valueType, transformer);
    }

    @Override
    public UnboundTransformedUpdaterBuilder<S, T> unbind() {
        return new UnboundTransformedUpdaterBuilderImpl<>(valueType, transformer);
    }

    @Override
    public void register(String topicPath, TransformedUpdateSource<S, T, TransformedUpdater<S, T>> updateSource) {
        final TopicUpdateControl updateControl = session.feature(TopicUpdateControl.class);
        updateControl.registerUpdateSource(
            topicPath,
            new UpdateSourceAdapter<>(new UpdateControlValueCache(updateControl), this.unbind(), updateSource));
    }
}
