
# StreamBuilders

To help with receiving values from topics a `StreamBuilder` can be used to construct a stream that delegates to a
`TransformedStream`. The `StreamBuilder` makes it easy to chain transformers together. A `StreamBuilder` is
immutable and allows a new `StreamBuilder` to be created from it that adds an additional transformation.

A `TransformedStream` adds additional error handling capabilities to a `ValueStream` to handle a
`TransformationException`. Using only `SafeTransformer` this additional error handling can be avoided.

Multiple transformations can be chained together to read a `JSON` value and extract part of it.

```java
StreamBuilders.newJsonStreamBuilder()
    .transform(Transformers.toMapOf(BigInteger.class))
    .transform(Transformers.project("timestamp"))
    .create(topics, "json/random", new TransformedStream.Default<JSON, BigInteger>() {
        @Override
        public void onValue(
            String topicPath,
            TopicSpecification topicSpecification,
            BigInteger oldValue,
            BigInteger newValue) {

            LOG.info("New timestamp {}", newValue);
        }
});
```

A `StreamBuilder` can also be used to create a fallback stream. A fallback stream receives notifications for any topics
that do not match the topic selector of any other stream.

A `StreamBuilder` returns a `StreamHandle` when it creates a stream or fallback stream. This can be used to close the
stream when it is no longer needed. Since the stream provided when registering a stream using the `StreamBuilder` is
wrapped in an adapter before it is passed to the Diffusion API the `Topics` feature cannot be used to close the stream.