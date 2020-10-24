package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectContainer;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.dsl.GroovyDslContainerInvoker;
import dev.nokee.model.internal.dsl.GroovyDslInvoker;
import groovy.lang.GroovyObjectSupport;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.descendentOf;
import static dev.nokee.utils.ActionUtils.onlyIf;
import static dev.nokee.utils.TransformerUtils.toSetTransformer;

public abstract class AbstractDomainObjectContainer<TYPE, T extends TYPE> extends GroovyObjectSupport implements DomainObjectContainer<T>, HasConfigureElementByNameSupport<T> {
	private final DomainObjectIdentifier owner;
	private final Class<T> elementType;
	private final PolymorphicDomainObjectInstantiator<TYPE> instantiator;
	private final DomainObjectConfigurer<TYPE> configurer;
	private final DomainObjectEventPublisher eventPublisher;
	private final DomainObjectProviderFactory<TYPE> providerFactory;
	private final RealizableDomainObjectRepository<TYPE> repository;
	private final KnownDomainObjectFactory<TYPE> knownObjectFactory;
	private final DisallowChangesTransformer<Set<TYPE>> disallowChangesTransformer = new DisallowChangesTransformer<>();
	private final Map<Class<? extends T>, Class<? extends T>> bindings = new HashMap<>();
	private final GroovyDslInvoker<T> invoker;

	protected AbstractDomainObjectContainer(DomainObjectIdentifier owner, Class<T> elementType, PolymorphicDomainObjectInstantiator<TYPE> instantiator, DomainObjectConfigurer<TYPE> configurer, DomainObjectEventPublisher eventPublisher, DomainObjectProviderFactory<TYPE> providerFactory, RealizableDomainObjectRepository<TYPE> repository, KnownDomainObjectFactory<TYPE> knownObjectFactory) {
		this.owner = owner;
		this.elementType = elementType;
		this.instantiator = instantiator;
		this.configurer = configurer;
		this.eventPublisher = eventPublisher;
		this.providerFactory = providerFactory;
		this.repository = repository;
		this.knownObjectFactory = knownObjectFactory;
		this.invoker = new GroovyDslContainerInvoker<>(this, owner, elementType, repository, configurer, new DomainObjectRegistry<T>() {
			@Override
			public <S extends T> DomainObjectProvider<S> register(String name, Class<S> type) {
				return AbstractDomainObjectContainer.this.register(name, type);
			}

			@Override
			public <S extends T> DomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action) {
				return AbstractDomainObjectContainer.this.register(name, type, action);
			}
		});
	}

	public Class<T> getElementType() {
		return elementType;
	}

	@Override
	public <U extends T> DomainObjectProvider<U> register(String name, Class<U> type) {
		disallowChangesTransformer.assertChangesAllowed();
		val identifier = newIdentifier(name, toImplementationType(type));
		doRegister(identifier);
		return providerFactory.create(identifier);
	}

	@Override
	public <U extends T> DomainObjectProvider<U> register(String name, Class<U> type, Action<? super U> action) {
		disallowChangesTransformer.assertChangesAllowed();
		val identifier = newIdentifier(name, toImplementationType(type));
		doRegister(identifier);
		configurer.configure(identifier, action);

		return providerFactory.create(identifier);
	}

	@SuppressWarnings("unchecked")
	private <U extends T> Class<U> toImplementationType(Class<U> type) {
		return (Class<U>) bindings.getOrDefault(type, type);
	}

	private <U extends T> void doRegister(TypeAwareDomainObjectIdentifier<U> identifier) {
		instantiator.assertCreatableType(identifier.getType());
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier));
		eventPublisher.publish(new RealizableDomainObjectDiscovered(identifier, () -> {
			val instance = instantiator.newInstance(identifier, identifier.getType());
			eventPublisher.publish(new DomainObjectCreated<>(identifier, instance));
		}));
	}

	protected abstract <U extends T> TypeAwareDomainObjectIdentifier<U> newIdentifier(String name, Class<U> type);

	@Override
	public <U extends T> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory) {
		instantiator.registerFactory(type, factory);
	}

	@Override
	public <U extends T> void registerBinding(Class<U> type, final Class<? extends U> implementationType) {
		instantiator.registerBinding(type, implementationType);
		bindings.put(type, implementationType);
	}

	@Override
	public void configureEach(Action<? super T> action) {
		configurer.configureEach(owner, elementType, action);
	}

	@Override
	public <U extends T> void configureEach(Class<U> type, Action<? super U> action) {
		configurer.configureEach(owner, type, action);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		configurer.configureEach(owner, elementType, onlyIf(spec, action));
	}

	public Provider<Set<T>> getElements() {
		return repository.filtered(descendentOf(owner)).map(disallowChangesTransformer).map(toSetTransformer(elementType));
	}

	public AbstractDomainObjectContainer<TYPE, T> disallowChanges() {
		disallowChangesTransformer.disallowChanges();
		return this;
	}

	protected <U extends T> void doWhenElementKnown(Class<U> type, Action<? super KnownDomainObject<U>> action) {
		configurer.whenElementKnown(owner, type, identifier -> {
			action.execute(knownObjectFactory.create(identifier));
		});
	}

	//region configure by name/type
	public void configure(String name, Action<? super T> action) {
		configurer.configure(owner, name, elementType, action);
	}

	public <S extends T> void configure(String name, Class<S> type, Action<? super S> action) {
		configurer.configure(owner, name, type, action);
	}
	//endregion

	@Override
	public Object invokeMethod(String name, Object args) {
		return invoker.invokeMethod(name, args);
	}
}
