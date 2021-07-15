package dev.nokee.model.core;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public interface ModelPredicateTester {
	ModelPredicate createSubject();

	ModelProjection trueObject();
	ModelProjection falseObject();

	ModelPredicate alwaysTruePredicate();
	ModelPredicate alwaysFalsePredicate();

	@Test
	default void returnsOneOfTheAndPredicateWhenBothAreTheSamePredicate() {
		val subject = createSubject();
		assertThat(subject.and(subject), is(subject));
	}

	@Test
	default void returnsFalseWhenOneOfTheAndPredicatesReturnsFalse() {
		assertThat(createSubject().and(alwaysFalsePredicate()).test(trueObject()), equalTo(false));
		assertThat(createSubject().and(alwaysTruePredicate()).test(falseObject()), equalTo(false));
	}

	@Test
	default void returnsTrueWhenBothAndPredicatesReturnsTrue() {
		assertThat(createSubject().and(alwaysTruePredicate()).test(trueObject()), equalTo(true));
	}

	@Test
	default void returnsFalseWhenBothAndPredicatesReturnsFalse() {
		assertThat(createSubject().and(alwaysTruePredicate()).test(falseObject()), equalTo(false));
	}

	@Test
	default void returnsOneOfTheOrPredicateWhenBothAreTheSamePredicate() {
		val subject = createSubject();
		assertThat(subject.or(subject), is(subject));
	}

	@Test
	default void returnsTrueWhenOneOfTheOrPredicatesReturnsTrue() {
		assertThat(createSubject().or(alwaysFalsePredicate()).test(trueObject()), equalTo(true));
		assertThat(createSubject().or(alwaysTruePredicate()).test(falseObject()), equalTo(true));
	}

	@Test
	default void returnsTrueWhenBothOrPredicatesReturnsTrue() {
		assertThat(createSubject().or(alwaysTruePredicate()).test(trueObject()), equalTo(true));
	}

	@Test
	default void returnsFalseWhenBothOrPredicatesReturnsFalse() {
		assertThat(createSubject().or(alwaysFalsePredicate()).test(falseObject()), equalTo(false));
	}
}
