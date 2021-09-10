package dev.nokee.model.internal;

import com.google.common.collect.Streams;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.internal.MutationGuard;
import org.gradle.api.internal.provider.ProviderInternal;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.stream.Collectors;

// Workaround https://github.com/gradle/gradle/issues/18109
@SuppressWarnings("rawtypes")
final class NamedDomainObjectProviderSelfMutationConfigureStrategy {
	private static final ThreadLocal<ArrayDeque<NamedDomainObjectProviderKey>> PROVIDER_UNDER_CONFIGURATION_BREAD_CRUMBS = ThreadLocal.withInitial(ArrayDeque::new);
	private final ArrayDeque<Action> actions = new ArrayDeque<>();
	private final MutationGuard guard;

	public NamedDomainObjectProviderSelfMutationConfigureStrategy(MutationGuard guard) {
		this.guard = guard;
	}

	public void configure(NamedDomainObjectProvider delegate, Action action) {
		val key = NamedDomainObjectProviderKey.of(delegate);

		// We don't necessarily want to execute the exact action that was "registered" because...
		//  Once a domain object is realized, the object is immediately added to the container before the actions are executed.
		//  In a self-mutating scenario, the action will execute immediately instead of being deferred to the end of the action chain.
		//  The result is an out of order mutation action execution.
		//  Here, we add the action to the action chain then add a special configure action to the provider.
		//  The configure action will simply execute the "next" action to execute.
		//  This will ensure the ordering of the action are respected in the grand scheme of things...
		//  It is not perfect but good enough.
		actions.addLast(action);
		if (PROVIDER_UNDER_CONFIGURATION_BREAD_CRUMBS.get().contains(key)) {
			// Only re-enable mutation when it's self-mutating, e.g. provider key in bread crumbs.
			withMutationEnabled(() -> tryConfigure(delegate, new ConfigureAction(key)));
		} else {
			tryConfigure(delegate, new ConfigureAction(key));
		}
	}

	private void withMutationEnabled(Runnable runnable) {
		guard.withMutationEnabled(ignored -> runnable.run()).execute(null);
	}

	@SuppressWarnings("unchecked")
	private void tryConfigure(NamedDomainObjectProvider delegate, Action action) {
		try {
			delegate.configure(action);
		} catch (RuntimeException ex) {
			throw providerConfigurationException(delegate, ex);
		}
	}

	private static RuntimeException providerConfigurationException(NamedDomainObjectProvider provider, Throwable cause) {
		return new RuntimeException(String.format("Configuration of provider '%s' (%s) failed: %s", provider.getName(),
			typeOf(provider).getSimpleName(), toBreadCrumbs(PROVIDER_UNDER_CONFIGURATION_BREAD_CRUMBS.get())), cause);
	}

	private static Class<?> typeOf(NamedDomainObjectProvider<?> provider) {
		return ((ProviderInternal<?>) provider).getType();
	}

	private static String toBreadCrumbs(Iterable<NamedDomainObjectProviderKey> keys) {
		return Streams.stream(keys).map(Objects::toString).collect(Collectors.joining(" > "));
	}

	private final class ConfigureAction implements Action {
		private final NamedDomainObjectProviderKey key;

		private ConfigureAction(NamedDomainObjectProviderKey key) {
			this.key = key;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void execute(Object o) {
			PROVIDER_UNDER_CONFIGURATION_BREAD_CRUMBS.get().addLast(key);
			try {
				actions.removeFirst().execute(o);
			} finally {
				PROVIDER_UNDER_CONFIGURATION_BREAD_CRUMBS.get().removeLast();
			}
		}
	}
}
