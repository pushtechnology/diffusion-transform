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

package com.pushtechnology.diffusion.examples.runnable;

import static com.pushtechnology.diffusion.client.topics.details.TopicType.JSON;
import static com.pushtechnology.diffusion.transform.updater.UpdaterBuilders.updaterBuilder;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;
import com.pushtechnology.diffusion.transform.transformer.Transformers;
import com.pushtechnology.diffusion.transform.updater.TransformedUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client that creates and updates JSON topics.
 *
 * @author Push Technology Limited
 */
public final class ProducingJson extends AbstractClient {
    private static final Logger LOG = LoggerFactory.getLogger(ProducingJson.class);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private volatile Future<?> updateTask;

    /**
     * Constructor.
     * @param url The URL to connect to
     * @param principal The principal to connect as
     */
    public ProducingJson(String url, String principal) {
        super(url, principal);
    }

    @Override
    public void onConnected(Session session) {
        final TopicControl topicControl = session.feature(TopicControl.class);
        topicControl
            .addTopic("json/random", JSON)
            .thenAccept(result -> beginUpdating(session))
            .exceptionally(ex -> {
                LOG.error("Failed to add topic json/random", ex);
                return null;
            });
    }

    private void beginUpdating(Session session) {
        final TopicUpdateControl.Updater updater = session
            .feature(TopicUpdateControl.class)
            .updater();

        final TransformedUpdater<JSON, RandomData> valueUpdater = updaterBuilder(JSON.class)
            .unsafeTransform(Transformers.<RandomData>fromPojo())
            .create(updater);

        updateTask = executor.scheduleAtFixedRate(
            () -> {
                try {
                    valueUpdater.update(
                        "json/random",
                        RandomData.next(),
                        new TopicUpdateControl.Updater.UpdateCallback.Default());
                }
                catch (TransformationException e) {
                    LOG.warn("Failed to transform data", e);
                }
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
        final ProducingJson client =
            new ProducingJson("ws://diffusion.example.com:80", "auth");
        client.start("auth_secret");
        client.waitForStopped();
    }
    // CHECKSTYLE.ON: UncommentedMain
}
