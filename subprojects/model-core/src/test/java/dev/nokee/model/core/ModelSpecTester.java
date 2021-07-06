package dev.nokee.model.core;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public interface ModelSpecTester<T> {
	ModelSpec<T> createSubject();

	ModelProjection trueObject();
	ModelProjection falseObject();

	ModelSpec<T> alwaysTrueSpec();
	ModelSpec<T> alwaysFalseSpec();

	@Test
	default void returnsOneOfTheAndSpecWhenBothAreTheSameSpec() {
		val subject = createSubject();
		assertThat(subject.and(subject), is(subject));
	}

	@Test
	default void returnsFalseWhenOneOfTheAndSpecsReturnsFalse() {
		assertThat(createSubject().and(alwaysFalseSpec()).isSatisfiedBy(trueObject()), equalTo(false));
		assertThat(createSubject().and(alwaysTrueSpec()).isSatisfiedBy(falseObject()), equalTo(false));
	}

	@Test
	default void returnsTrueWhenBothAndSpecsReturnsTrue() {
		assertThat(createSubject().and(alwaysTrueSpec()).isSatisfiedBy(trueObject()), equalTo(true));
	}

	@Test
	default void returnsFalseWhenBothAndSpecsReturnsFalse() {
		assertThat(createSubject().and(alwaysTrueSpec()).isSatisfiedBy(falseObject()), equalTo(false));
	}

	@Test
	default void returnsOneOfTheOrSpecWhenBothAreTheSameSpec() {
		val subject = createSubject();
		assertThat(subject.or(subject), is(subject));
	}

	@Test
	default void returnsTrueWhenOneOfTheOrSpecsReturnsTrue() {
		assertThat(createSubject().or(alwaysFalseSpec()).isSatisfiedBy(trueObject()), equalTo(true));
		assertThat(createSubject().or(alwaysTrueSpec()).isSatisfiedBy(falseObject()), equalTo(true));
	}

	@Test
	default void returnsTrueWhenBothOrSpecsReturnsTrue() {
		assertThat(createSubject().or(alwaysTrueSpec()).isSatisfiedBy(trueObject()), equalTo(true));
	}

	@Test
	default void returnsFalseWhenBothOrSpecsReturnsFalse() {
		assertThat(createSubject().or(alwaysFalseSpec()).isSatisfiedBy(falseObject()), equalTo(false));
	}
}
