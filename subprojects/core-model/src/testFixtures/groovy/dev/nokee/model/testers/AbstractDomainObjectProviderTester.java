package dev.nokee.model.testers;

import dev.nokee.model.DomainObjectProvider;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractDomainObjectProviderTester<T> {
	private SampleProviders<T> samples;

	@BeforeEach
	final void setUp() {
		this.samples = getSubjectGenerator().samples();
	}

	protected abstract TestProviderGenerator<T> getSubjectGenerator();

	protected final DomainObjectProvider<T> createSubject() {
		return getSubjectGenerator().create();
	}

	protected final SampleProvider<T> p0() {
		return samples.p0();
	}

	protected final SampleProvider<T> p1() {
		return samples.p1();
	}

	protected final SampleProvider<T> p2() {
		return samples.p2();
	}

	protected final Class<T> getSubjectType() {
		return getSubjectGenerator().getType();
	}

	protected final void expectUnresolved(DomainObjectProvider<T> provider) {
		@SuppressWarnings("unchecked")
		val action = (Action<T>) mock(Action.class);
		provider.configure(action);
		verify(action, never()).execute(any());
	}

	protected final void expectResolved(DomainObjectProvider<T> provider) {
		@SuppressWarnings("unchecked")
		val action = (Action<T>) mock(Action.class);
		provider.configure(action);
		verify(action, times(1)).execute(any());
	}

	protected final <S> S resolve(DomainObjectProvider<S> provider) {
		return provider.get();
	}

	protected final void resolve(Provider<?> provider) {
		provider.get();
	}
}
