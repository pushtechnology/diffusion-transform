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

package com.pushtechnology.diffusion.transform.updater;

import java.util.function.Function;

/**
 * An extension to {@link SafeUpdaterBuilder} that is bound to a session.
 *
 * @param <S> The type of value understood by the topic
 * @param <T> The type of value updates are provided as
 * @author Push Technology Limited
 */
public interface BoundSafeUpdaterBuilder<S, T> extends
    SafeUpdaterBuilder<S, T>,
    BoundUpdaterBuilder<S, T, SafeTransformedUpdater<S, T>, SafeTransformedUpdateSource<S, T>> {

    @Override
    <R> BoundSafeUpdaterBuilder<S, R> transform(Function<R, T> newTransformer);

    @Override
    SafeTransformedUpdater<S, T> create();

    @Override
    UnboundSafeUpdaterBuilder<S, T> unbind();
}
