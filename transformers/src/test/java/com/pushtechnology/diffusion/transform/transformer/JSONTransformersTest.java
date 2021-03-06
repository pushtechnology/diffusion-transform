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

import static com.pushtechnology.diffusion.transform.transformer.JSONTransformers.JSON_TRANSFORMERS;
import static com.pushtechnology.diffusion.transform.transformer.JacksonContext.JACKSON_CONTEXT;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.stringify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.datatype.json.JSONDataType;

import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;

/**
 * Unit tests for {@link JSONTransformers}.
 *
 * @author Push Technology Limited
 */
public final class JSONTransformersTest {
    private static final JSONDataType JSON_DATA_TYPE = Diffusion.dataTypes().json();

    @Test
    public void toObject() throws Exception {
        final JSON json = JSON_DATA_TYPE.fromJsonString("{\"name\": \"a name\", \"someNumber\": 7}");
        final UnsafeTransformer<JSON, TestBean> transformer = JSON_TRANSFORMERS.toObject(TestBean.class);
        final TestBean asBean = transformer.transform(json);
        assertEquals(asBean.getName(), "a name");
        assertEquals(asBean.getSomeNumber(), 7);
    }

    @Test
    public void toObjectNull() throws Exception {
        final UnsafeTransformer<JSON, TestBean> transformer = JSON_TRANSFORMERS.toObject(TestBean.class);
        final TestBean asBean = transformer.transform(null);
        assertNull(asBean);
    }

    @Test
    public void toType() throws Exception {
        final JSON json = JSON_DATA_TYPE.fromJsonString("\"some pop culture reference\"");
        final UnsafeTransformer<JSON, String> transformer = JSON_TRANSFORMERS.toType(new TypeReference<String>() {});
        final String asString = transformer.transform(json);
        assertEquals(asString, "some pop culture reference");
    }

    @Test
    public void toTypeNull() throws Exception {
        final UnsafeTransformer<JSON, String> transformer = JSON_TRANSFORMERS.toType(new TypeReference<String>() {});
        final String asString = transformer.transform(null);
        assertNull(asString);
    }

    @Test
    public void toMapOf() throws Exception {
        final JSON json = JSON_DATA_TYPE.fromJsonString("{\"key\": \"value\"}");
        final UnsafeTransformer<JSON, Map<String, String>> transformer = JSON_TRANSFORMERS.toMapOf(String.class);
        final Map<String, String> asMap = transformer.transform(json);
        assertThat(asMap, new IsMapContaining<>(equalTo("key"), equalTo("value")));
    }

    @Test
    public void toMapOfNull() throws Exception {
        final UnsafeTransformer<JSON, Map<String, String>> transformer = JSON_TRANSFORMERS.toMapOf(String.class);
        final Map<String, String> asMap = transformer.transform(null);
        assertNull(asMap);
    }

    @Test
    public void fromPojo() throws Exception {
        final TestBean bean = new TestBean();
        bean.setName("a name");
        bean.setSomeNumber(7);

        final UnsafeTransformer<TestBean, JSON> transformer = JSON_TRANSFORMERS.fromPojo();
        final JSON json = transformer.transform(bean);
        final Map<String, ?> asMap = JACKSON_CONTEXT.toMap(json);
        assertThat(asMap, new IsMapContaining<>(equalTo("name"), CoreMatchers.<Object>equalTo("a name")));
        assertThat(asMap, new IsMapContaining<>(equalTo("someNumber"), CoreMatchers.<Object>equalTo(7)));
    }

    @Test
    public void fromPojoNull() throws Exception {
        final UnsafeTransformer<TestBean, JSON> transformer = JSON_TRANSFORMERS.fromPojo();
        final JSON json = transformer.transform(null);
        assertNull(json);
    }

    @Test
    public void fromMap() throws Exception {
        final Map<String, String> sourceMap = new HashMap<>();
        sourceMap.put("key", "value");
        final UnsafeTransformer<Map<String, String>, JSON> transformer = JSON_TRANSFORMERS.fromMap();
        final JSON json = transformer.transform(sourceMap);
        final Map<String, String> asMap = JACKSON_CONTEXT.toMapOf(json, String.class);
        assertThat(asMap, new IsMapContaining<>(equalTo("key"), equalTo("value")));
    }

    @Test
    public void fromMapNull() throws Exception {
        final UnsafeTransformer<Map<String, String>, JSON> transformer = JSON_TRANSFORMERS.fromMap();
        final JSON json = transformer.transform(null);
        assertNull(json);
    }

    @Test
    public void jsonString() throws Exception {
        final JSON serialisedValue = JSON_TRANSFORMERS.parseJSON().transform("{\"key\":\"value\"}");

        assertEquals(JSON_DATA_TYPE.fromJsonString("{\"key\":\"value\"}"), serialisedValue);
        final Map<String, String> map = Transformers.toMapOf(String.class).transform(serialisedValue);
        assertEquals("value", map.get("key"));
        assertEquals(1, map.size());

        final String deserialisedValue = stringify().transform(serialisedValue);
        assertEquals("{\"key\":\"value\"}", deserialisedValue);
    }

    @Test
    public void jsonStringNull() throws Exception {
        final JSON serialisedValue = JSON_TRANSFORMERS.parseJSON().transform(null);
        assertNull(serialisedValue);

        final String deserialisedValue = stringify().transform(null);
        assertNull(deserialisedValue);
    }

    @Test
    public void transformersFromBuilder() throws Exception {
        final JSONTransformers transformers = JSONTransformers
            .builder()
            .build();

        final JSON json = transformers.fromPojo().transform(new Date(0L));

        final Integer timestamp = transformers.toObject(Integer.class).transform(json);

        assertNotNull(timestamp);
        assertEquals(0, (int) timestamp);
    }
}
