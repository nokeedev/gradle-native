/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nokee.model.internal;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import dev.nokee.internal.Factory;
import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.ConfigurationRegistry;
import dev.nokee.model.ExtensiblePolymorphicDomainObjectContainerRegistry;
import dev.nokee.model.PolymorphicDomainObjectRegistry;
import dev.nokee.model.TaskRegistry;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.nokee.model.internal.ModelObjectIdentifiers.asFullyQualifiedName;
import static dev.nokee.model.internal.ModelObjectIdentity.ofIdentity;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;

public final class ModelMapAdapters {
	private ModelMapAdapters() {}

	private interface ForwardingModelMap<ElementType> extends ModelMap<ElementType> {
		ModelMap<ElementType> delegate();

		@Override
		default <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ElementName name, Class<RegistrableType> type) {
			return delegate().register(name, type);
		}

		@Override
		default void configureEach(Action<? super ElementType> configureAction) {
			delegate().configureEach(configureAction);
		}

		@Override
		default <U> void configureEach(Class<U> type, Action<? super U> configureAction) {
			delegate().configureEach(type, configureAction);
		}

		@Override
		default void whenElementKnown(Action<? super KnownModelObject<ElementType>> configureAction) {
			delegate().whenElementKnown(configureAction);
		}

		@Override
		default <U> void whenElementKnown(Class<U> type, Action<? super KnownModelObject<U>> configureAction) {
			delegate().whenElementKnown(type, configureAction);
		}

		@Override
		default void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			delegate().whenElementFinalized(finalizeAction);
		}

		@Override
		default <U> void whenElementFinalized(Class<U> type, Action<? super U> finalizeAction) {
			delegate().whenElementFinalized(type, finalizeAction);
		}

		@Override
		default ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
			return delegate().getById(identifier);
		}

		@Override
		default <U> Provider<Set<U>> getElements(Class<U> type, Spec<? super ModelObjectIdentity<?>> spec) {
			return delegate().getElements(type, spec);
		}
	}

	private interface ForwardingModelObjectRegistry<ElementType> extends ModelObjectRegistry<ElementType> {
		ModelObjectRegistry<ElementType> delegate();

		@Override
		default <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return delegate().register(identifier, type);
		}

		@Override
		default RegistrableTypes getRegistrableTypes() {
			return delegate().getRegistrableTypes();
		}
	}

	// We need this implementation to access a NamedDomainObjectProvider<Project>
	public static /*final*/ class ForProject implements ForwardingModelMap<Project> {
		private final BaseModelMap<Project> delegate;

		@Inject
		public ForProject(NamedDomainObjectSet<Project> delegate, Project project, KnownElements knownElements, DiscoveredElements discoveredElements, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize) {
			this.delegate = new BaseModelMap<>(Project.class, null, knownElements, discoveredElements, onFinalize, delegate, null, providers, objects);
			knownElements.register(ofIdentity(ProjectIdentifier.of(project), Project.class), new PolymorphicDomainObjectRegistry<Project>() {
				@Override
				@SuppressWarnings("unchecked")
				public <S extends Project> NamedDomainObjectProvider<S> register(String name, Class<S> type) throws InvalidUserDataException {
					assert type.equals(Project.class);
					delegate.add(project);
					return (NamedDomainObjectProvider<S>) delegate.named(project.getName());
				}

				@Override
				@SuppressWarnings("unchecked")
				public <S extends Project> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
					assert type.equals(Project.class);
					delegate.add(project);
					return (NamedDomainObjectProvider<S>) delegate.named(project.getName());
				}

				@Override
				public RegistrableTypes getRegistrableTypes() {
					return Project.class::equals;
				}
			});
		}

		@Override
		public ModelMap<Project> delegate() {
			return delegate;
		}

		@Override
		public <RegistrableType extends Project> ModelObject<RegistrableType> register(ElementName name, Class<RegistrableType> type) {
			throw new UnsupportedOperationException("registering Gradle Project is not supported");
		}
	}

	public interface ForNamedDomainObjectContainer<ElementType> extends ModelObjectRegistry<ElementType>, ModelMap<ElementType> {}

	public static /*final*/ class ForConfigurationContainer implements ForwardingModelMap<Configuration>, ForwardingModelObjectRegistry<Configuration>, ForNamedDomainObjectContainer<Configuration>, HasPublicType {
		private final BaseModelMap<Configuration> delegate;

		@Inject
		public ForConfigurationContainer(ConfigurationContainer delegate, KnownElements knownElements, DiscoveredElements discoveredElements, Project project, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize) {
			this.delegate = new BaseModelMap<>(Configuration.class, new ConfigurationRegistry(delegate), knownElements, discoveredElements, onFinalize, delegate, new ContextualModelObjectIdentifier(ProjectIdentifier.of(project)), providers, objects);
		}

		@Override
		public BaseModelMap<Configuration> delegate() {
			return delegate;
		}

		@Override
		@SuppressWarnings("UnstableApiUsage")
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(new TypeToken<ForNamedDomainObjectContainer<Configuration>>() {}.getType());
		}
	}

	public interface ForPolymorphicDomainObjectContainer<ElementType> extends ModelObjectRegistry<ElementType>, ModelMap<ElementType> {}

	public static /*final*/ class ForTaskContainer implements ForwardingModelMap<Task>, ForwardingModelObjectRegistry<Task>, ForPolymorphicDomainObjectContainer<Task>, HasPublicType {
		private final BaseModelMap<Task> delegate;

		@Inject
		public ForTaskContainer(TaskContainer delegate, KnownElements knownElements, DiscoveredElements discoveredElements, Project project, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize) {
			this.delegate = new BaseModelMap<>(Task.class, new TaskRegistry(delegate), knownElements, discoveredElements, onFinalize, delegate, new ContextualModelObjectIdentifier(ProjectIdentifier.of(project)), providers, objects);
		}

		@Override
		public BaseModelMap<Task> delegate() {
			return delegate;
		}

		@Override
		@SuppressWarnings("UnstableApiUsage")
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(new TypeToken<ForPolymorphicDomainObjectContainer<Task>>() {}.getType());
		}
	}

	public interface ContextualModelElementInstantiator {
		<S> Function<DefaultKnownElements.KnownElement, S> newInstance(Factory<S> factory);
	}

	public static /*final*/ class ForExtensiblePolymorphicDomainObjectContainer<ElementType> implements ForwardingModelMap<ElementType>, ForwardingModelObjectRegistry<ElementType>, ModelObjectRegistry<ElementType>, ModelObjectFactoryRegistry<ElementType>, HasPublicType {
		private final Class<ElementType> elementType;
		private final ExtensiblePolymorphicDomainObjectContainerRegistry<ElementType> registry;
		private final DefaultKnownElements knownElementsEx;
		private final ManagedFactoryProvider managedFactory;
		private final ContextualModelElementInstantiator elementInstantiator;
		private final BaseModelMap<ElementType> delegate;

		@Inject
		public ForExtensiblePolymorphicDomainObjectContainer(Class<ElementType> elementType, ExtensiblePolymorphicDomainObjectContainer<ElementType> delegate, Instantiator instantiator, DefaultKnownElements knownElementsEx, DiscoveredElements discoveredElements, ContextualModelElementInstantiator elementInstantiator, Project project, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize) {
			this.delegate = new BaseModelMap<>(elementType, new ExtensiblePolymorphicDomainObjectContainerRegistry<>(delegate), knownElementsEx, discoveredElements, onFinalize, delegate, new ContextualModelObjectIdentifier(ProjectIdentifier.of(project)), providers, objects);
			this.elementType = elementType;
			this.knownElementsEx = knownElementsEx;
			this.managedFactory = new ManagedFactoryProvider(instantiator);
			this.elementInstantiator = elementInstantiator;
			this.registry = new ExtensiblePolymorphicDomainObjectContainerRegistry<>(delegate);
		}

		@Override
		public BaseModelMap<ElementType> delegate() {
			return delegate;
		}

		@Override
		public <U extends ElementType> void registerFactory(Class<U> type, NamedDomainObjectFactory<? extends U> factory) {
			registry.registerFactory(type, newFactory(type, factory));
		}

		private <U extends ElementType> NamedDomainObjectFactory<U> newFactory(Class<U> type, NamedDomainObjectFactory<? extends U> delegate) {
			return name -> knownElementsEx.create(name, type, elementInstantiator.newInstance((Factory<U>) () -> delegate.create(name)));
		}

		@Override
		public <U extends ElementType> void registerFactory(Class<U> type) {
			registerFactory(type, managedFactory.create(type));
		}

		@Override
		@SuppressWarnings("UnstableApiUsage")
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(new TypeToken<ForExtensiblePolymorphicDomainObjectContainer<ElementType>>() {}.where(new TypeParameter<ElementType>() {}, elementType).getType());
		}
	}

	public interface RealizeListener {
		void onRealize(ModelObjectIdentity<?> identity);
	}

	public static final class ModelElementIdentity implements ModelObject<Object> {
		private final ModelObjectIdentity<?> identity;
		private final ProviderFactory providers;
		private final ElementProvider elementProviderEx;
		private final DiscoveredElements discoveredElements;
		private final ModelElementFinalizer onFinalize;
		private final RealizeListener realizeListener;

		// TODO: Reduce visibility
		ModelElementIdentity(ModelObjectIdentity<?> identity, ProviderFactory providers, ElementProvider elementProvider, DiscoveredElements discoveredElements, RealizeListener realizeListener, ModelElementFinalizer onFinalize) {
			this.identity = identity;
			this.providers = providers;
			this.elementProviderEx = elementProvider;
			this.discoveredElements = discoveredElements;
			this.onFinalize = onFinalize;
			this.realizeListener = realizeListener;
		}

		public String getName() {
			return asFullyQualifiedName(identity.getIdentifier()).toString();
		}

		public ModelObjectIdentifier getIdentifier() {
			return identity.getIdentifier();
		}

		public boolean instanceOf(Class<?> type) {
			return identity.getType().isSubtypeOf(type);
		}

		@SuppressWarnings("unchecked")
		public <T> ModelObject<T> asModelObject(Class<T> type) {
			return (ModelObject<T>) this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public NamedDomainObjectProvider<Object> asProvider() {
			return (NamedDomainObjectProvider<Object>) elementProviderEx.named(getName(), identity.getType().getConcreteType());
		}

		@Override
		public Object get() {
			return asProvider().get();
		}

		public void realizeNow() {
			realizeListener.onRealize(identity);
		}

		@Override
		public ModelObject<Object> configure(Action<? super Object> configureAction) {
			// TODO: notify the action execute for a specific Key
			discoveredElements.onRealized(configureAction, a -> elementProviderEx.configure(getName(), identity.getType().getConcreteType(), a));
			return this;
		}

		@Override
		public ModelObject<Object> whenFinalized(Action<? super Object> finalizeAction) {
			// TODO: notify the action execute for a specific Key
			discoveredElements.onFinalized(finalizeAction, a -> onFinalize.accept(() -> configure(a)));
			return this;
		}

		@Override
		public String toString() {
			return "object '" + asFullyQualifiedName(identity.getIdentifier()) + "' (" + identity.getType().getConcreteType().getSimpleName() + ")";
		}

		public static final class ElementProvider {
			private final NamedDomainObjectCollection<Object> collection;

			@SuppressWarnings("unchecked")
			public ElementProvider(NamedDomainObjectCollection<?> collection) {
				this.collection = (NamedDomainObjectCollection<Object>) collection;
			}

			public <S> NamedDomainObjectProvider<S> named(String name, Class<S> type) {
				return collection.named(name, type);
			}

			public <S> void configure(String name, Class<S> type, Action<? super S> configureAction) {
				if (collection.getNames().contains(name)) {
					collection.named(name, type, configureAction);
				} else {
					collection.configureEach(it -> {
						if (collection.getNamer().determineName(it).equals(name)) {
							configureAction.execute(type.cast(it));
						}
					});
				}
			}
		}

		public static final class Factory {
			private final ProviderFactory providers;
			private final ElementProvider elementProvider;
			private final DiscoveredElements discoveredElements;
			private final ModelElementFinalizer onFinalize;

			public Factory(ProviderFactory providers, ElementProvider elementProvider, DiscoveredElements discoveredElements, ModelElementFinalizer onFinalize) {
				this.providers = providers;
				this.elementProvider = elementProvider;
				this.discoveredElements = discoveredElements;
				this.onFinalize = onFinalize;
			}

			public ModelElementIdentity create(ModelObjectIdentity<?> identity, RealizeListener realizeListener) {
				return new ModelElementIdentity(identity, providers, elementProvider, discoveredElements, realizeListener, onFinalize);
			}
		}
	}

	private static final class ContextualModelObjectIdentifier {
		private final ModelObjectIdentifier baseIdentifier;

		private ContextualModelObjectIdentifier(ModelObjectIdentifier baseIdentifier) {
			this.baseIdentifier = baseIdentifier;
		}

		public <R> R create(ElementName elementName, Function<? super ModelObjectIdentifier, ? extends R> action) {
			return action.apply(baseIdentifier.child(elementName));
		}
	}

	private static final class ManagedFactoryProvider {
		private final Instantiator instantiator;

		public ManagedFactoryProvider(Instantiator instantiator) {
			this.instantiator = instantiator;
		}

		public <T> NamedDomainObjectFactory<T> create(Class<T> type) {
			return __ -> instantiator.newInstance(type);
		}
	}

	private static class MyKnownModelObject<ElementType> implements KnownModelObject<ElementType> {
		private final ModelElementIdentity it;

		public MyKnownModelObject(ModelElementIdentity it) {
			this.it = it;
		}

		@Override
		public ModelObjectIdentifier getIdentifier() {
			return it.getIdentifier();
		}

		@Override
		public ModelType<?> getType() {
			return it.identity.getType();
		}

		@Override
		@SuppressWarnings("unchecked")
		public KnownModelObject<ElementType> configure(Action<? super ElementType> configureAction) {
			((ModelObject<ElementType>) it.asModelObject(it.identity.getType().getConcreteType())).configure(configureAction);
			return this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public KnownModelObject<ElementType> whenFinalized(Action<? super ElementType> finalizeAction) {
			((ModelObject<ElementType>) it.asModelObject(it.identity.getType().getConcreteType())).whenFinalized(finalizeAction);
			return this;
		}

		@Override
		public void realizeNow() {
			it.realizeNow();
		}

		@Override
		@SuppressWarnings("unchecked")
		public Provider<ElementType> asProvider() {
			return it.providers.provider(it::asProvider).flatMap(it -> (Provider<ElementType>) it);
		}

		@Override
		public String getName() {
			return it.getName();
		}
	}

	private static final class DefaultModelMapStrategy<ElementType> implements ModelMapStrategy<ElementType> {
		private final Class<ElementType> elementType;
		private final PolymorphicDomainObjectRegistry<ElementType> registry;
		private final KnownElements knownElements;
		private final ModelElementFinalizer onFinalize;
		private final NamedDomainObjectSet<ElementType> delegate;
		private final ProviderFactory providers;
		private final ObjectFactory objects;

		private DefaultModelMapStrategy(Class<ElementType> elementType, PolymorphicDomainObjectRegistry<ElementType> registry, KnownElements knownElements, ModelElementFinalizer onFinalize, NamedDomainObjectSet<ElementType> delegate, ProviderFactory providers, ObjectFactory objects) {
			this.elementType = elementType;
			this.registry = registry;
			this.knownElements = knownElements;
			this.onFinalize = onFinalize;
			this.delegate = delegate;
			this.providers = providers;
			this.objects = objects;
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
			return knownElements.register(identity, registry);
		}

		@Override
		public ModelObjectRegistry.RegistrableTypes getRegistrableTypes() {
			return registry.getRegistrableTypes()::canRegisterType;
		}

		@Override
		public void configureEach(Action<? super ElementType> configureAction) {
			delegate.configureEach(onlyKnown(configureAction));
		}

		@Override
		public void whenElementKnown(Action<? super KnownModelObject<ElementType>> configureAction) {
			knownElements.forEach(it -> configureAction.execute(new MyKnownModelObject<>(it)));
		}

		@Override
		public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			onFinalize.accept(() -> delegate.configureEach(onlyKnown(finalizeAction)));
		}

		@Override
		public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
			return knownElements.getById(identifier, elementType);
		}

		@Override
		public <U> Provider<Set<U>> getElements(Class<U> type, Spec<? super ModelObjectIdentity<?>> spec) {
			return providers.provider(() -> {
				final SetProperty<U> result = objects.setProperty(type);
				knownElements.forEach(it -> {
					if (it.identity.getType().isSubtypeOf(type) && spec.isSatisfiedBy(it.identity)) {
						result.add(it.asModelObject(type).asProvider());
					}
				});

				return result;
			}).flatMap(noOpTransformer());
		}

		private <T> Action<T> onlyKnown(Action<? super T> action) {
			return new ModelElementAction<>(new OnlyIfKnownAction<>(action));
		}

		private final class OnlyIfKnownAction<T> implements BiConsumer<ModelElement, T> {
			private final Action<? super T> delegate;

			public OnlyIfKnownAction(Action<? super T> delegate) {
				this.delegate = delegate;
			}

			@Override
			public void accept(ModelElement element, T t) {
				if (knownElements.isKnown(element.getIdentifier(), elementType)) {
					delegate.execute(t);
				}
			}
		}
	}

	private static final class BaseModelMap<ElementType> implements ModelMap<ElementType>, ModelObjectRegistry<ElementType> {
		private final ModelMapStrategy<ElementType> strategy;
		private final ContextualModelObjectIdentifier identifierFactory;

		private BaseModelMap(Class<ElementType> elementType, PolymorphicDomainObjectRegistry<ElementType> registry, KnownElements knownElements, DiscoveredElements discoveredElements, ModelElementFinalizer onFinalize, NamedDomainObjectSet<ElementType> delegate, ContextualModelObjectIdentifier identifierFactory, ProviderFactory providers, ObjectFactory objects) {
			this.strategy = new DiscoverableModelMapStrategy<>(discoveredElements, providers, new DefaultModelMapStrategy<>(elementType, registry, knownElements, onFinalize, delegate, providers, objects));
			this.identifierFactory = identifierFactory;
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return strategy.register(ofIdentity(identifier, type));
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return strategy.getRegistrableTypes();
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ElementName name, Class<RegistrableType> type) {
			return identifierFactory.create(name, identifier -> register(identifier, type));
		}

		@Override
		public void configureEach(Action<? super ElementType> configureAction) {
			strategy.configureEach(configureAction);
		}

		@Override
		public <U> void configureEach(Class<U> type, Action<? super U> configureAction) {
			strategy.configureEach(ofType(type, configureAction));
		}

		@Override
		public void whenElementKnown(Action<? super KnownModelObject<ElementType>> configureAction) {
			strategy.whenElementKnown(configureAction);
		}

		@Override
		public <U> void whenElementKnown(Class<U> type, Action<? super KnownModelObject<U>> configureAction) {
			strategy.whenElementKnown(ofType(new KnownModelObjectTypeOf<>(type), configureAction));
		}

		@Override
		public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			strategy.whenElementFinalized(finalizeAction);
		}

		@Override
		public <U> void whenElementFinalized(Class<U> type, Action<? super U> finalizeAction) {
			strategy.whenElementFinalized(ofType(type, finalizeAction));
		}

		@Override
		public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
			return strategy.getById(identifier);
		}

		@Override
		public <U> Provider<Set<U>> getElements(Class<U> type, Spec<? super ModelObjectIdentity<?>> spec) {
			return strategy.getElements(type, spec);
		}
	}
}
