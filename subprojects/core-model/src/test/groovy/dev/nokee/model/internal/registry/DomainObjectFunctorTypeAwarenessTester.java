package dev.nokee.model.internal.registry;

import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class DomainObjectFunctorTypeAwarenessTester<F> extends AbstractDomainObjectFunctorTester<F> {
	@Override
	protected <T> F createSubject(Class<T> type) {
		return createSubject(type, node("foo", new MyType(), new MyOtherType()));
	}

	protected Class<?> getType(F functor) {
		return invoke(functor, "getType", new Class[0], new Object[0]);
	}

	@Test
	void canQueryProviderType() {
		assertThat(getType(createSubject(MyType.class)), equalTo(MyType.class));
		assertThat(getType(createSubject(MyOtherType.class)), equalTo(MyOtherType.class));
	}
}
