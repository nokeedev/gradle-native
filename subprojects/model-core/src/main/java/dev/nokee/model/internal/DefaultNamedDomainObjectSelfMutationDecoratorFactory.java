package dev.nokee.model.internal;

import com.google.common.collect.Streams;
import org.gradle.api.*;
import org.gradle.api.internal.MutationGuard;
import org.gradle.api.internal.MutationGuards;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.internal.provider.ValueSanitizer;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.DisplayName;
import org.gradle.internal.state.Managed;

import javax.annotation.Nullable;

final class DefaultNamedDomainObjectSelfMutationDecoratorFactory implements NamedDomainObjectProviderSelfMutationDecoratorFactory {
	@Override
	public NamedDomainObjectProviderDecorator forContainer(NamedDomainObjectContainer<?> container) {
		if (container instanceof TaskContainer) {
			return new DefaultTaskProviderDecorator(container);
		} else {
			return new DefaultNamedDomainObjectProviderDecorator(container);
		}
	}

	private static final class DefaultTaskProviderDecorator implements NamedDomainObjectProviderDecorator {
		private final MutationGuard guard;
		private final NamedDomainObjectContainer<?> container;

		public DefaultTaskProviderDecorator(NamedDomainObjectContainer<?> container) {
			this.guard = MutationGuards.of(container);
			this.container = container;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> NamedDomainObjectProvider<T> decorate(NamedDomainObjectProvider<T> provider) {
			if (Streams.stream(container.getCollectionSchema().getElements()).noneMatch(it -> it.getName().equals(provider.getName()))) {
				throw new UnsupportedOperationException(String.format("Cannot decorate %s.", provider));
			}
			return new DefaultTaskProvider(provider);
		}

		@SuppressWarnings("rawtypes")
		private final class DefaultTaskProvider extends AbstractNamedDomainObjectProvider implements TaskProvider {
			private DefaultTaskProvider(NamedDomainObjectProvider delegate) {
				super(delegate, new NamedDomainObjectProviderSelfMutationConfigureStrategy(guard));
			}
		}
	}

	private static final class DefaultNamedDomainObjectProviderDecorator implements NamedDomainObjectProviderDecorator {
		private final MutationGuard guard;
		private final NamedDomainObjectContainer<?> container;

		public DefaultNamedDomainObjectProviderDecorator(NamedDomainObjectContainer<?> container) {
			this.guard = MutationGuards.of(container);
			this.container = container;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> NamedDomainObjectProvider<T> decorate(NamedDomainObjectProvider<T> provider) {
			if (Streams.stream(container.getCollectionSchema().getElements()).noneMatch(it -> it.getName().equals(provider.getName()))) {
				throw new UnsupportedOperationException(String.format("Cannot decorate %s.", provider));
			}
			return new DefaultNamedDomainObjectProvider(provider);
		}

		private final class DefaultNamedDomainObjectProvider extends AbstractNamedDomainObjectProvider {
			private DefaultNamedDomainObjectProvider(NamedDomainObjectProvider delegate) {
				super(delegate, new NamedDomainObjectProviderSelfMutationConfigureStrategy(guard));
			}
		}
	}

	// Note: Implements Named interface because of DefaultNamedDomainObjectCollection.AbstractNamedDomainObjectProvider implementation
	@SuppressWarnings({"rawtypes", "unchecked"})
	private abstract static class AbstractNamedDomainObjectProvider implements NamedDomainObjectProvider, ProviderInternal, Managed, Named {
		protected final NamedDomainObjectProvider delegate;
		private final NamedDomainObjectProviderSelfMutationConfigureStrategy configureStrategy;

		protected AbstractNamedDomainObjectProvider(NamedDomainObjectProvider delegate, NamedDomainObjectProviderSelfMutationConfigureStrategy configureStrategy) {
			this.delegate = delegate;
			this.configureStrategy = configureStrategy;
		}

		@Override
		public final void configure(Action action) {
			configureStrategy.configure(delegate, action);
		}

		@Override
		public final String getName() {
			return delegate.getName();
		}

		@Nullable
		@Override
		public final Class<Object> getType() {
			return ((ProviderInternal) delegate).getType();
		}

		@Override
		public final ProviderInternal map(Transformer transformer) {
			return ((ProviderInternal) delegate).map(transformer);
		}

		@Override
		public final Value calculateValue() {
			return ((ProviderInternal) delegate).calculateValue();
		}

		@Override
		public final ProviderInternal asSupplier(DisplayName owner, Class targetType, ValueSanitizer sanitizer) {
			return ((ProviderInternal) delegate).asSupplier(owner, targetType, sanitizer);
		}

		@Override
		public final ProviderInternal withFinalValue() {
			return ((ProviderInternal) delegate).withFinalValue();
		}

		@Override
		public final boolean maybeVisitBuildDependencies(TaskDependencyResolveContext context) {
			return ((ProviderInternal) delegate).maybeVisitBuildDependencies(context);
		}

		@Override
		public final void visitProducerTasks(Action<? super Task> visitor) {
			((ProviderInternal) delegate).visitProducerTasks(visitor);
		}

		@Override
		public final boolean isValueProducedByTask() {
			return ((ProviderInternal) delegate).isValueProducedByTask();
		}

		@Override
		public final void visitDependencies(TaskDependencyResolveContext context) {
			((ProviderInternal) delegate).visitDependencies(context);
		}

		@Override
		public final Object get() {
			return delegate.get();
		}

		@Nullable
		@Override
		public final Object getOrNull() {
			return delegate.getOrNull();
		}

		@Override
		public final Object getOrElse(Object defaultValue) {
			return delegate.getOrElse(defaultValue);
		}

		@Override
		public final Provider flatMap(Transformer transformer) {
			return delegate.flatMap(transformer);
		}

		@Override
		public final boolean isPresent() {
			return delegate.isPresent();
		}

		@Override
		public final Provider orElse(Object value) {
			return delegate.orElse(value);
		}

		@Override
		public final Provider orElse(Provider provider) {
			return delegate.orElse(provider);
		}

		@Nullable
		@Override
		public final Object unpackState() {
			return ((Managed) delegate).unpackState();
		}

		@Override
		public final boolean isImmutable() {
			return ((Managed) delegate).isImmutable();
		}

		@Override
		public final Class<?> publicType() {
			return ((Managed) delegate).publicType();
		}

		@Override
		public final int getFactoryId() {
			return ((Managed) delegate).getFactoryId();
		}
	}
}
