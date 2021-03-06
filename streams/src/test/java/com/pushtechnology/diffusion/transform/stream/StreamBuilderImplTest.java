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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.function.Function;

import com.pushtechnology.diffusion.client.features.TimeSeries.Event;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.TopicSelector;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;
import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.timeseries.event.EventImpl;
import com.pushtechnology.diffusion.timeseries.event.EventMetadataImpl;
import com.pushtechnology.diffusion.transform.transformer.Transformers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Unit tests for {@link StreamBuilderImpl}.
 *
 * @author Push Technology Limited
 */
public final class StreamBuilderImplTest {
    @Mock
    private Session session;
    @Mock
    private Topics topics;
    @Mock
    private TopicSelector selector;
    @Mock
    private TransformedStream<String, String> stream;
    @Mock
    private TransformedStream<JSON, JSON> jsonStream;
    @Mock
    private TransformedStream<Event<JSON>, Event<JSON>> timeseriesStream;
    @Mock
    private JSON json;
    @Captor
    private ArgumentCaptor<Topics.ValueStream<Event<JSON>>> streamCaptor;
    @Captor
    private ArgumentCaptor<Event<JSON>> eventCaptor;

    @Before
    public void setUp() {
        initMocks(this);

        when(session.feature(Topics.class)).thenReturn(topics);
    }

    @Test
    public void unsafeTransform() {
        final StreamBuilder<
                String,
                String,
                TransformedStream<String, String>,
                TransformedStream<Event<String>, Event<String>>> streamBuilder =
            new StreamBuilderImpl<>(String.class, Transformers.toTransformer(Function.identity()));

        final StreamBuilder<String, String, TransformedStream<String, String>, TransformedStream<Event<String>, Event<String>>> transformedStreamBuilder =
            streamBuilder.unsafeTransform(Transformers.toTransformer(Function.identity()));

        assertTrue(transformedStreamBuilder instanceof StreamBuilderImpl);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createPath() {
        final StreamBuilder<String, String, TransformedStream<String, String>, TransformedStream<Event<String>, Event<String>>> streamBuilder =
            new StreamBuilderImpl<>(String.class, Transformers.toTransformer(Function.identity()));
        streamBuilder.register(topics, "path", stream);

        verify(topics).addStream(eq("path"), eq(String.class), isA(StreamAdapter.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSelector() {
        final StreamBuilder<String, String, TransformedStream<String, String>, TransformedStream<Event<String>, Event<String>>> streamBuilder =
            new StreamBuilderImpl<>(String.class, Transformers.toTransformer(Function.identity()));
        streamBuilder.register(topics, selector, stream);

        verify(topics).addStream(eq(selector), eq(String.class), isA(StreamAdapter.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createFallback() {
        final StreamBuilder<
                JSON,
                JSON,
                TransformedStream<JSON, JSON>,
                TransformedStream<Event<JSON>, Event<JSON>>> streamBuilder =
            new StreamBuilderImpl<>(JSON.class, Transformers.toTransformer(Function.identity()));
        streamBuilder.createFallback(topics, jsonStream);

        verify(topics).addFallbackStream(eq(JSON.class), isA(Topics.ValueStream.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createPathWithSession() {
        final StreamBuilder<String, String, TransformedStream<String, String>, TransformedStream<Event<String>, Event<String>>> streamBuilder =
            new StreamBuilderImpl<>(String.class, Transformers.toTransformer(Function.identity()));
        streamBuilder.register(session, "path", stream);

        verify(topics).addStream(eq("path"), eq(String.class), isA(StreamAdapter.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSelectorWithSession() {
        final StreamBuilder<String, String, TransformedStream<String, String>, TransformedStream<Event<String>, Event<String>>> streamBuilder =
            new StreamBuilderImpl<>(String.class, Transformers.toTransformer(Function.identity()));
        streamBuilder.register(session, selector, stream);

        verify(topics).addStream(eq(selector), eq(String.class), isA(StreamAdapter.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createFallbackWithSession() {
        final StreamBuilder<JSON, JSON, TransformedStream<JSON, JSON>, TransformedStream<Event<JSON>, Event<JSON>>> streamBuilder =
            new StreamBuilderImpl<>(JSON.class, Transformers.toTransformer(Function.identity()));
        streamBuilder.createFallback(session, jsonStream);

        verify(topics).addFallbackStream(eq(JSON.class), isA(Topics.ValueStream.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createTimeSeriesWithSession() {
        final StreamBuilder<JSON, JSON, TransformedStream<JSON, JSON>, TransformedStream<Event<JSON>, Event<JSON>>> streamBuilder =
            new StreamBuilderImpl<>(JSON.class, Transformers.toTransformer(Function.identity()));
        streamBuilder.createTimeSeries(session, "path", timeseriesStream);

        verify(topics).addTimeSeriesStream(eq("path"), eq(JSON.class), streamCaptor.capture());

        final EventMetadataImpl metadata0 = new EventMetadataImpl(0, 0, "author");
        streamCaptor
            .getValue()
            .onValue("path", null, null, EventImpl.createEvent(metadata0, metadata0, json));

        verify(timeseriesStream)
            .onValue(
                eq("path"),
                isNull(TopicSpecification.class),
                isNull(Event.class),
                eventCaptor.capture());

        final Event<JSON> event0 = eventCaptor.getValue();
        assertFalse(event0.isEditEvent());
        assertEquals(0, event0.sequence());
        assertEquals(0, event0.timestamp());
        assertEquals("author", event0.author());

        final EventMetadataImpl metadata1 = new EventMetadataImpl(1, 1, "author");
        streamCaptor
            .getValue()
            .onValue("path", null, null, EventImpl.createEvent(metadata1, metadata0, json));

        verify(timeseriesStream)
            .onValue(
                eq("path"),
                isNull(TopicSpecification.class),
                isNotNull(Event.class),
                eventCaptor.capture());

        final Event<JSON> event1 = eventCaptor.getValue();
        assertTrue(event1.isEditEvent());
        assertEquals(1, event1.sequence());
        assertEquals(1, event1.timestamp());
        assertEquals("author", event1.author());
        assertEquals(metadata0, event1.originalEvent());
    }
}
