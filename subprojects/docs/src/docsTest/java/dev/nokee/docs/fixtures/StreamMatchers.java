/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.docs.fixtures;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public final class StreamMatchers {
	private StreamMatchers() {}

    /**
     * Matcher for a stream which yields no elements.
     *
     * @param <T> The type of items
     * @param <S> The type of the BaseStream
     */
    public static <T,S extends BaseStream<T, ? extends S>> Matcher<S> yieldsNothing() {
        return new TypeSafeMatcher<S>() {

            private Iterator<T> actualIterator;

            @Override
            protected boolean matchesSafely(S actual) {
                actualIterator = actual.iterator();
                return !actualIterator.hasNext();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("A Stream yielding no elements");
            }

            @Override
            protected void describeMismatchSafely(S item, Description description) {
                description.appendText("the Stream started with ").appendValue(actualIterator.next());
                if (actualIterator.hasNext()) {
                    description.appendText(" and will yield even more elements");
                } else {
                    description.appendText(" and is then exhausted");
                }
            }
        };
    }

    /**
     * Matcher for an empty stream.
     *
     * @param <T> The type of items
     * @param <S> The type of the BaseStream
     *
     * @deprecated name clashes with {@link org.hamcrest.Matchers#empty()},
     *             use {@link #yieldsNothing()} instead
     */
    @Deprecated
    public static <T,S extends BaseStream<T, ? extends S>> Matcher<S> empty() {
        return yieldsNothing();
    }

    /**
     * A matcher for a finite Stream producing the same number of items as the expected Stream,
     * and producing equal items as expected in the same order.
     *
     * For infinite Streams use {@link #startsWith}
     *
     * @param expected A BaseStream against which to compare
     * @param <T> The type of items produced by each BaseStream
     * @param <S> The type of BaseStream
     * @see #startsWith
     * @see #startsWithInt
     * @see #startsWithLong
     * @see #startsWithDouble
     */
    public static <T,S extends BaseStream<T,? extends S>> Matcher<S> yieldsSameAs(S expected) {
        return new BaseStreamMatcher<T,S>() {
            @Override
            protected boolean matchesSafely(S actual) {
                return remainingItemsEqual(expected.iterator(), actual.iterator());
            }
        };
    }

    /**
     * A matcher for a finite Stream producing the same number of items as the expected Stream,
     * and producing equal items as expected in the same order.
     *
     * @param expected A BaseStream against which to compare
     * @param <T> The type of items produced by each BaseStream
     * @param <S> The type of BaseStream
     *
     * @deprecated name clashes with {@link org.hamcrest.Matchers#equalTo(Object)},
     *             use {@link #yieldsSameAs(BaseStream)} instead
     */
    @Deprecated
    public static <T,S extends BaseStream<T,? extends S>> Matcher<S> equalTo(S expected) {
        return yieldsSameAs(expected);
    }

    /**
     * A matcher for potentially infinite Streams of objects where the first limit items from each must be
     * equal
     *
     * @param expected A Stream to check against
     * @param limit Only check this number of items from actual Stream
     * @param <T> The type of items produced by each Stream
     * @see #equalTo
     * @see #startsWithInt
     * @see #startsWithLong
     * @see #startsWithDouble
     */
    public static <T> Matcher<Stream<T>> startsWith(Stream<T> expected, long limit) {
        return new BaseStreamMatcher<T,Stream<T>>() {
            @Override
            protected boolean matchesSafely(Stream<T> actual) {
                return remainingItemsEqual(expected.limit(limit).iterator(), actual.limit(limit).iterator());
            }
        };
    }

    /**
     * A matcher for potentially infinite Streams of primitive doubles where the first limit items from each must be
     * equal
     *
     * @param expected A Stream to check against
     * @param limit Only check this number of items from actual Stream
     * @see #equalTo
     * @see #startsWith
     * @see #startsWithInt
     * @see #startsWithLong
     */
    public static Matcher<DoubleStream> startsWith(DoubleStream expected, long limit) {
        return new BaseStreamMatcher<Double,DoubleStream>() {
            @Override
            protected boolean matchesSafely(DoubleStream actual) {
                return remainingItemsEqual(expected.limit(limit).iterator(), actual.limit(limit).iterator());
            }
        };
    }

    /**
     * A matcher for potentially infinite Streams of primitive ints where the first limit items from each must be
     * equal
     *
     * @param expected A Stream to check against
     * @param limit Only check this number of items from actual Stream
     * @see #equalTo
     * @see #startsWith
     * @see #startsWithLong
     * @see #startsWithDouble
     */
    public static Matcher<IntStream> startsWith(IntStream expected, long limit) {
        return new BaseStreamMatcher<Integer,IntStream>() {
            @Override
            protected boolean matchesSafely(IntStream actual) {
                return remainingItemsEqual(expected.limit(limit).iterator(), actual.limit(limit).iterator());
            }
        };
    }

    /**
     * A matcher for potentially infinite Streams of primitive ints where the first limit items from each must be
     * equal
     *
     * @param expected A Stream to check against
     * @param limit Only check this number of items from actual Stream
     * @see #equalTo
     * @see #startsWith
     * @see #startsWithInt
     * @see #startsWithDouble
     */
    public static Matcher<LongStream> startsWith(LongStream expected, long limit) {
        return new BaseStreamMatcher<Long,LongStream>() {
            @Override
            protected boolean matchesSafely(LongStream actual) {
                return remainingItemsEqual(expected.limit(limit).iterator(), actual.limit(limit).iterator());
            }
        };
    }

    private static void describeToStartsAllWith(Description description, long limit, Matcher<?> matcher) {
        description
                .appendText("First ")
                .appendText(Long.toString(limit))
                .appendText(" to match ")
                .appendValue(matcher);
    }

    /**
     * A matcher for potentially infinite Streams of objects, the first limit of which must match the given Matcher
     *
     * @param matcher A matcher to apply to items produced from the Stream
     * @param limit Only check this number of items from the Stream
     * @see #allMatch
     * @see #startsWithAllLong
     * @see #startsWithAllInt
     * @see #startsWithAllDouble
     */
    public static <T> Matcher<Stream<T>> startsWithAll(Matcher<T> matcher, long limit) {
        return new StreamAllMatches<T>(matcher) {
            @Override
            protected boolean matchesSafely(Stream<T> actual) {
                return super.matchesSafely(actual.limit(limit));
            }

            @Override
            public void describeTo(Description description) {
                describeToStartsAllWith(description, limit, matcher);
            }
        };

    }

    /**
     * A matcher for potentially infinite Streams of primitive longs, the first limit of which must match the given Matcher
     *
     * @param matcher A matcher to apply to items produced from the Stream
     * @param limit Only check this number of items from the Stream
     * @see #allMatchLong
     * @see #startsWithAll
     * @see #startsWithAllInt
     * @see #startsWithAllDouble
     */
    public static Matcher<LongStream> startsWithAllLong(Matcher<Long> matcher, long limit) {
        return new LongStreamAllMatches(matcher) {
            @Override
            protected boolean matchesSafely(LongStream actual) {
                return super.matchesSafely(actual.limit(limit));
            }

            @Override
            public void describeTo(Description description) {
                describeToStartsAllWith(description, limit, matcher);
            }
        };
    }

    /**
     * A matcher for potentially infinite Streams of primitive ints, the first limit of which must match the given Matcher
     *
     * @param matcher A matcher to apply to items produced from the Stream
     * @param limit Only check this number of items from the Stream
     * @see #allMatchInt
     * @see #startsWithAll
     * @see #startsWithAllLong
     * @see #startsWithAllDouble
     */
    public static Matcher<IntStream> startsWithAllInt(Matcher<Integer> matcher, long limit) {
        return new IntStreamAllMatches(matcher) {
            @Override
            protected boolean matchesSafely(IntStream actual) {
                return super.matchesSafely(actual.limit(limit));
            }

            @Override
            public void describeTo(Description description) {
                describeToStartsAllWith(description, limit, matcher);
            }
        };
    }

    /**
     * A matcher for potentially infinite Streams of primitive doubles, the first limit of which must match the given Matcher
     *
     * @param matcher A matcher to apply to items produced from the Stream
     * @param limit Only check this number of items from the Stream
     * @see #allMatchDouble
     * @see #startsWithAll
     * @see #startsWithAllInt
     * @see #startsWithAllLong
     */
    public static Matcher<DoubleStream> startsWithAllDouble(Matcher<Double> matcher, long limit) {
        return new DoubleStreamAllMatches(matcher) {
            @Override
            protected boolean matchesSafely(DoubleStream actual) {
                return super.matchesSafely(actual.limit(limit));
            }

            @Override
            public void describeTo(Description description) {
                describeToStartsAllWith(description, limit, matcher);
            }
        };
    }

    private static void describeToStartsAnyWith(Description description, long limit, Matcher<?> matcher) {
        description
                .appendText("Any of first ")
                .appendText(Long.toString(limit))
                .appendText(" to match ")
                .appendValue(matcher);
    }

    /**
     * A matcher for potentially infinite Streams of objects,
     * at least one of the first limit of which must match the given Matcher
     *
     * @param matcher A matcher to apply to items produced from the Stream
     * @param limit Only check this number of items from the Stream
     * @see #anyMatch
     * @see #startsWithAnyInt
     * @see #startsWithAnyLong
     * @see #startsWithAnyDouble
     */
    public static <T> Matcher<Stream<T>> startsWithAny(Matcher<T> matcher, long limit) {
        return new StreamAnyMatches<T>(matcher) {
            @Override
            protected boolean matchesSafely(Stream<T> actual) {
                return super.matchesSafely(actual.limit(limit));
            }

            @Override
            public void describeTo(Description description) {
                describeToStartsAnyWith(description, limit, matcher);
            }
        };
    }

    /**
     * A matcher for potentially infinite Streams of primitive longs,
     * at least one of the first limit of which must match the given Matcher
     *
     * @param matcher A matcher to apply to items produced from the Stream
     * @param limit Only check this number of items from the Stream
     * @see #anyMatchLong
     * @see #startsWithAny
     * @see #startsWithAnyInt
     * @see #startsWithAnyDouble
     */
    public static Matcher<LongStream> startsWithAnyLong(Matcher<Long> matcher, long limit) {
        return new LongStreamAnyMatches(matcher) {
            @Override
            protected boolean matchesSafely(LongStream actual) {
                return super.matchesSafely(actual.limit(limit));
            }

            @Override
            public void describeTo(Description description) {
                describeToStartsAnyWith(description, limit, matcher);
            }
        };
    }

    /**
     * A matcher for potentially infinite Streams of primitive doubles,
     * at least one of the first limit of which must match the given Matcher
     *
     * @param matcher A matcher to apply to items produced from the Stream
     * @param limit Only check this number of items from the Stream
     * @see #anyMatchDouble
     * @see #startsWithAny
     * @see #startsWithAnyInt
     * @see #startsWithAnyLong
     */
    public static Matcher<DoubleStream> startsWithAnyDouble(Matcher<Double> matcher, long limit) {
        return new DoubleStreamAnyMatches(matcher) {
            @Override
            protected boolean matchesSafely(DoubleStream actual) {
                return super.matchesSafely(actual.limit(limit));
            }

            @Override
            public void describeTo(Description description) {
                describeToStartsAnyWith(description, limit, matcher);
            }
        };
    }

    /**
     * A matcher for potentially infinite Streams of primitive ints,
     * at least one of the first limit of which must match the given Matcher
     *
     * @param matcher A matcher to apply to items produced from the Stream
     * @param limit Only check this number of items from the Stream
     * @see #anyMatchInt
     * @see #startsWithAny
     * @see #startsWithAnyLong
     * @see #startsWithAnyDouble
     */
    public static Matcher<IntStream> startsWithAnyInt(Matcher<Integer> matcher, long limit) {
        return new IntStreamAnyMatches(matcher) {
            @Override
            protected boolean matchesSafely(IntStream actual) {
                return super.matchesSafely(actual.limit(limit));
            }

            @Override
            public void describeTo(Description description) {
                describeToStartsAnyWith(description, limit, matcher);
            }
        };
    }

    /**
     * The BaseStream must produce exactly the given expected items in order, and no more.
     *
     * For infinite BaseStreams see {@link #startsWith(Object...)} or a primitive stream variant
     * @param expectedMatchers Matchers for the items that should be produced by the BaseStream
     * @param <T> The type of items
     * @param <S> The type of the BaseStream
     * @see #startsWith(Object...)
     * @see #startsWithInt(int...)
     * @see #startsWithLong(long...)
     * @see #startsWithDouble(double...)
     */
    @SafeVarargs
    public static <T, S extends BaseStream<T, ? extends S>> Matcher<S> yieldsExactly(Matcher<T>... expectedMatchers) {
        return new BaseMatcherStreamMatcher<T,S>() {
            @Override
            protected boolean matchesSafely(S actual) {
                return remainingItemsMatch(new ArrayIterator<>(expectedMatchers), actual.iterator());
            }
        };
    }

    /**
     * The BaseStream must produce exactly the given expected items in order, and no more.
     *
     * @param expectedMatchers Matchers for the items that should be produced by the BaseStream
     * @param <T> The type of items
     * @param <S> The type of the BaseStream
     *
     * @deprecated name clashes with {@link org.hamcrest.Matchers#contains(Matcher...)},
     *             use {@link #yieldsExactly(Matcher...)} instead
     */
    @SafeVarargs
    @Deprecated
    public static <T, S extends BaseStream<T, ? extends S>> Matcher<S> contains(Matcher<T>... expectedMatchers) {
        return yieldsExactly(expectedMatchers);
    }

    /**
     * The BaseStream must produce exactly the given expected items in order, and no more.
     *
     * For infinite BaseStreams see {@link #startsWith(Object...)} or a primitive stream variant
     * @param expected The items that should be produced by the BaseStream
     * @param <T> The type of items
     * @param <S> The type of the BaseStream
     * @see #startsWith(Object...)
     * @see #startsWithInt(int...)
     * @see #startsWithLong(long...)
     * @see #startsWithDouble(double...)
     */
    @SafeVarargs
    public static <T, S extends BaseStream<T, ? extends S>> Matcher<S> yieldsExactly(T... expected) {
        return new BaseStreamMatcher<T,S>() {
            @Override
            protected boolean matchesSafely(S actual) {
                return remainingItemsEqual(new ArrayIterator<>(expected), actual.iterator());
            }
        };
    }

    /**
     * The BaseStream must produce exactly the given expected items in order, and no more.
     *
     * @param expected The items that should be produced by the BaseStream
     * @param <T> The type of items
     * @param <S> The type of the BaseStream
     *
     * @deprecated name clashes with {@link org.hamcrest.Matchers#contains(Object...)},
     *             use {@link #yieldsExactly(Object...)} instead
     */
    @SafeVarargs
    @Deprecated
    public static <T, S extends BaseStream<T, ? extends S>> Matcher<S> contains(T... expected) {
        return yieldsExactly(expected);
    }

    /**
     * A matcher for a finite Stream of objects, all of which must match the given Matcher.
     *
     * For infinite Streams see {@link #startsWithAll}
     *
     * @param matcher A Matcher against which to compare items from the Stream
     * @param <T> The type of items produced by the Stream
     * @see #startsWithAll
     * @see #allMatchInt
     * @see #allMatchLong
     * @see #allMatchDouble
     */
    public static <T> Matcher<Stream<T>> allMatch(Matcher<T> matcher) {
        return new StreamAllMatches<T>(matcher) {
            @Override
            public void describeTo(Description description) {
                description.appendText("All to match ").appendValue(matcher);
            }
        };
    }

    /**
     * A matcher for a finite Stream of primitive ints, all of which must match the given Matcher.
     *
     * For infinite Streams see {@link #startsWithAllInt}
     *
     * @param matcher A Matcher against which to compare items from the Stream
     * @see #startsWithAllInt
     * @see #allMatch
     * @see #allMatchLong
     * @see #allMatchDouble
     */
    public static Matcher<IntStream> allMatchInt(Matcher<Integer> matcher) {
        return new IntStreamAllMatches(matcher) {
            @Override
            public void describeTo(Description description) {
                description.appendText("All to match ").appendValue(matcher);
            }
        };
    }

    /**
     * A matcher for a finite Stream of primitive longs, all of which must match the given Matcher.
     *
     * For infinite Streams see {@link #startsWithAllLong}
     *
     * @param matcher A Matcher against which to compare items from the Stream
     * @see #startsWithAllLong
     * @see #allMatch
     * @see #allMatchInt
     * @see #allMatchDouble
     */
    public static Matcher<LongStream> allMatchLong(Matcher<Long> matcher) {
        return new LongStreamAllMatches(matcher) {
            @Override
            public void describeTo(Description description) {
                description.appendText("All to match ").appendValue(matcher);
            }
        };
    }

    /**
     * A matcher for a finite Stream of primitive doubles, all of which must match the given Matcher.
     *
     * For infinite Streams see {@link #startsWithAllDouble}
     *
     * @param matcher A Matcher against which to compare items from the Stream
     * @see #startsWithAllDouble
     * @see #allMatch
     * @see #allMatchInt
     * @see #allMatchLong
     */
    public static Matcher<DoubleStream> allMatchDouble(Matcher<Double> matcher) {
        return new DoubleStreamAllMatches(matcher) {
            @Override
            public void describeTo(Description description) {
                description.appendText("All to match ").appendValue(matcher);
            }
        };
    }

    /**
     * A matcher for a finite Stream of objects, at least one of which must match the given Matcher.
     *
     * For infinite Streams see {@link #startsWithAny}
     *
     * @param matcher A Matcher against which to compare items from the Stream
     * @param <T> The type of items produced by the Stream
     * @see #startsWithAny
     * @see #anyMatchInt
     * @see #anyMatchLong
     * @see #anyMatchDouble
     */
    public static <T> Matcher<Stream<T>> anyMatch(Matcher<T> matcher) {
        return new StreamAnyMatches<T>(matcher) {
            @Override
            public void describeTo(Description description) {
                description.appendText("Any to match ").appendValue(matcher);
            }
        };
    }

    /**
     * A matcher for a finite Stream of primitive longs, at least one of which must match the given Matcher.
     *
     * For infinite Streams see {@link #startsWithAnyLong}
     *
     * @param matcher A Matcher against which to compare items from the Stream
     * @see #startsWithAnyLong
     * @see #anyMatch
     * @see #anyMatchInt
     * @see #anyMatchDouble
     */
    public static Matcher<LongStream> anyMatchLong(Matcher<Long> matcher) {
        return new LongStreamAnyMatches(matcher) {
            @Override
            public void describeTo(Description description) {
                description.appendText("Any to match ").appendValue(matcher);
            }
        };
    }

    /**
     * A matcher for a finite Stream of primitive doubles, at least one of which must match the given Matcher.
     *
     * For infinite Streams see {@link #startsWithAnyDouble}
     *
     * @param matcher A Matcher against which to compare items from the Stream
     * @see #startsWithAnyDouble
     * @see #anyMatch
     * @see #anyMatchInt
     * @see #anyMatchDouble
     */
    public static Matcher<DoubleStream> anyMatchDouble(Matcher<Double> matcher) {
        return new DoubleStreamAnyMatches(matcher) {
            @Override
            public void describeTo(Description description) {
                description.appendText("Any to match ").appendValue(matcher);
            }
        };
    }

    /**
     * A matcher for a finite Stream of primitive ints, at least one of which must match the given Matcher.
     *
     * For infinite Streams see {@link #startsWithAnyInt}
     *
     * @param matcher A Matcher against which to compare items from the Stream
     * @see #startsWithAnyInt
     * @see #anyMatch
     * @see #anyMatchLong
     * @see #anyMatchDouble
     */

    public static Matcher<IntStream> anyMatchInt(Matcher<Integer> matcher) {
        return new IntStreamAnyMatches(matcher) {
            @Override
            public void describeTo(Description description) {
                description.appendText("Any to match ").appendValue(matcher);
            }
        };
    }

    /**
     * A matcher for a potentially infinite Stream of objects against n expected items, matching if the first n items
     * produced by the Stream equal the expected items in order. Whether the Stream would subsequently produce
     * additional items is irrelevant.
     *
     * @param expected The expected items produced first by the Stream
     * @param <T> The type of items
     * @see #contains
     * @see #startsWithInt
     * @see #startsWithDouble
     * @see #startsWithLong
     */

    @SafeVarargs
    public static <T> Matcher<Stream<T>> startsWith(T... expected) {
        return new BaseStreamMatcher<T,Stream<T>>() {
            @Override
            protected boolean matchesSafely(Stream<T> actual) {
                return remainingItemsEqual(new ArrayIterator<>(expected), actual.limit(expected.length).iterator());
            }
        };
    }

    /**
     * A matcher for a potentially infinite Stream of primitive doubles against n expected items, matching if the first n items
     * produced by the Stream equal the expected items in order. Whether the Stream would subsequently produce
     * additional items is irrelevant.
     *
     * @param expected The expected items produced first by the Stream
     * @see #contains
     * @see #startsWith
     * @see #startsWithInt
     * @see #startsWithLong
     */
    public static Matcher<DoubleStream> startsWithDouble(double... expected) {
        return new BaseStreamMatcher<Double,DoubleStream>() {
            @Override
            protected boolean matchesSafely(DoubleStream actual) {
                return remainingItemsEqual(new DoubleArrayIterator(expected), actual.limit(expected.length).iterator());
            }
        };
    }

    /**
     * A matcher for a potentially infinite Stream of primitive longs against n expected items, matching if the first n items
     * produced by the Stream equal the expected items in order. Whether the Stream would subsequently produce
     * additional items is irrelevant.
     *
     * @param expected The expected items produced first by the Stream
     * @see #contains
     * @see #startsWith
     * @see #startsWithInt
     * @see #startsWithDouble
     */
    public static Matcher<LongStream> startsWithLong(long... expected) {
        return new BaseStreamMatcher<Long,LongStream>() {
            @Override
            protected boolean matchesSafely(LongStream actual) {
                return remainingItemsEqual(new LongArrayIterator(expected), actual.limit(expected.length).iterator());
            }
        };
    }

    /**
     * A matcher for a potentially infinite Stream of primitive ints against n expected items, matching if the first n items
     * produced by the Stream equal the expected items in order. Whether the Stream would subsequently produce
     * additional items is irrelevant.
     *
     * @param expected The expected items produced first by the Stream
     * @see #contains
     * @see #startsWith
     * @see #startsWithLong
     * @see #startsWithDouble
     */
    public static Matcher<IntStream> startsWithInt(int... expected) {
        return new BaseStreamMatcher<Integer,IntStream>() {
            @Override
            protected boolean matchesSafely(IntStream actual) {
                return remainingItemsEqual(new IntArrayIterator(expected), actual.limit(expected.length).iterator());
            }
        };
    }

    private static abstract class BaseStreamMatcher<T,S extends BaseStream<T,?>> extends TypeSafeMatcher<S> {
        final List<T> expectedAccumulator = new LinkedList<>();
        final List<T> actualAccumulator = new LinkedList<>();

        @Override
        public void describeTo(Description description) {
            describe(description, expectedAccumulator);
        }

        @Override
        protected void describeMismatchSafely(S item, Description description) {
            describe(description, actualAccumulator);
        }

        private void describe(Description description, List<T> values) {
            description.appendText("Stream of ").appendValueList("[", ",", "]", values);
        }

        boolean remainingItemsEqual(Iterator<T> expectedIterator, Iterator<T> actualIterator) {
            if (!expectedIterator.hasNext() && !actualIterator.hasNext()) {
                return true;
            }
            if (expectedIterator.hasNext() && actualIterator.hasNext()) {
                T nextExpected = expectedIterator.next();
                expectedAccumulator.add(nextExpected);
                T nextActual = actualIterator.next();
                actualAccumulator.add(nextActual);
                if (Objects.equals(nextExpected, nextActual)) {
                    return remainingItemsEqual(expectedIterator, actualIterator);
                }
            }
            expectedIterator.forEachRemaining(expectedAccumulator::add);
            actualIterator.forEachRemaining(actualAccumulator::add);
            return false;
        }
    }

    private static abstract class BaseMatcherStreamMatcher<T,S extends BaseStream<T,?>> extends TypeSafeMatcher<S> {
        final List<Matcher<T>> expectedAccumulator = new LinkedList<>();
        final List<T> actualAccumulator = new LinkedList<>();

        @Override
        protected void describeMismatchSafely(S item, Description description) {
            description.appendText("Stream of ").appendValueList("[", ",", "]", actualAccumulator);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Stream of ").appendValueList("[", ",", "]", expectedAccumulator);
        }

        boolean remainingItemsMatch(Iterator<Matcher<T>> expectedIterator, Iterator<T> actualIterator) {
            if (!expectedIterator.hasNext() && !actualIterator.hasNext()) {
                return true;
            }
            if (expectedIterator.hasNext() && actualIterator.hasNext()) {
                Matcher<T> nextExpected = expectedIterator.next();
                expectedAccumulator.add(nextExpected);
                T nextActual = actualIterator.next();
                actualAccumulator.add(nextActual);
                if (nextExpected.matches(nextActual)) {
                    return remainingItemsMatch(expectedIterator, actualIterator);
                }
            }
            expectedIterator.forEachRemaining(expectedAccumulator::add);
            actualIterator.forEachRemaining(actualAccumulator::add);
            return false;
        }
    }

    private static void allMatchMismatch(Description mismatchDescription, long position, Object nonMatch) {
        mismatchDescription.appendText("Item ").appendText(Long.toString(position)).appendText(" failed to match: ").appendValue(nonMatch);
    }

    private static abstract class StreamAllMatches<T> extends TypeSafeMatcher<Stream<T>> {
        private T nonMatching;
        private long positionNonMatching = -1L;
        private final Matcher<T> matcher;

        StreamAllMatches(Matcher<T> matcher) {
            this.matcher = matcher;
        }

        @Override
        protected boolean matchesSafely(Stream<T> actual) {
            return actual
                    .peek(i -> {nonMatching = i; positionNonMatching++;})
                    .allMatch(matcher::matches);
        }

        @Override
        protected void describeMismatchSafely(Stream<T> actual, Description mismatchDescription) {
            allMatchMismatch(mismatchDescription, positionNonMatching, nonMatching);
        }
    }

    private static abstract class IntStreamAllMatches extends TypeSafeMatcher<IntStream> {
        private int nonMatching;
        private long positionNonMatching = -1L;
        private final Matcher<Integer> matcher;

        IntStreamAllMatches(Matcher<Integer> matcher) {
            this.matcher = matcher;
        }

        @Override
        protected boolean matchesSafely(IntStream actual) {
            return actual
                    .peek(i -> {nonMatching = i; positionNonMatching++;})
                    .allMatch(matcher::matches);
        }

        @Override
        protected void describeMismatchSafely(IntStream actual, Description mismatchDescription) {
            allMatchMismatch(mismatchDescription, positionNonMatching, nonMatching);
        }
    }

    private static abstract class LongStreamAllMatches extends TypeSafeMatcher<LongStream> {
        private long nonMatching;
        private long positionNonMatching = -1L;
        private final Matcher<Long> matcher;

        LongStreamAllMatches(Matcher<Long> matcher) {
            this.matcher = matcher;
        }

        @Override
        protected boolean matchesSafely(LongStream actual) {
            return actual
                    .peek(i -> {nonMatching = i; positionNonMatching++;})
                    .allMatch(matcher::matches);
        }

        @Override
        protected void describeMismatchSafely(LongStream actual, Description mismatchDescription) {
            allMatchMismatch(mismatchDescription, positionNonMatching, nonMatching);
        }
    }

    private static abstract class DoubleStreamAllMatches extends TypeSafeMatcher<DoubleStream> {
        private double nonMatching;
        private long positionNonMatching = -1L;
        private final Matcher<Double> matcher;

        DoubleStreamAllMatches(Matcher<Double> matcher) {
            this.matcher = matcher;
        }

        @Override
        protected boolean matchesSafely(DoubleStream actual) {
            return actual
                    .peek(i -> {nonMatching = i; positionNonMatching++;})
                    .allMatch(matcher::matches);
        }

        @Override
        protected void describeMismatchSafely(DoubleStream actual, Description mismatchDescription) {
            allMatchMismatch(mismatchDescription, positionNonMatching, nonMatching);
        }
    }

    private static void anyMatchMismatch(Description mismatchDescription, List<?> accumulator) {
        mismatchDescription
                .appendText("None of these items matched: ")
                .appendValueList("[", ",", "]", accumulator);
    }

    private static abstract class StreamAnyMatches<T> extends TypeSafeMatcher<Stream<T>> {
        final List<T> accumulator = new LinkedList<>();
        final Matcher<T> matcher;

        StreamAnyMatches(Matcher<T> matcher) {
            this.matcher = matcher;
        }

        @Override
        protected boolean matchesSafely(Stream<T> actual) {
            return actual.peek(accumulator::add).anyMatch(matcher::matches);
        }

        @Override
        protected void describeMismatchSafely(Stream<T> actual, Description mismatchDescription) {
            anyMatchMismatch(mismatchDescription,accumulator);
        }
    }

    private static abstract class LongStreamAnyMatches extends TypeSafeMatcher<LongStream> {
        final List<Long> accumulator = new LinkedList<>();
        final Matcher<Long> matcher;

        LongStreamAnyMatches(Matcher<Long> matcher) {
            this.matcher = matcher;
        }

        @Override
        protected boolean matchesSafely(LongStream actual) {
            return actual.peek(accumulator::add).anyMatch(matcher::matches);
        }

        @Override
        protected void describeMismatchSafely(LongStream actual, Description mismatchDescription) {
            anyMatchMismatch(mismatchDescription, accumulator);
        }
    }

    private static abstract class IntStreamAnyMatches extends TypeSafeMatcher<IntStream> {
        final List<Integer> accumulator = new LinkedList<>();
        final Matcher<Integer> matcher;

        IntStreamAnyMatches(Matcher<Integer> matcher) {
            this.matcher = matcher;
        }

        @Override
        protected boolean matchesSafely(IntStream actual) {
            return actual.peek(accumulator::add).anyMatch(matcher::matches);
        }

        @Override
        protected void describeMismatchSafely(IntStream actual, Description mismatchDescription) {
            anyMatchMismatch(mismatchDescription, accumulator);
        }
    }

    private static abstract class DoubleStreamAnyMatches extends TypeSafeMatcher<DoubleStream> {
        final List<Double> accumulator = new LinkedList<>();
        final Matcher<Double> matcher;

        DoubleStreamAnyMatches(Matcher<Double> matcher) {
            this.matcher = matcher;
        }

        @Override
        protected boolean matchesSafely(DoubleStream actual) {
            return actual.peek(accumulator::add).anyMatch(matcher::matches);
        }

        @Override
        protected void describeMismatchSafely(DoubleStream actual, Description mismatchDescription) {
            anyMatchMismatch(mismatchDescription, accumulator);
        }
    }


    private static class ArrayIterator<T> implements Iterator<T> {
        private final T[] expected;
        private int currentPos = 0;

        @SafeVarargs
        public ArrayIterator(T... expected) {
            this.expected = expected;
        }

        @Override
        public boolean hasNext() {
            return currentPos < expected.length;
        }

        @Override
        public T next() {
            return expected[currentPos++];
        }
    }

    private static class IntArrayIterator implements PrimitiveIterator.OfInt {
        private final int[] expected;
        private int currentPos = 0;

        public IntArrayIterator(int... expected) {
            this.expected = expected;
        }

        @Override
        public boolean hasNext() {
            return currentPos < expected.length;
        }

        @Override
        public int nextInt() {
            return expected[currentPos++];
        }
    }

    private static class LongArrayIterator implements PrimitiveIterator.OfLong {
        private final long[] expected;
        private int currentPos = 0;

        public LongArrayIterator(long... expected) {
            this.expected = expected;
        }

        @Override
        public boolean hasNext() {
            return currentPos < expected.length;
        }

        @Override
        public long nextLong() {
            return expected[currentPos++];
        }
    }

    private static class DoubleArrayIterator implements PrimitiveIterator.OfDouble {
        private final double[] expected;
        private int currentPos = 0;

        public DoubleArrayIterator(double... expected) {
            this.expected = expected;
        }

        @Override
        public boolean hasNext() {
            return currentPos < expected.length;
        }

        @Override
        public double nextDouble() {
            return expected[currentPos++];
        }
    }
}
