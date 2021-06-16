package dev.nokee.model.streams;

import lombok.val;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.alwaysTrue;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

interface BranchedModelStreamTester<T> {
	ModelStream<T> createSubject();

	T createElement();

	@Test
	default void defaultBranchNameToIndexWhereDefaultBranchIsZero() {
		val branches = createSubject().split().branch(alwaysTrue()).branch(alwaysTrue()).defaultBranch();
		assertAll(
			() -> assertThat(branches, aMapWithSize(3)),
			() -> assertThat("default branch", branches, hasEntry(equalTo("0"), isA(ModelStream.class))),
			() -> assertThat("first branch", branches, hasEntry(equalTo("1"), isA(ModelStream.class))),
			() -> assertThat("second branch", branches, hasEntry(equalTo("2"), isA(ModelStream.class)))
		);
	}

	@Test
	default void defaultBranchNameStartsAtOneWhenNoDefaultBranch() {
		val branches = createSubject().split().branch(alwaysTrue()).branch(alwaysTrue()).noDefaultBranch();
		assertThat(branches, not(hasKey("0")));
	}

	@Test
	default void defaultBranchNameAreNotReused() {
		val branches = createSubject().split().branch(alwaysTrue(), Branched.as("first")).branch(alwaysTrue()).noDefaultBranch();
		assertThat(branches, allOf(not(hasKey("1")), hasKey("first")));
	}

	@Test
	default void sendElementsToFirstMatchingBranch() {
		val branches = createSubject().split().branch(alwaysTrue()).branch(alwaysTrue()).noDefaultBranch();
		val e0 = createElement();
		val e1 = createElement();
		val e2 = createElement();
		assertAll(
			() -> assertThat(branches.get("1").collect(toList()), providerOf(contains(e0, e1, e2))),
			() -> assertThat(branches.get("2").collect(toList()), providerOf(emptyIterable()))
		);
	}

	@Test
	default void canSplitElementsIntoMultipleBranches() {
		val e0 = createElement();
		val e1 = createElement();
		val e2 = createElement();
		val branches = createSubject().split().branch(e0::equals).branch(asList(e1, e2)::contains).noDefaultBranch();
		assertAll(
			() -> assertThat(branches.get("1").collect(toList()), providerOf(contains(e0))),
			() -> assertThat(branches.get("2").collect(toList()), providerOf(contains(e1, e2)))
		);
	}

	@Test
	default void dropsElementsNotMatchedByAnyBranchesWhenNoDefaultBranch() {
		val e0 = createElement();
		val e1 = createElement();
		val e2 = createElement(); // dropped
		val branches = createSubject().split().branch(e0::equals).branch(e1::equals).noDefaultBranch();
		assertAll(
			() -> assertThat(branches.get("1").collect(toList()), providerOf(contains(e0))),
			() -> assertThat(branches.get("2").collect(toList()), providerOf(contains(e1)))
		);
	}

	@Test
	default void captureElementsNotMatchedByAnyBranchesInDefaultBranch() {
		val e0 = createElement();
		val branches = createSubject().split().branch(e0::equals).branch(alwaysFalse()).defaultBranch();
		val e1 = createElement();
		val e2 = createElement();
		assertAll(
			() -> assertThat(branches.get("1").collect(toList()), providerOf(contains(e0))),
			() -> assertThat(branches.get("2").collect(toList()), providerOf(emptyIterable())),
			() -> assertThat(branches.get("0").collect(toList()), providerOf(contains(e1, e2)))
		);
	}
}
