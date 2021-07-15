package dev.nokee.model.internal;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.ModelRule;
import dev.nokee.model.registry.ModelRegistry;
import dev.nokee.model.streams.ModelStream;
import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DefaultModelRegistryTest {
	private final ModelRegistry subject = new DefaultModelRegistry(objectFactory());

	@Test
	void hasRootNode() {
		assertThat(subject.getRoot(), notNullValue());
	}

	@Test
	void rootNodeHasRootIdentity() {
		assertThat(subject.getRoot().getIdentity(), equalTo(DomainObjectIdentities.root()));
	}

	@Test
	void rootNodeDoesNotHaveParent() {
		assertThat(subject.getRoot().getParent(), emptyOptional());
	}

	@Test /** @see dev.nokee.model.internal.DefaultModelRegistryAllProjectionsStreamTest */
	void newRegistryHasNoProjectionInStream() {
		assertThat(subject.allProjections().collect(toList()), providerOf(emptyIterable()));
	}

	@Test
	void canRegisterModelRuleInstance() {
		val rule = new TestModelRule();
		subject.registerRule(rule);
		assertThat(TestModelRule.capturedExecution, calledOnceWith(singleArgumentOf(subject.allProjections())));
	}

	@Test
	void canRegisterModelRuleClass() {
		subject.registerRule(TestModelRule.class);
		assertThat(TestModelRule.capturedExecution, calledOnceWith(singleArgumentOf(subject.allProjections())));
	}

	public static class TestModelRule implements ModelRule {
		private static ActionTestUtils.MockAction<ModelStream<ModelProjection>> capturedExecution;

		@Inject
		public TestModelRule() {
			capturedExecution = ActionTestUtils.mockAction();
		}

		@Override
		public void execute(ModelStream<ModelProjection> stream) {
			capturedExecution.execute(stream);
		}
	}
}
