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

package com.pushtechnology.diffusion.transform.messaging.send.tohandler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.diffusion.client.features.Messaging;
import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.transform.transformer.SafeTransformer;
import com.pushtechnology.diffusion.transform.transformer.Transformer;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * Unit tests for {@link BoundSafeMessageSenderBuilderImpl}.
 *
 * @author Matt Champion 11/04/2017
 */
public final class BoundSafeMessageSenderBuilderImplTest {
    @Mock
    private SafeTransformer<String, JSON> transformer;
    @Mock
    private Transformer<String, String> stringTransformer;
    @Mock
    private SafeTransformer<String, String> safeTransformer;
    @Mock
    private UnsafeTransformer<String, String> unsafeTransformer;
    @Mock
    private Messaging messaging;
    @Mock
    private JSON json;
    @Mock
    private Messaging.SendCallback sendCallback;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(transformer.transform("value")).thenReturn(json);
        when(stringTransformer.transform("value")).thenReturn("value");
        when(safeTransformer.transform("value")).thenReturn("value");
        when(unsafeTransformer.transform("value")).thenReturn("value");
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(transformer, unsafeTransformer, safeTransformer, messaging, json, sendCallback);
    }

    @Test
    public void transformAndSend() throws Exception {
        final BoundTransformedMessageSenderBuilder<String> builder =
            new BoundSafeMessageSenderBuilderImpl<>(messaging, transformer).transform(stringTransformer);

        final MessageSender<String> sender = builder.build();
        sender.send("path", "value", sendCallback);

        verify(transformer).transform("value");
        verify(stringTransformer).transform("value");
        verify(messaging).send("path", json, sendCallback);
    }

    @Test
    public void transformWithAndSend() throws Exception {
        final BoundTransformedMessageSenderBuilder<String> builder =
            new BoundSafeMessageSenderBuilderImpl<>(messaging, transformer).transformWith(unsafeTransformer);

        final MessageSender<String> sender = builder.build();
        sender.send("path", "value", sendCallback);

        verify(transformer).transform("value");
        verify(unsafeTransformer).transform("value");
        verify(messaging).send("path", json, sendCallback);
    }

    @Test
    public void safeTransformAndSend() throws Exception {
        final BoundSafeMessageSenderBuilder<String> builder =
            new BoundSafeMessageSenderBuilderImpl<>(messaging, transformer).transform(safeTransformer);

        final MessageSender<String> sender = builder.build();
        sender.send("path", "value", sendCallback);

        verify(transformer).transform("value");
        verify(safeTransformer).transform("value");
        verify(messaging).send("path", json, sendCallback);
    }
}