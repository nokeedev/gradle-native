package dev.nokee.model.internal.registry;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.ModelIdentifier;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class DomainObjectFunctorIdentifierAwarenessTester<F> extends AbstractDomainObjectFunctorTester<F> {
	@Override
	protected <T> F createSubject(Class<T> type) {
		return createSubject(type, node("foo", new MyType(), new MyOtherType()));
	}

	protected DomainObjectIdentifier getIdentifier(F functor) {
		return invoke(functor, "getIdentifier", new Class[0], new Object[0]);
	}

	@Test
	void canQueryProviderIdentifier() {
		assertThat(getIdentifier(createSubject(MyType.class)), equalTo(ModelIdentifier.of("foo", MyType.class)));
		assertThat(getIdentifier(createSubject(MyOtherType.class)), equalTo(ModelIdentifier.of("foo", MyOtherType.class)));
	}
}
