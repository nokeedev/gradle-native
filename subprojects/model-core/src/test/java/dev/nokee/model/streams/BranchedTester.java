package dev.nokee.model.streams;

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Predicates.alwaysTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.isA;

public interface BranchedTester<T> {
	Branched<T> createSubject();

	BranchedModelStream<T> createStream();

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(createSubject());
	}

	@Test
	default void returnsSameInstanceWithName() {
		val subject = createSubject();
		assertThat(subject.withName("foo"), isA(subject.getClass()));
	}

	@Test
	default void canOverrideBranchName() {
		val branches = createStream()
			.branch(alwaysTrue(), createSubject().withName("first"))
			.branch(alwaysTrue(), createSubject().withName("second"))
			.noDefaultBranch();
		assertThat(branches.keySet(), containsInAnyOrder("first", "second"));
	}

	@Test
	default void canOverrideDefaultBranchName() {
		val branches = createStream().defaultBranch(createSubject().withName("default"));
		assertThat(branches.keySet(), containsInAnyOrder("default"));
	}
}
