package dev.nokee.model.testers;

import dev.nokee.model.DomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class DomainObjectProviderMapTester<T> extends AbstractDomainObjectProviderTransformTester<T> {
	@Override
	protected <S> Provider<S> transform(DomainObjectProvider<T> provider, Transformer<S, T> mapper) {
		return provider.map(mapper);
	}

	@Test
	void throwsExceptionIfTransformerIsNull() {
		assertThrows(NullPointerException.class, () -> createSubject().map(null));
	}
}
