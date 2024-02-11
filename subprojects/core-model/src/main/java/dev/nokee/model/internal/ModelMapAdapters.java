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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
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
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskContainer;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

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
		public ForProject(NamedDomainObjectSet<Project> delegate, Project project, DiscoveredElements discoveredElements, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize, ModelElementParents elementParents, SetProviderFactory setProviders) {
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
			this.delegate = new BaseModelMap<>(Project.class, registry, discoveredElements, onFinalize, delegate, null, providers, objects, elementParents, setProviders);
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
		public ForConfigurationContainer(ConfigurationContainer delegate, DiscoveredElements discoveredElements, Project project, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize, ModelElementParents elementParents, SetProviderFactory setProviders) {
			this.delegate = new BaseModelMap<>(Configuration.class, new ConfigurationRegistry(delegate), discoveredElements, onFinalize, delegate, new ContextualModelObjectIdentifier(ProjectIdentifier.of(project)), providers, objects, elementParents, setProviders);
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
		public ForTaskContainer(TaskContainer delegate, DiscoveredElements discoveredElements, Project project, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize, ModelElementParents elementParents, SetProviderFactory setProviders) {
			this.delegate = new BaseModelMap<>(Task.class, new TaskRegistry(delegate), discoveredElements, onFinalize, delegate, new ContextualModelObjectIdentifier(ProjectIdentifier.of(project)), providers, objects, elementParents, setProviders);
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

	interface ModelElementLookup {
		@Nullable
		ModelElement find(String name);
	}

	public static /*final*/ class ForExtensiblePolymorphicDomainObjectContainer<ElementType> implements ForwardingModelMap<ElementType>, ForwardingModelObjectRegistry<ElementType>, ModelObjectRegistry<ElementType>, ModelObjectFactoryRegistry<ElementType>, HasPublicType {
		private final Class<ElementType> elementType;
		private final ExtensiblePolymorphicDomainObjectContainerRegistry<ElementType> registry;
		private final ManagedFactoryProvider managedFactory;
		private final BaseModelMap<ElementType> delegate;

		@Inject
		public ForExtensiblePolymorphicDomainObjectContainer(Class<ElementType> elementType, ExtensiblePolymorphicDomainObjectContainer<ElementType> delegate, Instantiator instantiator, DiscoveredElements discoveredElements, Project project, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize, ModelElementParents elementParents, SetProviderFactory setProviders) {
			this.delegate = new BaseModelMap<>(elementType, new ExtensiblePolymorphicDomainObjectContainerRegistry<>(delegate), discoveredElements, onFinalize, delegate, new ContextualModelObjectIdentifier(ProjectIdentifier.of(project)), providers, objects, elementParents, setProviders);
			this.elementType = elementType;
			this.managedFactory = new ManagedFactoryProvider(instantiator);
			this.registry = new ExtensiblePolymorphicDomainObjectContainerRegistry<>(delegate);
		}

		@Override
		public BaseModelMap<ElementType> delegate() {
			return delegate;
		}

		@Override
		public <U extends ElementType> void registerFactory(Class<U> type, NamedDomainObjectFactory<? extends U> factory) {
			registry.registerFactory(type, new ModelObjectFactory<>(type, factory));
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

		private final class ModelObjectFactory<T extends ElementType> implements NamedDomainObjectFactory<T> {
			private final Class<T> type;
			private final NamedDomainObjectFactory<? extends T> delegate;

			private ModelObjectFactory(Class<T> type, NamedDomainObjectFactory<? extends T> delegate) {
				this.type = type;
				this.delegate = delegate;
			}

			@Override
			public T create(String name) {
				ModelElement element = delegate().find(name);
				if (element == null) {
					register(name, type);
					element = delegate().find(name);
				}
				return ModelElementSupport.newInstance(element, () -> delegate.create(name));
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

	// Responsible to limit configuration of domain objects to only those registered.
	//   It's important to limit the extent of the configuration to ensure lifecycle ordering.
	private static final class RegisteredOnlyModelElementContainer<ElementType> implements ModelElementContainer<ElementType> {
		private final Set<String> knownElements = new HashSet<>();
		private final Namer<ElementType> namer;
		private final ModelElementContainer<ElementType> delegate;

		private RegisteredOnlyModelElementContainer(Namer<ElementType> namer, ModelElementContainer<ElementType> delegate) {
			this.namer = namer;
			this.delegate = delegate;
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
			knownElements.add(identity.getName());
			return delegate.register(identity);
		}

		@Override
		public void configureEach(Action<? super ElementType> configureAction) {
			delegate.configureEach(new OnlyIfKnownAction<>(configureAction));
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

	// Responsible for bridging domain object registry and element lifecycle configuration, i.e. mutate, finalized, etc.
	//   It's also responsible to adapt the NamedDomainObjectProvider into ModelObject.
	private static final class GradleCollectionAdapter<ElementType> implements ModelElementContainer<ElementType> {
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
		public void configureEach(Action<? super ElementType> configureAction) {
			collection.configureEach(configureAction);
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

	// Layer 0: Responsible for registration and configuration of ModelObject.
	interface ModelElementContainer<ElementType> {
		<RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity);

		void configureEach(Action<? super ElementType> configureAction);

		void whenElementFinalized(Action<? super ElementType> finalizeAction);
	}

	interface ModelElementConfigurer<ElementType> {
		<T extends ElementType> void configure(ModelObjectIdentity<T> identity, Action<? super T> configureAction);
	}

	interface ModelElementProvider<ElementType> {
		<T extends ElementType> Provider<T> forIdentity(ModelObjectIdentity<T> identity);
	}

	private static final class GradleCollectionModelElementConfigurerAdapter<ElementType> implements ModelElementConfigurer<ElementType> {
		private final NamedDomainObjectCollection<ElementType> collection;

		private GradleCollectionModelElementConfigurerAdapter(NamedDomainObjectCollection<ElementType> collection) {
			this.collection = collection;
		}

		@Override
		public <T extends ElementType> void configure(ModelObjectIdentity<T> identity, Action<? super T> configureAction) {
			final String name = identity.getName();
			final Class<T> type = identity.getType().getConcreteType();

			if (collection.getNames().contains(name)) {
				collection.named(name, type).configure(configureAction);
			} else {
				collection.configureEach(it -> {
					if (collection.getNamer().determineName(it).equals(name)) {
						configureAction.execute(type.cast(it));
					}
				});
			}
		}
	}

	private static final class GradleCollectionModelElementProviderAdapter<ElementType> implements ModelElementProvider<ElementType> {
		private final NamedDomainObjectCollection<ElementType> collection;
		private final ProviderFactory providers;

		private GradleCollectionModelElementProviderAdapter(NamedDomainObjectCollection<ElementType> collection, ProviderFactory providers) {
			this.collection = collection;
			this.providers = providers;
		}

		@Override
		public <T extends ElementType> Provider<T> forIdentity(ModelObjectIdentity<T> identity) {
			return providers.provider(() -> {
				final String name = identity.getName();
				final Class<T> type = identity.getType().getConcreteType();
				return collection.named(name, type);
			}).flatMap(noOpTransformer());
		}
	}

	// Responsible for managing all APIs relying on the "known" objects, i.e. getElements, whenElementKnown, etc.
	private static final class KnownElementModelMapStrategy<ElementType> implements ModelMapStrategy<ElementType> {
		private final Map<ModelObjectIdentifier, ModelObject<?>> knownObjects = new HashMap<>();
		private final SetProviderFactory setProviders;
		private final ModelElementContainer<ElementType> delegate;
		private final DomainObjectSet<KnownModelObject<? extends ElementType>> knownElements;
		private final KnownModelObjectFactory factory;

		@SuppressWarnings({"unchecked", "UnstableApiUsage"})
		private KnownElementModelMapStrategy(Class<ElementType> elementType, SetProviderFactory setProviders, ObjectFactory objects, ModelElementFinalizer finalizer, ModelElementConfigurer<ElementType> configurer, ModelElementProvider<ElementType> provider, ModelElementContainer<ElementType> delegate) {
			this.setProviders = setProviders;
			this.delegate = delegate;
			this.knownElements = (DomainObjectSet<KnownModelObject<? extends ElementType>>) objects.domainObjectSet(new TypeToken<KnownModelObject<ElementType>>() {}.where(new TypeParameter<ElementType>() {}, elementType).getRawType());
			this.factory = new KnownModelObjectFactory(finalizer, configurer, provider);
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
			knownElements.add(factory.create(identity));
			final ModelObject<RegistrableType> result = delegate.register(identity);
			knownObjects.put(identity.getIdentifier(), result);
			return result;
		}

		@Override
		public void configureEach(Action<? super ElementType> configureAction) {
			delegate.configureEach(configureAction);
		}

		@Override
		public void whenElementKnown(Action<? super KnownModelObject<? extends ElementType>> configureAction) {
			knownElements.all(configureAction);
		}

		@Override
		public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			delegate.whenElementFinalized(finalizeAction);
		}

		@Override
		@SuppressWarnings("unchecked")
		public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
			return (ModelObject<ElementType>) knownObjects.computeIfAbsent(identifier, __ -> { throw new RuntimeException("not known"); });
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U> Provider<Set<U>> getElements(Class<U> type, Spec<? super ModelObjectIdentity<?>> spec) {
			return setProviders.create(builder -> {
				knownElements.forEach(it -> {
					if (it.getType().isSubtypeOf(type) && spec.isSatisfiedBy(ofIdentity(it.getIdentifier(), it.getType()))) {
						builder.add((Provider<? extends U>) it.asProvider());
					}
				});
			});
		}

		private final class KnownModelObjectFactory {
			private final ModelElementFinalizer finalizer;
			private final ModelElementConfigurer<ElementType> configurer;
			private final ModelElementProvider<ElementType> provider;

			private KnownModelObjectFactory(ModelElementFinalizer finalizer, ModelElementConfigurer<ElementType> configurer, ModelElementProvider<ElementType> provider) {
				this.finalizer = finalizer;
				this.configurer = configurer;
				this.provider = provider;
			}

			public <ObjectType extends ElementType> KnownModelObject<ObjectType> create(ModelObjectIdentity<ObjectType> identity) {
				return new KnownModelObject<ObjectType>() {
					@Override
					public ModelObjectIdentifier getIdentifier() {
						return identity.getIdentifier();
					}

					@Override
					public ModelType<?> getType() {
						return identity.getType();
					}

					@Override
					public KnownModelObject<ObjectType> configure(Action<? super ObjectType> configureAction) {
						configurer.configure(identity, configureAction);
						return this;
					}

					@Override
					public KnownModelObject<ObjectType> whenFinalized(Action<? super ObjectType> finalizeAction) {
						finalizer.accept(() -> configure(finalizeAction));
						return this;
					}

					@Override
					public void realizeNow() {
						asProvider().get();
					}

					@Override
					public Provider<ObjectType> asProvider() {
						return provider.forIdentity(identity);
					}

					@Override
					public String getName() {
						return identity.getName();
					}
				};
			}
		}
	}

	public interface ModelElementParents {
		Stream<ModelElement> parentOf(ModelObjectIdentifier identifier);
	}

	// Responsible to decorate all domain objects with their respective ModelElement.
	//   It doubles as a lookup for ModelElement required by "extensible" ModelMap's domain object factories.
	private static final class ModelElementDecorator<ElementType> implements ModelElementContainer<ElementType>, ModelElementLookup {
		private final Multimap<String, ModelObjectIdentity<?>> nameToIdentities = ArrayListMultimap.create();
		private final Map<String, ModelElement> nameToElements = new HashMap<>();
		private final Map<ModelObjectIdentity<?>, Provider<?>> identityToProviders = new HashMap<>();
		private final ModelElementContainer<ElementType> delegate;
		private final ModelElementParents elementParents;
		private final ProviderFactory providers;

		public ModelElementDecorator(Namer<ElementType> namer, ModelElementParents elementParents, ProviderFactory providers, ModelElementContainer<ElementType> delegate) {
			this.elementParents = elementParents;
			this.providers = providers;
			this.delegate = delegate;

			delegate.configureEach(new InjectModelElementAction<>(namer));
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
			nameToIdentities.put(identity.getName(), identity);
			nameToElements.computeIfAbsent(identity.getName(), this::create);
			final ModelObject<RegistrableType> result = delegate.register(identity);
			identityToProviders.put(identity, result.asProvider());
			return result;
		}

		@Override
		public void configureEach(Action<? super ElementType> configureAction) {
			delegate.configureEach(configureAction);
		}

		@Override
		public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			delegate.whenElementFinalized(finalizeAction);
		}

		@Nullable
		@Override
		public ModelElement find(String name) {
			return nameToElements.get(name); // can be null
		}

		private final class InjectModelElementAction<S extends ElementType> implements Action<S> {
			private final Namer<ElementType> namer;

			private InjectModelElementAction(Namer<ElementType> namer) {
				this.namer = namer;
			}

			@Override
			public void execute(S it) {
				@Nullable final ModelElement element = nameToElements.get(namer.determineName(it));
				if (element != null) {
					ModelElementSupport.injectIdentifier(it, element);
				}
			}
		}

		private ModelElement create(String name) {
			return new ModelElement() {
				private final Collection<ModelObjectIdentity<?>> identities = nameToIdentities.get(name);

				@Override
				public ModelObjectIdentifier getIdentifier() {
					assert identities.size() >= 1; // TODO: Need to figure out
					return identities.iterator().next().getIdentifier();
				}

				@Override
				public Stream<ModelElement> getParents() {
					return elementParents.parentOf(getIdentifier());
				}

				@Override
				public boolean instanceOf(Class<?> type) {
					return nameToIdentities.get(name).stream().anyMatch(it -> it.getType().isSubtypeOf(type));
				}

				@Override
				@SuppressWarnings("unchecked")
				public <T> Provider<T> safeAs(Class<T> type) {
					return providers.provider(() -> identities.stream().filter(it -> it.getType().isSubtypeOf(type)).findFirst().map(it -> (Provider<T>) identityToProviders.get(it)).orElse(null)).flatMap(it -> it);
				}

				@Override
				public String getName() {
					return name;
				}
			};
		}
	}

	private static final class BaseModelMap<ElementType> implements ModelMap<ElementType>, ModelObjectRegistry<ElementType>, ModelElementLookup {
		private final ModelMapStrategy<ElementType> strategy;
		private final ContextualModelObjectIdentifier identifierFactory;
		private final RegistrableTypes registrableTypes;
		private final ModelElementLookup elementsLookup;

		private BaseModelMap(Class<ElementType> elementType, PolymorphicDomainObjectRegistry<ElementType> registry, DiscoveredElements discoveredElements, ModelElementFinalizer finalizer, NamedDomainObjectSet<ElementType> delegate, ContextualModelObjectIdentifier identifierFactory, ProviderFactory providers, ObjectFactory objects, ModelElementParents elementParents, SetProviderFactory setProviders) {
			// This may seems like a very complicated "new soup" but there is a very good reason for this design.
			//   Each class is responsible for one aspect of the whole ModelMap behaviour (separation of concerns).
			//   There is three level of API:
			//     1) Full ModelMap
			//     2) Restricted ModelMap API (no overloading)
			//     3) Elements handling of the ModelMap API
			//   The various layer handle one behaviour.
			final ModelElementDecorator<ElementType> elementsLookup = new ModelElementDecorator<>(delegate.getNamer(), elementParents, providers, new RegisteredOnlyModelElementContainer<>(delegate.getNamer(), new GradleCollectionAdapter<>(registry, new GradleCollectionElements<>(delegate), finalizer)));
			this.strategy = new DiscoverableModelMapStrategy<>(discoveredElements, providers, new KnownElementModelMapStrategy<>(elementType, setProviders, objects, finalizer, new GradleCollectionModelElementConfigurerAdapter<>(delegate), new GradleCollectionModelElementProviderAdapter<>(delegate, providers), elementsLookup));
			this.elementsLookup = elementsLookup;
			this.identifierFactory = identifierFactory;
			this.registrableTypes = registry.getRegistrableTypes()::canRegisterType;
		}

		@Override
		public ModelElement find(String name) {
			return elementsLookup.find(name);
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
		@SuppressWarnings("unchecked")
		public void whenElementKnown(Action<? super KnownModelObject<ElementType>> configureAction) {
			// FIXME: Should we declare the method as KnownModelObject<? extends ElementType>???
			strategy.whenElementKnown(it -> configureAction.execute((KnownModelObject<ElementType>) it));
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
