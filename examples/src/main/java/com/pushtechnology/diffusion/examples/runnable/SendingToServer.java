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

import static com.pushtechnology.diffusion.transform.messaging.send.RequestSenderBuilders.requestSenderBuilder;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.transform.messaging.send.RequestToHandlerSender;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;
import com.pushtechnology.diffusion.transform.transformer.Transformers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client that sends JSON messages to the server.
 *
 * @author Push Technology Limited
 */
public final class SendingToServer extends AbstractClient {
    private static final Logger LOG = LoggerFactory.getLogger(SendingToServer.class);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private volatile Future<?> updateTask;

    /**
     * Constructor.
     * @param url The URL to connect to
     * @param principal The principal to connect as
     */
    public SendingToServer(String url, String principal) {
        super(url, principal);
    }

    @Override
    public void onStarted(Session session) {
        final RequestToHandlerSender<RandomData, String> sender = requestSenderBuilder(JSON.class, String.class)
            .unsafeTransformRequest(Transformers.<RandomData>fromPojo())
            .bind(session)
            .buildToHandlerSender();

        updateTask = executor.scheduleAtFixedRate(
            () -> {
                try {
                    sender.sendRequest(
                        "json/random",
                        RandomData.next()).thenAccept(n -> LOG.info("Request sent and received"));
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
        final SendingToServer client =
            new SendingToServer("ws://diffusion.example.com:80", "auth");
        client.start("auth_secret");
        client.waitForStopped();
    }
    // CHECKSTYLE.ON: UncommentedMain
}
