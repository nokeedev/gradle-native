package dev.nokee.model.testers;

import dev.nokee.model.DomainObjectProvider;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public abstract class DomainObjectProviderConfigureUsingClosureTester<T> extends AbstractDomainObjectProviderConfigureTester<T> {
	@Override
	protected void configure(DomainObjectProvider<T> provider, Action<T> configurationAction) {
		provider.configure(new ClosureActionAdapter<T>(this) {
			@Override
			public void doCall(T t) {
				configurationAction.execute(t);
			}
		});
	}

	private static abstract class ClosureActionAdapter<T> extends groovy.lang.Closure<Void> {

		public ClosureActionAdapter(Object owner, Object thisObject) {
			super(owner, thisObject);
		}

		public ClosureActionAdapter(Object owner) {
			super(owner);
		}

		public abstract void doCall(T t);
	}

	private interface CapturedValue {
		void capture(Object value);
	}

	@Test
	void throwsExceptionIfConfigurationClosureIsNull() {
		assertThrows(NullPointerException.class, () -> createSubject().configure((Closure<Void>) null));
	}

	@Test
	void configuresDelegateFirstResolveStrategy() {
		val captor = mock(CapturedValue.class);
		val closure = new ClosureActionAdapter<T>(this) {
			@Override
			public void doCall(Object o) {
				captor.capture(getResolveStrategy());
			}
		};
		val provider = createSubject();

		when:
		provider.configure(closure);
		resolve(provider);

		then:
		verify(captor, times(1)).capture(Closure.DELEGATE_FIRST);
	}

	@Test
	void configuresDelegateToProviderValue() {
		val captor = mock(CapturedValue.class);
		val closure = new ClosureActionAdapter<T>(this) {
			@Override
			public void doCall(T t) {
				captor.capture(getDelegate());
			}
		};
		val provider = createSubject();

		when:
		provider.configure(closure);
		val value = resolve(provider);

		then:
		verify(captor, times(1)).capture(value);
	}
}
