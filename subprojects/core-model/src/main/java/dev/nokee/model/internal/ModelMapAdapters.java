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
import org.gradle.api.DomainObjectSet;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Namer;
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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
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
			final PolymorphicDomainObjectRegistry<Project> registry = new PolymorphicDomainObjectRegistry<Project>() {
				@Override
				@SuppressWarnings("unchecked")
				public <S extends Project> NamedDomainObjectProvider<S> register(String name, Class<S> type) throws InvalidUserDataException {
					assert name.equals(project.getName());
					assert type.equals(Project.class);
					delegate.add(project);
					return (NamedDomainObjectProvider<S>) delegate.named(project.getName());
				}

				@Override
				@SuppressWarnings("unchecked")
				public <S extends Project> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
					assert name.equals(project.getName());
					assert type.equals(Project.class);
					delegate.add(project);
					return (NamedDomainObjectProvider<S>) delegate.named(project.getName());
				}

				@Override
				public RegistrableTypes getRegistrableTypes() {
					return Project.class::equals;
				}
			};
			this.delegate = new BaseModelMap<>(Project.class, registry, knownElements, discoveredElements, onFinalize, delegate, null, providers, objects);
			this.delegate.register(ProjectIdentifier.of(project), Project.class);
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

	interface Elements<ElementType> {
		void configureEach(Action<? super ElementType> configureAction);
	}

	private static final class GradleCollectionElements<ElementType> implements Elements<ElementType> {
		private final NamedDomainObjectCollection<ElementType> collection;

		private GradleCollectionElements(NamedDomainObjectCollection<ElementType> collection) {
			this.collection = collection;
		}

		@Override
		public void configureEach(Action<? super ElementType> configureAction) {
			collection.configureEach(configureAction);
		}
	}

	private static final class FilterCollectionAdapter<ElementType> implements GradleCollection<ElementType> {
		private final Set<String> knownElements = new HashSet<>();
		private final Namer<ElementType> namer;
		private final Elements<ElementType> collection;
		private final GradleCollection<ElementType> delegate;

		private FilterCollectionAdapter(Namer<ElementType> namer, GradleCollection<ElementType> delegate) {
			this.namer = namer;
			this.delegate = delegate;
			this.collection = new Elements<ElementType>() {
				@Override
				public void configureEach(Action<? super ElementType> configureAction) {
					delegate.getElements().configureEach(new OnlyIfKnownAction<>(configureAction));
				}
			};
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
			knownElements.add(identity.getName());
			return delegate.register(identity);
		}

		@Override
		public Elements<ElementType> getElements() {
			return collection;
		}

		@Override
		public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			delegate.whenElementFinalized(new OnlyIfKnownAction<>(finalizeAction));
		}

		private final class OnlyIfKnownAction<T extends ElementType> implements Action<T> {
			private final Action<? super T> delegate;

			public OnlyIfKnownAction(Action<? super T> delegate) {
				this.delegate = delegate;
			}

			@Override
			public void execute(T t) {
				if (knownElements.contains(namer.determineName(t))) {
					delegate.execute(t);
				}
			}
		}
	}

	private static final class GradleCollectionAdapter<ElementType> implements GradleCollection<ElementType> {
		private final PolymorphicDomainObjectRegistry<ElementType> registry;
		private final Elements<ElementType> collection;
		private final ModelElementFinalizer finalizer;

		private GradleCollectionAdapter(PolymorphicDomainObjectRegistry<ElementType> registry, Elements<ElementType> collection, ModelElementFinalizer finalizer) {
			this.registry = registry;
			this.collection = collection;
			this.finalizer = finalizer;
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
			final NamedDomainObjectProvider<RegistrableType> provider = registry.registerIfAbsent(identity.getName(), identity.getType().getConcreteType());
			return new DefaultModelObject<>(provider);
		}

		@Override
		public Elements<ElementType> getElements() {
			return collection;
		}

		@Override
		public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			finalizer.accept(() -> collection.configureEach(finalizeAction));
		}

		private final class DefaultModelObject<ObjectType> implements ModelObject<ObjectType> {
			private final NamedDomainObjectProvider<ObjectType> provider;

			private DefaultModelObject(NamedDomainObjectProvider<ObjectType> provider) {
				this.provider = provider;
			}

			@Override
			public NamedDomainObjectProvider<ObjectType> asProvider() {
				return provider;
			}

			@Override
			public ObjectType get() {
				return provider.get();
			}

			@Override
			public ModelObject<ObjectType> configure(Action<? super ObjectType> configureAction) {
				provider.configure(configureAction);
				return this;
			}

			@Override
			public ModelObject<ObjectType> whenFinalized(Action<? super ObjectType> finalizeAction) {
				finalizer.accept(() -> provider.configure(finalizeAction));
				return this;
			}

			@Override
			public String getName() {
				return provider.getName();
			}
		}
	}

	private interface GradleCollection<ElementType> {
		<RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity);

		Elements<ElementType> getElements();

		void whenElementFinalized(Action<? super ElementType> finalizeAction);
	}

	private interface ModelMapElementsProviderFactory {
		<T> Provider<Set<T>> provider(Consumer<? super Builder<T>> action);

		interface Builder<T> {
			Builder<T> add(Provider<? extends T> provider);
		}
	}

	private static final class DefaultModelMapElementsProviderFactory implements ModelMapElementsProviderFactory {
		private final ProviderFactory providers;
		private final ObjectFactory objects;

		private DefaultModelMapElementsProviderFactory(ProviderFactory providers, ObjectFactory objects) {
			this.providers = providers;
			this.objects = objects;
		}

		@Override
		@SuppressWarnings({"unchecked", "UnstableApiUsage"})
		public <T> Provider<Set<T>> provider(Consumer<? super Builder<T>> action) {
			return providers.provider(() -> {
				final SetProperty<Object> result = objects.setProperty(Object.class);
				action.accept(new Builder<T>() {
					@Override
					public Builder<T> add(Provider<? extends T> provider) {
						result.add(provider);
						return this;
					}
				});

				return (Provider<? extends Set<T>>) result;
			}).flatMap(noOpTransformer());
		}
	}

	private static final class DefaultModelMapStrategy<ElementType> implements ModelMapStrategy<ElementType> {
		private final Class<ElementType> elementType;
		private final Set<ModelObjectIdentifier> knownIdentifiers = new HashSet<>();
		private final KnownElements legacyKnownElements;
		private final ModelMapElementsProviderFactory providers;
		private final GradleCollection<ElementType> delegate;
		private final DomainObjectSet<KnownModelObject<ElementType>> knownElements;

		@SuppressWarnings({"unchecked", "UnstableApiUsage"})
		private DefaultModelMapStrategy(Class<ElementType> elementType, KnownElements knownElements, ProviderFactory providers, ObjectFactory objects, GradleCollection<ElementType> delegate) {
			this.elementType = elementType;
			this.legacyKnownElements = knownElements;
			this.providers = new DefaultModelMapElementsProviderFactory(providers, objects);
			this.delegate = delegate;
			this.knownElements = (DomainObjectSet<KnownModelObject<ElementType>>) objects.domainObjectSet(new TypeToken<KnownModelObject<ElementType>>() {}.where(new TypeParameter<ElementType>() {}, elementType).getRawType());

			knownElements.forEach(it -> this.knownElements.add(new MyKnownModelObject<>(it)));
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
			knownIdentifiers.add(identity.getIdentifier());
			return legacyKnownElements.register(identity, delegate::register);
		}

		@Override
		public void configureEach(Action<? super ElementType> configureAction) {
			delegate.getElements().configureEach(configureAction);
		}

		@Override
		public void whenElementKnown(Action<? super KnownModelObject<ElementType>> configureAction) {
			knownElements.all(configureAction);
		}

		@Override
		public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			delegate.whenElementFinalized(finalizeAction);
		}

		@Override
		public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
			assert knownIdentifiers.contains(identifier);
			return legacyKnownElements.getById(identifier, elementType);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U> Provider<Set<U>> getElements(Class<U> type, Spec<? super ModelObjectIdentity<?>> spec) {
			return providers.provider(builder -> {
				knownElements.forEach(it -> {
					if (it.getType().isSubtypeOf(type) && spec.isSatisfiedBy(ofIdentity(it.getIdentifier(), it.getType()))) {
						builder.add((Provider<? extends U>) it.asProvider());
					}
				});
			});
		}
	}

	private static final class BaseModelMap<ElementType> implements ModelMap<ElementType>, ModelObjectRegistry<ElementType> {
		private final ModelMapStrategy<ElementType> strategy;
		private final ContextualModelObjectIdentifier identifierFactory;
		private final RegistrableTypes registrableTypes;

		private BaseModelMap(Class<ElementType> elementType, PolymorphicDomainObjectRegistry<ElementType> registry, KnownElements knownElements, DiscoveredElements discoveredElements, ModelElementFinalizer finalizer, NamedDomainObjectSet<ElementType> delegate, ContextualModelObjectIdentifier identifierFactory, ProviderFactory providers, ObjectFactory objects) {
			this.strategy = new DiscoverableModelMapStrategy<>(discoveredElements, providers, new DefaultModelMapStrategy<>(elementType, knownElements, providers, objects, new FilterCollectionAdapter<>(delegate.getNamer(), new GradleCollectionAdapter<>(registry, new GradleCollectionElements<>(delegate), finalizer))));
			this.identifierFactory = identifierFactory;
			this.registrableTypes = registry.getRegistrableTypes()::canRegisterType;
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return strategy.register(ofIdentity(identifier, type));
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return registrableTypes;
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
