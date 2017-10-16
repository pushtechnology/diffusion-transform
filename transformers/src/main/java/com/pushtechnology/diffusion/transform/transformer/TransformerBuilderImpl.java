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

package com.pushtechnology.diffusion.transform.transformer;

import static com.pushtechnology.diffusion.transform.transformer.Transformers.chain;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.toTransformer;

/**
 * Implementation of {@link TransformerBuilder}.
 *
 * @param <S> The type of source value accepted by the transformers this builds
 * @param <T> The type of target value returned by the transformers this builds
 * @author Push Technology Limited
 */
@SuppressWarnings("deprecation")
/*package*/ final class TransformerBuilderImpl<S, T> implements TransformerBuilder<S, T> {
    private final Transformer<S, T> transformer;

    /*package*/ TransformerBuilderImpl(Transformer<S, T> transformer) {
        this.transformer = transformer;
    }

    @Override
    public <R> TransformerBuilder<S, R> transform(Transformer<T, R> newTransformer) {
        return new TransformerBuilderImpl<>(chain(transformer, newTransformer));
    }

    @Override
    public <R> TransformerBuilder<S, R> transformWith(UnsafeTransformer<T, R> newTransformer) {
        return new TransformerBuilderImpl<>(chain(transformer, toTransformer(newTransformer)));
    }

    @Override
    public Transformer<S, T> build() {
        return transformer;
    }
}
