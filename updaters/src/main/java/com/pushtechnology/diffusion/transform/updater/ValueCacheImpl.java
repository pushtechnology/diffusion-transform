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

import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.topics.TopicSelector;

/**
 * Implementation of {@link ValueCache}.
 *
 * @param <S> The type of value returned by the cache
 * @author Push Technology Limited
 */
/*package*/ final class ValueCacheImpl<S> implements ValueCache<S> {
    private final TopicUpdateControl.ValueUpdater<S> delegate;

    ValueCacheImpl(TopicUpdateControl.ValueUpdater<S> delegate) {
        this.delegate = delegate;
    }

    @Override
    public S getCachedValue(String topicPath) throws IllegalArgumentException, ClassCastException {
        return delegate.getCachedValue(topicPath);
    }

    @Override
    public void removeCachedValues(String topics) throws IllegalArgumentException {
        delegate.removeCachedValues(topics);
    }

    @Override
    public void removeCachedValues(TopicSelector topics) throws IllegalArgumentException {
        delegate.removeCachedValues(topics);
    }
}
