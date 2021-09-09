package dev.nokee.model.streams;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

public interface ModelStreamTester<T> {
	ModelStream<T> subject();

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(subject());
	}

	@Test
	default void returnsBranchedModelStreamOnSplit() {
		assertThat(subject().split(), isA(BranchedModelStream.class));
	}

	@Test
	default void returnsModelStreamOnFilter() {
		assertThat(subject().filter(alwaysTrue()), isA(ModelStream.class));
	}

	@Test
	default void returnsModelStreamOnMap() {
		assertThat(subject().map(identity()), isA(ModelStream.class));
	}

	@Test
	default void returnsModelStreamOnFlatMap() {
		assertThat(subject().flatMap(flatIdentity()), isA(ModelStream.class));
	}

	@Test
	default void returnsModelStreamOnSorting() {
		assertThat(subject().sorted(noParticularOrder()), isA(ModelStream.class));
	}

	@Test
	default void returnsProviderOnFirstFind() {
		assertThat(subject().findFirst(), isA(Provider.class));
	}

	@Test
	default void returnsProviderOnReduction() {
		assertThat(subject().reduce(alwaysLastElement()), isA(Provider.class));
	}

	@Test
	default void returnsProviderOnMaximumReduction() {
		assertThat(subject().min(noParticularOrder()), isA(Provider.class));
	}

	@Test
	default void returnsProviderOnMinimumReduction() {
		assertThat(subject().max(noParticularOrder()), isA(Provider.class));
	}

	@Test
	default void returnsItselfOnPeek() {
		assertThat(subject().peek(doSomething()), is(subject()));
	}

	@Test
	default void returnsProviderOnCollect() {
		assertThat(subject().collect(Collectors.toList()), isA(Provider.class));
	}

	static <T> Predicate<T> alwaysTrue() {
		return ignored -> true;
	}

	static <T> Function<T, Iterable<? extends T>> flatIdentity() {
		return ImmutableList::of;
	}

	static <T> BinaryOperator<T> alwaysLastElement() {
		return (a, b) -> a;
	}

	static <T> Comparator<T> noParticularOrder() {
		return (a, b) -> -1;
	}

	static <T> Consumer<T> doSomething() {
		return ignored -> { /* do something meaningless */ };
	}
}
