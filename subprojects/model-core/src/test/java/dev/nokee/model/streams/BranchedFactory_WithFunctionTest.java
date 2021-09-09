package dev.nokee.model.streams;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static com.google.common.base.Functions.constant;
import static com.google.common.base.Predicates.alwaysTrue;
import static dev.nokee.model.streams.Branched.withFunction;
import static java.util.function.Function.identity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BranchedFactory_WithFunctionTest implements BranchedFactoryTester<Object>, BranchedTester<Object> {
	@Override
	public Branched<Object> createSubject() {
		return withFunction(identity());
	}

	@Override
	public BranchedModelStream<Object> createStream() {
		return new DefaultBranchedModelStream<>(new DefaultModelStream<>(Topic.of(Stream::empty)));
	}

	@Test
	void doesNotAddBranchToResultMapWhenFunctionReturnsNull() {
		assertThat(createStream().branch(alwaysTrue(), withFunction(constant(null))).noDefaultBranch(), anEmptyMap());
	}

	@Test
	void addsBranchUsingDefaultNameToResultMapWhenFunctionReturnsStream() {
		assertThat(createStream().branch(alwaysTrue(), withFunction(identity())).noDefaultBranch(),
			allOf(aMapWithSize(1), hasEntry(equalTo("1"), isA(ModelStream.class))));
	}
}
