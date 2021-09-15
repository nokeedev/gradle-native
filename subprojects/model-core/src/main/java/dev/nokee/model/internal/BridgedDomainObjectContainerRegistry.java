package dev.nokee.model.internal;

import dev.nokee.model.registry.ModelRegistry;
import dev.nokee.utils.NamedDomainObjectCollectionUtils;
import dev.nokee.utils.SpecUtils;
import lombok.Value;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.specs.Spec;

import java.util.Optional;
import java.util.function.Supplier;

final class BridgedDomainObjectContainerRegistry<T> extends NamedDomainObjectContainerRegistry<T> implements Action<NamedDomainObjectCollectionUtils.KnownElement<T>> {
	private static final ThreadLocal<Spec<NamedDomainObjectCollectionUtils.KnownElement<?>>> CURRENT_DOMAIN_OBJECT_SPEC = new ThreadLocal<>();
	private final NamedDomainObjectContainerRegistry<T> delegate;
	private final Action<NamedDomainObjectCollectionUtils.KnownElement<T>> whenElementKnown;

	public BridgedDomainObjectContainerRegistry(NamedDomainObjectContainerRegistry<T> delegate, ModelRegistry modelRegistry) {
		this.delegate = delegate;
		this.whenElementKnown = new RegisterModelProjection<>(modelRegistry);
	}

	@Override
	public <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type) {
		return doRegister(name, type, () -> delegate.register(name, type));
	}

	@Override
	public <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action) {
		return doRegister(name, type, () -> delegate.register(name, type, action));
	}

	@Override
	public <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
		return doRegister(name, type, () -> delegate.registerIfAbsent(name, type));
	}

	@Override
	public <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type, Action<? super S> action) {
		return doRegister(name, type, () -> delegate.registerIfAbsent(name, type, action));
	}

	private static <T> NamedDomainObjectProvider<T> doRegister(String name, Class<T> type, Supplier<? extends NamedDomainObjectProvider<T>> supplier) {
		val previous = CURRENT_DOMAIN_OBJECT_SPEC.get();
		CURRENT_DOMAIN_OBJECT_SPEC.set(new DomainObjectSpec(name, type));
		try {
			return supplier.get();
		} finally {
			CURRENT_DOMAIN_OBJECT_SPEC.set(previous);
		}
	}

	@Override
	public NamedDomainObjectRegistry.RegistrableTypes getRegistrableTypes() {
		return delegate.getRegistrableTypes();
	}

	@Override
	public Optional<NamedDomainObjectContainer<T>> getContainer() {
		return delegate.getContainer();
	}

	@Override
	public void execute(NamedDomainObjectCollectionUtils.KnownElement<T> knownElement) {
		doNotExecuteIfPreKnown(knownElement, whenElementKnown);
	}

	private static <T> void doNotExecuteIfPreKnown(NamedDomainObjectCollectionUtils.KnownElement<T> knownElement, Action<NamedDomainObjectCollectionUtils.KnownElement<T>> action) {
		val spec = Optional.ofNullable(CURRENT_DOMAIN_OBJECT_SPEC.get()).orElse(SpecUtils.satisfyNone());
		if (!spec.isSatisfiedBy(knownElement)) {
			action.execute(knownElement);
		}
	}

	@Value
	private static class DomainObjectSpec implements Spec<NamedDomainObjectCollectionUtils.KnownElement<?>> {
		String name;
		Class<?> type;

		@Override
		public boolean isSatisfiedBy(NamedDomainObjectCollectionUtils.KnownElement<?> element) {
			return element.getName().equals(name) && element.getType().equals(type);
		}
	}
}
