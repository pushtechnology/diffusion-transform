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

package com.pushtechnology.diffusion.examples.runnable;

import static com.pushtechnology.diffusion.client.topics.details.TopicType.BINARY;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.byteArrayToBinary;
import static com.pushtechnology.diffusion.transform.updater.UpdaterBuilders.updaterBuilder;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.transform.updater.SafeTransformedUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A client that creates and updates Binary topics.
 *
 * @author Push Technology Limited
 */
public final class ProducingBinary extends AbstractClient {
    private static final Logger LOG = LoggerFactory.getLogger(ProducingBinary.class);
    private static final Function<RandomData, Binary> SERIALISER = new Function<RandomData, ByteBuffer>() {
        @Override
        public ByteBuffer apply(RandomData value) {
            final ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putInt(value.getId());
            buffer.putLong(value.getTimestamp());
            buffer.putInt(value.getRandomInt());
            return buffer;
        }
    }
        .andThen(ByteBuffer::array)
        .andThen(byteArrayToBinary());
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private volatile Future<?> updateTask;

    /**
     * Constructor.
     * @param url The URL to connect to
     * @param principal The principal to connect as
     */
    public ProducingBinary(String url, String principal) {
        super(url, principal);
    }

    @Override
    public void onConnected(Session session) {
        final TopicControl topicControl = session.feature(TopicControl.class);
        topicControl
            .addTopic("binary/random", BINARY)
            .thenAccept(result -> beginUpdating(session))
            .exceptionally(ex -> {
                LOG.error("Failed to add topic binary/random", ex);
                return null;
            });
    }

    private void beginUpdating(Session session) {
        LOG.debug("Begin updating topic");

        final TopicUpdateControl.Updater updater = session
            .feature(TopicUpdateControl.class)
            .updater();

        // Create a one-way transforming value updater that cannot be used to lookup cached values
        final SafeTransformedUpdater<Binary, RandomData> valueUpdater = updaterBuilder(Binary.class)
            .transform(SERIALISER)
            .create(updater);

        updateTask = EXECUTOR.scheduleAtFixedRate(
            () -> {
                valueUpdater.update(
                    "binary/random",
                    RandomData.next(),
                    new TopicUpdateControl.Updater.UpdateCallback.Default());
            },
            0L,
            1L,
            SECONDS);
    }

    @Override
    public void onDisconnected() {
        updateTask.cancel(false);
    }

    @Override
    public void onError(ErrorReason errorReason) {
        LOG.error("Failed to start client: {}", errorReason);
    }

    /**
     * Entry point for the example.
     * @param args The command line arguments
     * @throws InterruptedException If the main thread was interrupted
     */
    // CHECKSTYLE.OFF: UncommentedMain
    public static void main(String[] args) throws InterruptedException {
        final ProducingBinary client =
            new ProducingBinary("ws://diffusion.example.com:80", "auth");
        client.start("auth_secret");
        client.waitForStopped();
    }
    // CHECKSTYLE.ON: UncommentedMain
}
