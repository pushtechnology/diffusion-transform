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

import static com.pushtechnology.diffusion.client.Diffusion.dataTypes;
import static com.pushtechnology.diffusion.client.features.Topics.UnsubscribeReason.REQUESTED;
import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTING;
import static com.pushtechnology.diffusion.client.topics.details.TopicSpecification.TIME_SERIES_EVENT_VALUE_TYPE;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.TIME_SERIES;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.bigIntegerToBinary;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.binaryToBigInteger;
import static java.math.BigInteger.TEN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.TimeSeries.Event;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.features.Topics.ValueStream;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.transform.transformer.Transformers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.verification.VerificationWithTimeout;

/**
 * Integration test for Binary updaters.
 *
 * @author Push Technology Limited
 */
public final class BinaryUpdateIT {
    @Mock
    private Session.Listener listener;
    @Mock
    private ValueStream<Binary> stream;
    @Mock
    private ValueStream<Event<Binary>> timeSeriesStream;
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
    private ArgumentCaptor<Binary> valueCaptor;
    @Captor
    private ArgumentCaptor<Event<Binary>> eventCaptor;

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
    public void postConditions() throws ExecutionException, InterruptedException {
        session.feature(TopicControl.class).removeTopics("?test//").get();

        session.close();

        verify(listener, timed()).onSessionStateChanged(session, CONNECTED_ACTIVE, CLOSED_BY_CLIENT);

        verifyNoMoreInteractions(listener, stream, addCallback, removalCallback, completionCallback, updateCallback);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fallback() throws Exception {
        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(Binary.class, stream);

        topics.subscribe("?test/", completionCallback);
        verify(completionCallback, timed()).onComplete();

        final TopicControl topicControl = session.feature(TopicControl.class);
        topicControl.addTopic("test/topic", TopicType.BINARY, addCallback);
        verify(addCallback, timed()).onTopicAdded("test/topic");

        verify(stream, timed()).onSubscription(eq("test/topic"), specificationCaptor.capture());
        final TopicSpecification specification0 = specificationCaptor.getValue();
        assertEquals(TopicType.BINARY, specification0.getType());

        final TransformedUpdater<Binary, BigInteger> valueUpdater = UpdaterBuilders
            .binaryUpdaterBuilder()
            .unsafeTransform(Transformers.toTransformer(bigIntegerToBinary()))
            .create(session.feature(TopicUpdateControl.class).updater());

        valueUpdater.update("test/topic", TEN, updateCallback);
        verify(updateCallback, timed()).onSuccess();

        verify(stream, timed())
            .onValue(eq("test/topic"), specificationCaptor.capture(), isNull(Binary.class), valueCaptor.capture());
        final Binary value = valueCaptor.getValue();
        assertEquals(TEN, binaryToBigInteger().transform(value));

        topics.unsubscribe("?test//", completionCallback);
        verify(completionCallback, timed().times(2)).onComplete();

        verify(stream, timed()).onUnsubscription(eq("test/topic"), specificationCaptor.capture(), eq(REQUESTED));
        final TopicSpecification specification1 = specificationCaptor.getValue();
        assertEquals(specification0, specification1);

        topics.removeStream(stream);
        verify(stream, timed()).onClose();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void stream() throws Exception {
        final Topics topics = session.feature(Topics.class);
        topics.addStream("?test//", Binary.class, stream);

        topics.subscribe("?test//", completionCallback);
        verify(completionCallback, timed()).onComplete();

        final TopicControl topicControl = session.feature(TopicControl.class);
        topicControl.addTopic("test/topic", TopicType.BINARY, addCallback);
        verify(addCallback, timed()).onTopicAdded("test/topic");

        verify(stream, timed()).onSubscription(eq("test/topic"), specificationCaptor.capture());
        final TopicSpecification specification0 = specificationCaptor.getValue();
        assertEquals(TopicType.BINARY, specification0.getType());

        final TransformedUpdater<Binary, BigInteger> valueUpdater = UpdaterBuilders
            .binaryUpdaterBuilder()
            .unsafeTransform(Transformers.toTransformer(bigIntegerToBinary()))
            .create(session.feature(TopicUpdateControl.class).updater());

        valueUpdater.update("test/topic", TEN, updateCallback);
        verify(updateCallback, timed()).onSuccess();

        verify(stream, timed())
            .onValue(eq("test/topic"), specificationCaptor.capture(), isNull(Binary.class), valueCaptor.capture());
        final Binary value = valueCaptor.getValue();
        assertEquals(TEN, binaryToBigInteger().transform(value));

        topics.unsubscribe("?test//", completionCallback);
        verify(completionCallback, timed().times(2)).onComplete();

        verify(stream, timed()).onUnsubscription(eq("test/topic"), specificationCaptor.capture(), eq(REQUESTED));
        final TopicSpecification specification1 = specificationCaptor.getValue();
        assertEquals(specification0, specification1);

        topics.removeStream(stream);
        verify(stream, timed()).onClose();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void timeseries() throws ExecutionException, InterruptedException {
        final Topics topics = session.feature(Topics.class);
        topics.addTimeSeriesStream("?test//", Binary.class, timeSeriesStream);

        topics.subscribe("?test//").get();

        final TopicControl topicControl = session.feature(TopicControl.class);
        topicControl.addTopic(
            "test/topic",
            topicControl
                .newSpecification(TIME_SERIES)
                .withProperty(TIME_SERIES_EVENT_VALUE_TYPE, dataTypes().binary().getTypeName()))
            .get();

        verify(timeSeriesStream, timed()).onSubscription(eq("test/topic"), specificationCaptor.capture());
        final TopicSpecification specification0 = specificationCaptor.getValue();
        assertEquals(TopicType.TIME_SERIES, specification0.getType());

        final TimeSeriesUpdater<BigInteger> valueUpdater = UpdaterBuilders
            .binaryUpdaterBuilder()
            .unsafeTransform(Transformers.toTransformer(bigIntegerToBinary()))
            .createTimeSeries(session);

        valueUpdater.append("test/topic", TEN).get();

        verify(timeSeriesStream, timed()).onValue(
            eq("test/topic"),
            specificationCaptor.capture(),
            isNull(Event.class),
            eventCaptor.capture());

        assertEquals(bigIntegerToBinary().apply(TEN), eventCaptor.getValue().value());

        topics.unsubscribe("?test//").get();

        verify(timeSeriesStream, timed()).onUnsubscription(eq("test/topic"), specificationCaptor.capture(), eq(REQUESTED));

        topics.removeStream(timeSeriesStream);
        verify(timeSeriesStream, timed()).onClose();
    }

    private VerificationWithTimeout timed() {
        return timeout(5000L);
    }
}
