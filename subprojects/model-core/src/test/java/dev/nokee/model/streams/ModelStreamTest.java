package dev.nokee.model.streams;

import dev.nokee.model.TestProjection;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ModelStreamTest {
	@Test
	void canCreateStreamFromCustomTopic() {
		// NOTE: Important to specify an up-cast element type as we are testing the generic bound
		val stream = assertDoesNotThrow(() -> ModelStream.<Object>of(new TestTopic()));
		assertThat(stream, isA(ModelStream.class));
	}

	private static final class TestTopic extends Topic<TestProjection> {
		@Override
		public Stream<TestProjection> get() {
			return Stream.empty();
		}
	}
}
