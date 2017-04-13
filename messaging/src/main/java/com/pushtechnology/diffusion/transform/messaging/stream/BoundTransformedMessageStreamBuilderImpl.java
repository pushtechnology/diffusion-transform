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

package com.pushtechnology.diffusion.transform.messaging.stream;

import static com.pushtechnology.diffusion.transform.transformer.Transformers.chain;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.toTransformer;

import com.pushtechnology.diffusion.client.content.Content;
import com.pushtechnology.diffusion.client.features.Messaging;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.transform.transformer.Transformer;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * Implementation of {@link BoundTransformedMessageStreamBuilder}.
 *
 * @param <V> the type of values
 * @author Push Technology Limited
 */
/*package*/ final class BoundTransformedMessageStreamBuilderImpl<V> implements BoundTransformedMessageStreamBuilder<V> {
    private final Transformer<Content, V> transformer;
    private final Session session;

    BoundTransformedMessageStreamBuilderImpl(Transformer<Content, V> transformer, Session session) {
        this.transformer = transformer;
        this.session = session;
    }

    @Override
    public <R> BoundTransformedMessageStreamBuilder<R> transform(Transformer<V, R> newTransformer) {
        return new BoundTransformedMessageStreamBuilderImpl<>(chain(transformer, newTransformer), session);
    }

    @Override
    public <R> BoundTransformedMessageStreamBuilder<R> transformWith(UnsafeTransformer<V, R> newTransformer) {
        return new BoundTransformedMessageStreamBuilderImpl<>(
            chain(transformer, toTransformer(newTransformer)),
            session);
    }

    @Override
    public MessageStreamHandle register(TransformedMessageStream<V> stream) {
        final Messaging.MessageStream adapter = new TransformedMessageStreamAdapter<>(transformer, stream);
        final Messaging messaging = session.feature(Messaging.class);
        final MessageStreamHandleImpl handle = new MessageStreamHandleImpl(messaging, adapter);
        messaging.addFallbackMessageStream(adapter);
        return handle;
    }
}