package dev.nokee.model.dsl;

import dev.nokee.model.core.ModelPredicateTester;
import dev.nokee.model.core.ModelProjection;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Deprecated
public interface ModelSpecTester<T> extends ModelPredicateTester {
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
		assertThat(createSubject().and(alwaysFalseSpec()).test(trueObject()), equalTo(false));
		assertThat(createSubject().and(alwaysTrueSpec()).test(falseObject()), equalTo(false));
	}

	@Test
	default void returnsTrueWhenBothAndSpecsReturnsTrue() {
		assertThat(createSubject().and(alwaysTrueSpec()).test(trueObject()), equalTo(true));
	}

	@Test
	default void returnsFalseWhenBothAndSpecsReturnsFalse() {
		assertThat(createSubject().and(alwaysTrueSpec()).test(falseObject()), equalTo(false));
	}

	@Test
	default void returnsOneOfTheOrSpecWhenBothAreTheSameSpec() {
		val subject = createSubject();
		assertThat(subject.or(subject), is(subject));
	}

	@Test
	default void returnsTrueWhenOneOfTheOrSpecsReturnsTrue() {
		assertThat(createSubject().or(alwaysFalseSpec()).test(trueObject()), equalTo(true));
		assertThat(createSubject().or(alwaysTrueSpec()).test(falseObject()), equalTo(true));
	}

	@Test
	default void returnsTrueWhenBothOrSpecsReturnsTrue() {
		assertThat(createSubject().or(alwaysTrueSpec()).test(trueObject()), equalTo(true));
	}

	@Test
	default void returnsFalseWhenBothOrSpecsReturnsFalse() {
		assertThat(createSubject().or(alwaysFalseSpec()).test(falseObject()), equalTo(false));
	}
}
