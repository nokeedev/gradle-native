package dev.nokee.model.testers;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.utils.ProviderUtils;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class DomainObjectProviderFlatMapTester<T> extends AbstractDomainObjectProviderTransformTester<T> {
	@Override
	protected <S> Provider<S> transform(DomainObjectProvider<T> provider, Transformer<S, T> mapper) {
		return provider.flatMap(it -> ProviderUtils.fixed(mapper.transform(it)));
	}

	@Test
	void throwsExceptionIfTransformerIsNull() {
		assertThrows(NullPointerException.class, () -> createSubject().flatMap(null));
	}
}
