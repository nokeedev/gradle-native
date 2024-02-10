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
import org.gradle.api.provider.SetProperty;
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
import java.util.function.Consumer;
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
		public ForProject(NamedDomainObjectSet<Project> delegate, Project project, DiscoveredElements discoveredElements, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize, ModelElementParents elementParents) {
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
			this.delegate = new BaseModelMap<>(Project.class, registry, discoveredElements, onFinalize, delegate, null, providers, objects, elementParents);
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
		public ForConfigurationContainer(ConfigurationContainer delegate, DiscoveredElements discoveredElements, Project project, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize, ModelElementParents elementParents) {
			this.delegate = new BaseModelMap<>(Configuration.class, new ConfigurationRegistry(delegate), discoveredElements, onFinalize, delegate, new ContextualModelObjectIdentifier(ProjectIdentifier.of(project)), providers, objects, elementParents);
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
		public ForTaskContainer(TaskContainer delegate, DiscoveredElements discoveredElements, Project project, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize, ModelElementParents elementParents) {
			this.delegate = new BaseModelMap<>(Task.class, new TaskRegistry(delegate), discoveredElements, onFinalize, delegate, new ContextualModelObjectIdentifier(ProjectIdentifier.of(project)), providers, objects, elementParents);
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
		public ForExtensiblePolymorphicDomainObjectContainer(Class<ElementType> elementType, ExtensiblePolymorphicDomainObjectContainer<ElementType> delegate, Instantiator instantiator, DiscoveredElements discoveredElements, Project project, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer onFinalize, ModelElementParents elementParents) {
			this.delegate = new BaseModelMap<>(elementType, new ExtensiblePolymorphicDomainObjectContainerRegistry<>(delegate), discoveredElements, onFinalize, delegate, new ContextualModelObjectIdentifier(ProjectIdentifier.of(project)), providers, objects, elementParents);
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
		<T extends ElementType> NamedDomainObjectProvider<T> named(String name, Class<T> type);
		Set<String> getNames();
		Namer<ElementType> getNamer();
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

		@Override
		public <T extends ElementType> NamedDomainObjectProvider<T> named(String name, Class<T> type) {
			return collection.named(name, type);
		}

		@Override
		public Set<String> getNames() {
			return collection.getNames();
		}

		@Override
		public Namer<ElementType> getNamer() {
			return collection.getNamer();
		}
	}

	private static final class FilterCollectionAdapter<ElementType> implements GradleCollection<ElementType> {
		private final Set<String> knownElements = new HashSet<>();
		private final Namer<ElementType> namer;
		private final Elements<ElementType> collection;
		private final GradleCollection<ElementType> delegate;

		private FilterCollectionAdapter(GradleCollection<ElementType> delegate) {
			this.namer = delegate.getElements().getNamer();
			this.delegate = delegate;
			this.collection = new Elements<ElementType>() {
				private final Elements<ElementType> elements = delegate.getElements();

				@Override
				public void configureEach(Action<? super ElementType> configureAction) {
					elements.configureEach(new OnlyIfKnownAction<>(configureAction));
				}

				@Override
				public <T extends ElementType> NamedDomainObjectProvider<T> named(String name, Class<T> type) {
					if (!knownElements.contains(name)) {
						throw new IllegalArgumentException("not known");
					}
					return elements.named(name, type);
				}

				@Override
				public Set<String> getNames() {
					return knownElements; // we can just return the known names
				}

				@Override
				public Namer<ElementType> getNamer() {
					return elements.getNamer();
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
		private final Map<ModelObjectIdentifier, ModelObject<?>> knownObjects = new HashMap<>();
		private final ModelMapElementsProviderFactory providers;
		private final GradleCollection<ElementType> delegate;
		private final DomainObjectSet<KnownModelObject<? extends ElementType>> knownElements;
		private final KnownModelObjectFactory factory;

		@SuppressWarnings({"unchecked", "UnstableApiUsage"})
		private DefaultModelMapStrategy(Class<ElementType> elementType, ProviderFactory providers, ObjectFactory objects, ModelElementFinalizer finalizer, GradleCollection<ElementType> delegate) {
			this.providers = new DefaultModelMapElementsProviderFactory(providers, objects);
			this.delegate = delegate;
			this.knownElements = (DomainObjectSet<KnownModelObject<? extends ElementType>>) objects.domainObjectSet(new TypeToken<KnownModelObject<ElementType>>() {}.where(new TypeParameter<ElementType>() {}, elementType).getRawType());
			this.factory = new KnownModelObjectFactory(providers, finalizer);
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
			delegate.getElements().configureEach(configureAction);
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
			return providers.provider(builder -> {
				knownElements.forEach(it -> {
					if (it.getType().isSubtypeOf(type) && spec.isSatisfiedBy(ofIdentity(it.getIdentifier(), it.getType()))) {
						builder.add((Provider<? extends U>) it.asProvider());
					}
				});
			});
		}

		private final class KnownModelObjectFactory {
			private final ProviderFactory providers;
			private final ModelElementFinalizer finalizer;

			private KnownModelObjectFactory(ProviderFactory providers, ModelElementFinalizer finalizer) {
				this.providers = providers;
				this.finalizer = finalizer;
			}

			public <ObjectType extends ElementType> KnownModelObject<ObjectType> create(ModelObjectIdentity<ObjectType> identity) {
				return new KnownModelObject<ObjectType>() {
					private final Elements<ElementType> collection = delegate.getElements();
					private final String name = identity.getName();
					private final Class<ObjectType> type = identity.getType().getConcreteType();

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
						if (collection.getNames().contains(name)) {
							collection.named(name, type).configure(configureAction);
						} else {
							collection.configureEach(it -> {
								if (collection.getNamer().determineName(it).equals(name)) {
									configureAction.execute(type.cast(it));
								}
							});
						}
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
						return providers.provider(() -> delegate.getElements().named(name, type)).flatMap(noOpTransformer());
					}

					@Override
					public String getName() {
						return name;
					}
				};
			}
		}
	}

	public interface ModelElementParents {
		Stream<ModelElement> parentOf(ModelObjectIdentifier identifier);
	}

	private static final class ModelElementDecorator<ElementType> implements GradleCollection<ElementType>, ModelElementLookup {
		private final Multimap<String, ModelObjectIdentity<?>> nameToIdentities = ArrayListMultimap.create();
		private final Map<String, ModelElement> nameToElements = new HashMap<>();
		private final Map<ModelObjectIdentity<?>, Provider<?>> identityToProviders = new HashMap<>();
		private final ModelMapAdapters.GradleCollection<ElementType> delegate;
		private final ModelElementParents elementParents;
		private final ProviderFactory providers;

		public ModelElementDecorator(ModelElementParents elementParents, ProviderFactory providers, GradleCollection<ElementType> delegate) {
			this.elementParents = elementParents;
			this.providers = providers;
			this.delegate = delegate;

			delegate.getElements().configureEach(new InjectModelElementAction<>());
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
		public Elements<ElementType> getElements() {
			return delegate.getElements();
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
			private final Namer<ElementType> namer = delegate.getElements().getNamer();

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

		private BaseModelMap(Class<ElementType> elementType, PolymorphicDomainObjectRegistry<ElementType> registry, DiscoveredElements discoveredElements, ModelElementFinalizer finalizer, NamedDomainObjectSet<ElementType> delegate, ContextualModelObjectIdentifier identifierFactory, ProviderFactory providers, ObjectFactory objects, ModelElementParents elementParents) {
			final ModelElementDecorator<ElementType> elementsLookup = new ModelElementDecorator<>(elementParents, providers, new FilterCollectionAdapter<>(new GradleCollectionAdapter<>(registry, new GradleCollectionElements<>(delegate), finalizer)));
			this.strategy = new DiscoverableModelMapStrategy<>(discoveredElements, providers, new DefaultModelMapStrategy<>(elementType, providers, objects, finalizer, elementsLookup));
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
