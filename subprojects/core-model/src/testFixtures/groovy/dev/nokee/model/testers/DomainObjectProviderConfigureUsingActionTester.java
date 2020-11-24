package dev.nokee.model.testers;

import dev.nokee.model.DomainObjectProvider;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class DomainObjectProviderConfigureUsingActionTester<T> extends AbstractDomainObjectProviderConfigureTester<T> {
	@Override
	protected void configure(DomainObjectProvider<T> provider, Action<T> configurationAction) {
		provider.configure(configurationAction);
	}

	@Test
	void throwsExceptionIfConfigurationActionIsNull() {
		assertThrows(NullPointerException.class, () -> createSubject().configure((Action<T>) null));
	}
}
