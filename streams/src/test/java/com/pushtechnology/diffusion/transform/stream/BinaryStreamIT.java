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

package com.pushtechnology.diffusion.transform.stream;

import static com.pushtechnology.diffusion.client.Diffusion.dataTypes;
import static com.pushtechnology.diffusion.client.features.Topics.UnsubscribeReason.REQUESTED;
import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTING;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.bigIntegerToBinary;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.binaryToBigInteger;
import static java.math.BigInteger.TEN;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.TimeSeries;
import com.pushtechnology.diffusion.client.features.TimeSeries.Event;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.verification.VerificationWithTimeout;

/**
 * Integration test for JSON streams.
 * @author Push Technology Limited
 */
public final class BinaryStreamIT {
    @Mock
    private Session.Listener listener;
    @Mock
    private TransformedStream<Binary, BigInteger> stream;
    @Mock
    private TransformedStream<Event<Binary>, Event<BigInteger>> timeSeriesStream;
    @Mock
    private TopicControl.AddCallback addCallback;
    @Mock
    private TopicControl.RemovalCallback removalCallback;
    @Mock
    private Topics.CompletionCallback completionCallback;
    @Mock
    private TopicUpdateControl.Updater.UpdateCallback updateCallback;
    @Captor
    private ArgumentCaptor<TopicSpecification> specificationCaptor;
    @Captor
    private ArgumentCaptor<BigInteger> valueCaptor;
    @Captor
    private ArgumentCaptor<Event<BigInteger>> eventCaptor;

    private Session session;

    @Before
    public void setUp() {
        initMocks(this);
        session = Diffusion
            .sessions()
            .listener(listener)
            .principal("control")
            .password("password")
            .open("ws://localhost:8080");
        verify(listener, timed()).onSessionStateChanged(session, CONNECTING, CONNECTED_ACTIVE);
    }

    @After
    public void postConditions() {
        session.feature(TopicControl.class).remove("?test//", removalCallback);
        verify(removalCallback, timed()).onTopicsRemoved();

        session.close();

        verify(listener, timed()).onSessionStateChanged(session, CONNECTED_ACTIVE, CLOSED_BY_CLIENT);

        verifyNoMoreInteractions(listener, stream, addCallback, removalCallback, completionCallback, updateCallback);
    }

    @Test
    public void fallback() throws TransformationException {
        final Topics topics = session.feature(Topics.class);
        final StreamHandle streamHandle = StreamBuilders
            .newBinaryStreamBuilder()
            .unsafeTransform(binaryToBigInteger())
            .createFallback(topics, stream);

        topics.subscribe("?test//", completionCallback);
        verify(completionCallback, timed()).onComplete();

        final TopicControl topicControl = session.feature(TopicControl.class);
        topicControl.addTopic("test/topic", TopicType.BINARY, addCallback);
        verify(addCallback, timed()).onTopicAdded("test/topic");

        verify(stream, timed()).onSubscription(eq("test/topic"), specificationCaptor.capture());
        final TopicSpecification specification0 = specificationCaptor.getValue();
        assertEquals(TopicType.BINARY, specification0.getType());

        final TopicUpdateControl.ValueUpdater<Binary> updater = session
            .feature(TopicUpdateControl.class)
            .updater()
            .valueUpdater(Binary.class);

        updater.update("test/topic", bigIntegerToBinary().apply(TEN), updateCallback);
        verify(updateCallback, timed()).onSuccess();

        verify(stream, timed())
            .onValue(eq("test/topic"), specificationCaptor.capture(), isNull(BigInteger.class), valueCaptor.capture());
        final BigInteger value = valueCaptor.getValue();
        assertEquals(TEN, value);

        topics.unsubscribe("?test//", completionCallback);
        verify(completionCallback, timed().times(2)).onComplete();

        verify(stream, timed()).onUnsubscription(eq("test/topic"), specificationCaptor.capture(), eq(REQUESTED));
        final TopicSpecification specification1 = specificationCaptor.getValue();
        assertEquals(specification0, specification1);

        streamHandle.close();
        verify(stream, timed()).onClose();
    }

    @Test
    public void stream() throws TransformationException {
        final Topics topics = session.feature(Topics.class);
        final StreamHandle streamHandle = StreamBuilders
            .newBinaryStreamBuilder()
            .unsafeTransform(binaryToBigInteger())
            .register(topics, "?test//", stream);

        topics.subscribe("?test//", completionCallback);
        verify(completionCallback, timed()).onComplete();

        final TopicControl topicControl = session.feature(TopicControl.class);
        topicControl.addTopic("test/topic", TopicType.BINARY, addCallback);
        verify(addCallback, timed()).onTopicAdded("test/topic");

        verify(stream, timed()).onSubscription(eq("test/topic"), specificationCaptor.capture());
        final TopicSpecification specification0 = specificationCaptor.getValue();
        assertEquals(TopicType.BINARY, specification0.getType());

        final TopicUpdateControl.ValueUpdater<Binary> updater = session
            .feature(TopicUpdateControl.class)
            .updater()
            .valueUpdater(Binary.class);

        updater.update("test/topic", bigIntegerToBinary().apply(TEN), updateCallback);
        verify(updateCallback, timed()).onSuccess();

        verify(stream, timed())
            .onValue(eq("test/topic"), specificationCaptor.capture(), isNull(BigInteger.class), valueCaptor.capture());
        final BigInteger value = valueCaptor.getValue();
        assertEquals(TEN, value);

        topics.unsubscribe("?test//", completionCallback);
        verify(completionCallback, timed().times(2)).onComplete();

        verify(stream, timed()).onUnsubscription(eq("test/topic"), specificationCaptor.capture(), eq(REQUESTED));
        final TopicSpecification specification1 = specificationCaptor.getValue();
        assertEquals(specification0, specification1);

        streamHandle.close();
        verify(stream, timed()).onClose();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void timeseries() throws InterruptedException, ExecutionException, TimeoutException {
        final Topics topics = session.feature(Topics.class);
        final StreamHandle streamHandle = StreamBuilders
            .newBinaryStreamBuilder()
            .unsafeTransform(binaryToBigInteger())
            .createTimeSeries(session, "?test//", timeSeriesStream);

        topics.subscribe("?test//", completionCallback);
        verify(completionCallback, timed()).onComplete();

        final TopicControl topicControl = session.feature(TopicControl.class);
        final TopicSpecification specification = topicControl
            .newSpecification(TopicType.TIME_SERIES)
            .withProperty(TopicSpecification.TIME_SERIES_EVENT_VALUE_TYPE, dataTypes().binary().getTypeName());
        topicControl.addTopic("test/topic",
                              specification).get(5, SECONDS);

        verify(timeSeriesStream, timed()).onSubscription(eq("test/topic"), specificationCaptor.capture());
        final TopicSpecification specification0 = specificationCaptor.getValue();
        assertEquals(specification, specification0);

        session
            .feature(TimeSeries.class)
            .append("test/topic", Binary.class, bigIntegerToBinary().apply(TEN)).get(5, SECONDS);

        verify(timeSeriesStream, timed())
            .onValue(eq("test/topic"), specificationCaptor.capture(), isNull(Event.class), eventCaptor.capture());
        final Event<BigInteger> value = eventCaptor.getValue();
        assertEquals(TEN, value.value());

        topics.unsubscribe("?test//", completionCallback);
        verify(completionCallback, timed().times(2)).onComplete();

        verify(timeSeriesStream, timed()).onUnsubscription(eq("test/topic"), specificationCaptor.capture(), eq(REQUESTED));
        final TopicSpecification specification1 = specificationCaptor.getValue();
        assertEquals(specification0, specification1);

        streamHandle.close();
        verify(timeSeriesStream, timed()).onClose();
    }

    private VerificationWithTimeout timed() {
        return timeout(5000L);
    }
}
