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

package com.pushtechnology.diffusion.transform.messaging;

import com.pushtechnology.diffusion.client.callbacks.Stream;

/**
 * A stream of values received as messages.
 *
 * @param <V> the type of values
 * @author Push Technology Limited
 */
public interface MessageStream<V> extends Stream {

    /**
     * Notified when a message is received.
     *
     * @param path the path to which the message was sent
     * @param message the message
     */
    void onMessageReceived(String path, V message);
}
