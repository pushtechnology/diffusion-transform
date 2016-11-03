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

import java.nio.charset.Charset;

import com.pushtechnology.diffusion.datatype.binary.Binary;

/**
 * Transformer from {@link Binary} to {@link String}.
 *
 * @author Push Technology Limited
 */
/*package*/ final class BinaryToStringTransformer implements Transformer<Binary, String> {
    private final  Charset charset;

    /**
     * Constructor.
     */
    BinaryToStringTransformer(Charset charset) {
        this.charset = charset;
    }

    @Override
    public String transform(Binary value) throws TransformationException {
        return new String(value.toByteArray(), charset);
    }
}