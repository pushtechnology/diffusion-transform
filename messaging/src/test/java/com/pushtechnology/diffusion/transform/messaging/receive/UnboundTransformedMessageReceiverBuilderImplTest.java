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

package com.pushtechnology.diffusion.transform.messaging.receive;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.content.Content;
import com.pushtechnology.diffusion.client.features.Messaging;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.transform.transformer.Transformer;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * Unit tests for {@link UnboundTransformedMessageReceiverBuilderImpl}.
 *
 * @author Push Technology Limited
 */
@SuppressWarnings("deprecation")
public final class UnboundTransformedMessageReceiverBuilderImplTest {
    @Mock
    private Messaging messaging;
    @Mock
    private MessagingControl messagingControl;
    @Mock
    private Session session;
    @Mock
    private Transformer<Content, String> contentTransformer;
    @Mock
    private Transformer<String, String> stringTransformer;
    @Mock
    private UnsafeTransformer<String, String> unsafeTransformer;
    @Mock
    private TransformedMessageStream<String> messageStream;
    @Mock
    private TransformedMessageHandler<String> messageHandler;

    @Before
    public void setUp() {
        initMocks(this);

        when(session.feature(Messaging.class)).thenReturn(messaging);
        when(session.feature(MessagingControl.class)).thenReturn(messagingControl);
    }

    @Test
    public void transform() {
        final UnboundTransformedMessageReceiverBuilder<String> builder =
            new UnboundTransformedMessageReceiverBuilderImpl<>(contentTransformer);
        final UnboundTransformedMessageReceiverBuilder<String> newBuilder = builder.transform(stringTransformer);

        assertNotSame(builder, newBuilder);
    }

    @Test
    public void transformWith() {
        final UnboundTransformedMessageReceiverBuilder<String> builder =
            new UnboundTransformedMessageReceiverBuilderImpl<>(contentTransformer);
        final UnboundTransformedMessageReceiverBuilder<String> newBuilder = builder.transformWith(unsafeTransformer);

        assertNotSame(builder, newBuilder);
    }

    @Test
    public void bind() throws Exception {
        final BoundTransformedMessageReceiverBuilder<String> builder =
            new UnboundTransformedMessageReceiverBuilderImpl<>(contentTransformer)
                .bind(session);

        assertNotNull(builder);
    }

    @Test
    public void register() {
        final UnboundTransformedMessageReceiverBuilder<String> builder =
            new UnboundTransformedMessageReceiverBuilderImpl<>(contentTransformer);
        builder.register(session, messageStream);

        verify(session).feature(Messaging.class);
        verify(messaging).addFallbackMessageStream(isA(Messaging.MessageStream.class));
    }

    @Test
    public void registerWithSelector() {
        final UnboundTransformedMessageReceiverBuilder<String> builder =
            new UnboundTransformedMessageReceiverBuilderImpl<>(contentTransformer);
        builder.register(session, "selector", messageStream);

        verify(session).feature(Messaging.class);
        verify(messaging).addMessageStream(eq("selector"), isA(Messaging.MessageStream.class));
    }

    @Test
    public void registerHandler() {
        final UnboundTransformedMessageReceiverBuilder<String> builder =
            new UnboundTransformedMessageReceiverBuilderImpl<>(contentTransformer);
        builder.register(session, "selector", messageHandler);

        verify(session).feature(MessagingControl.class);
        verify(messagingControl).addMessageHandler(eq("selector"), isA(MessagingControl.MessageHandler.class));
    }
}
