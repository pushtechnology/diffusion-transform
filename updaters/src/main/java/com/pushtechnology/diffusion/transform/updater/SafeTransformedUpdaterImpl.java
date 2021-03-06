/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
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

import java.util.function.Function;

import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater.UpdateCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater.UpdateContextCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.ValueUpdater;

/**
 * Implementation of {@link SafeTransformedUpdater}.
 *
 * @param <S> The type of value understood by the topic
 * @param <T> The type of value updates are provided as
 * @author Push Technology Limited
 */
/*package*/ final class SafeTransformedUpdaterImpl<S, T> implements SafeTransformedUpdater<S, T> {
    private final ValueUpdater<S> updater;
    private final Function<T, S> transformer;

    SafeTransformedUpdaterImpl(ValueUpdater<S> updater, Function<T, S> transformer) {
        this.updater = updater;
        this.transformer = transformer;
    }

    @Override
    public void update(String topicPath, T value, UpdateCallback callback) {
        updater.update(topicPath, transformer.apply(value), callback);
    }

    @Override
    public <C> void update(String topicPath, T value, C context, UpdateContextCallback<C> callback) {
        updater.update(topicPath, transformer.apply(value), context, callback);
    }

    @Override
    public ValueCache<S> untransformedValueCache() {
        return new ValueCacheImpl<>(updater);
    }
}
