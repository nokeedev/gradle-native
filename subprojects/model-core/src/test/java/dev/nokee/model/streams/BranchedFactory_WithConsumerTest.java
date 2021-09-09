package dev.nokee.model.streams;

import dev.nokee.utils.ConsumerTestUtils;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static com.google.common.base.Predicates.alwaysTrue;
import static dev.nokee.model.streams.Branched.withConsumer;
import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BranchedFactory_WithConsumerTest implements BranchedFactoryTester<Object>, BranchedTester<Object> {
	@Override
	public Branched<Object> createSubject() {
		return withConsumer(it -> {});
	}

	@Override
	public BranchedModelStream<Object> createStream() {
		return new DefaultBranchedModelStream<>(new DefaultModelStream<>(Topic.of(Stream::empty)));
	}

	@Test
	void addsBranchUsingDefaultNameToResultMapOfBranchedStream() {
		val capturedStream = new MutableObject<ModelStream<Object>>();
		val branches = createStream().branch(alwaysTrue(), withConsumer(capturedStream::setValue)).noDefaultBranch();
		assertThat(branches, allOf(aMapWithSize(1), hasEntry("1", capturedStream.getValue())));
	}

	@Test
	void callsBackBranchWithParentStream() {
		val chain = ConsumerTestUtils.mockConsumer();
		createStream().branch(alwaysTrue(), withConsumer(chain));
		assertThat(chain, calledOnceWith(singleArgumentOf(isA(ModelStream.class))));
	}
}
