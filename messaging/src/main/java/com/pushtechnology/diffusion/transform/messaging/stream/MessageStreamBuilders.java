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

package com.pushtechnology.diffusion.transform.messaging.stream;

import com.pushtechnology.diffusion.client.content.Content;
import com.pushtechnology.diffusion.transform.transformer.Transformers;

/**
 * Factory for creating instances of {@link MessageStream}s.
 *
 * @author Matt Champion 12/04/2017
 */
public final class MessageStreamBuilders {
    private MessageStreamBuilders() {
    }

    /**
     * Create a {@link UnboundSafeMessageStreamBuilder} from a {@link Content} source.
     *
     * @return the message stream builder
     */
    public static UnboundSafeMessageStreamBuilder<Content> newMessageStreamBuilder() {
        return new UnboundSafeMessageStreamBuilderImpl<>(Transformers.<Content>identity());
    }
}
