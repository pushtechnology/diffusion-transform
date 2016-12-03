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

package com.pushtechnology.diffusion.transform.messaging;

import com.pushtechnology.diffusion.transform.transformer.Transformer;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * A builder for {@link TransformedMessageStream}s that has been bound to a session.
 *
 * @param <V> the type of values
 * @author Push Technology Limited
 */
public interface BoundTransformedMessageStreamBuilder<V> extends
        BoundMessageStreamBuilder<V, TransformedMessageStream<V>>,
        TransformedMessageStreamBuilder<V> {

    @Override
    <R> BoundTransformedMessageStreamBuilder<R> transform(Transformer<V, R> newTransformer);

    @Override
    <R> BoundTransformedMessageStreamBuilder<R> transformWith(UnsafeTransformer<V, R> newTransformer);

    @Override
    MessageStreamHandle register(TransformedMessageStream<V> stream);
}