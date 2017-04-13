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

import com.pushtechnology.diffusion.client.features.Messaging;

/**
 * Implementation of {@link MessageStreamHandle}.
 *
 * @author Push Technology Limited
 */
/*package*/ class MessageStreamHandleImpl implements MessageStreamHandle {
    private final Messaging messaging;
    private final Messaging.MessageStream stream;

    MessageStreamHandleImpl(Messaging messaging, Messaging.MessageStream stream) {
        this.messaging = messaging;
        this.stream = stream;
    }

    @Override
    public void close() {
        messaging.removeMessageStream(stream);
    }
}