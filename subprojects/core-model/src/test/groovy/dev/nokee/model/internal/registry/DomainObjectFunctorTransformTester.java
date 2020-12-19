package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjections;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

public abstract class DomainObjectFunctorTransformTester<F> extends AbstractDomainObjectFunctorTester<F> {
	private final MyType myTypeInstance = new MyType();
	private final ModelNode node = node("foo", ModelProjections.ofInstance(myTypeInstance));
	private final Provider<String> subject = transform(createSubject(MyType.class), MyType::getValue);

	@Override
	protected <T> F createSubject(Class<T> type) {
		return createSubject(type, node);
	}

	@Override
	protected abstract <T> F createSubject(Class<T> type, ModelNode node);

	protected abstract <T, R> Provider<R> transform(F functor, Function<? super T, ? extends R> mapper);

	@BeforeEach
	void configureDefaultValue() {
		myTypeInstance.setValue("default");
	}

	@Test
	void doesNotEagerlyRealizeModelNode() {
		assertThat(node.getState(), lessThan(ModelNode.State.Realized));
	}

	@Test
	void realizeModelNodeWhenMappedProviderIsQueried() {
		subject.getOrNull();
		assertThat(node.getState(), equalTo(ModelNode.State.Realized));
	}

	@Test
	void providerReturnsMappedValue() {
		assertThat(subject.get(), equalTo("default"));
	}

	@Test
	void changesToNodeProjectionAreReflectedByTheMappedProvider() {
		myTypeInstance.setValue("new-value");
		assertThat(subject.get(), equalTo("new-value"));
	}

	static class MyType {
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
