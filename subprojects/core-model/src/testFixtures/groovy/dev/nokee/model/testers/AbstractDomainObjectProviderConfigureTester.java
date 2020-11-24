package dev.nokee.model.testers;

import dev.nokee.model.DomainObjectProvider;
import lombok.val;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractDomainObjectProviderConfigureTester<T> extends AbstractDomainObjectProviderTester<T> {
	protected abstract void configure(DomainObjectProvider<T> provider, Action<T> configurationAction);

	@Test
	void doesNotCallConfigurationActionImmediately() {
		@SuppressWarnings("unchecked")
		val action = (Action<T>) mock(Action.class);
		configure(createSubject(), action);
		verify(action, Mockito.never()).execute(any());
	}

	@Test
	void callsConfigurationActionWhenProviderResolves() {
		@SuppressWarnings("unchecked")
		val action = (Action<T>) mock(Action.class);
		val provider = createSubject();

		when:
		configure(provider, action);
		resolve(provider);

		then:
		verify(action, times(1)).execute(any());
	}

	@Test
	void callsConfigurationActionImmediatelyWhenProviderWasAlreadyResolved() {
		@SuppressWarnings("unchecked")
		val action = (Action<T>) mock(Action.class);
		val provider = createSubject();

		when:
		resolve(provider);
		configure(provider, action);

		then:
		verify(action, times(1)).execute(any());
	}
}
