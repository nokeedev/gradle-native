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
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.nokee.model.internal.SupportedTypes.instanceOf;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.utils.NamedDomainObjectCollectionUtils.registerIfAbsent;

public final class ModelMapAdapters {
	private ModelMapAdapters() {}

	// We need this implementation to access a NamedDomainObjectProvider<Project>
	public static /*final*/ class ForProject implements ModelMap<Project> {
		private final Project project;
		private final DiscoveredElements discoveredElements;
		private final ModelObject<Project> object;
		private final Consumer<Runnable> onFinalize;

		@Inject
		public ForProject(NamedDomainObjectSet<Project> delegate, Project project, KnownElements knownElements, DiscoveredElements discoveredElements) {
			this.project = project;
			this.discoveredElements = discoveredElements;
			this.object = knownElements.register(ProjectIdentifier.of(project), Project.class, name -> {
				delegate.add(project);
				return delegate.named(project.getName());
			});
			this.onFinalize = it -> project.afterEvaluate(__ -> it.run());
		}

		@Override
		public <RegistrableType extends Project> ModelObject<RegistrableType> register(ElementName name, Class<RegistrableType> type) {
			throw new UnsupportedOperationException("registering Gradle Project is not supported");
		}

		@Override
		public void configureEach(Action<? super Project> configureAction) {
			object.configure(configureAction);
		}

		@Override
		public <U> void configureEach(Class<U> type, Action<? super U> configureAction) {
			object.configure(ofType(type, configureAction));
		}

		@Override
		public void whenElementKnown(Action<? super KnownModelObject<Project>> configureAction) {
			discoveredElements.onKnown(configureAction, a -> a.execute(new MyKnownModelObject<>((ModelElementIdentity) (ModelObject<?>) object)));
		}

		@Override
		public <U> void whenElementKnown(Class<U> type, Action<? super KnownModelObject<U>> configureAction) {
			discoveredElements.onKnown(ofType(new KnownModelObjectTypeOf<>(type), configureAction), a -> a.execute(new MyKnownModelObject<>((ModelElementIdentity) (ModelObject<?>) object)));
		}

		@Override
		public void whenElementFinalized(Action<? super Project> finalizeAction) {
			discoveredElements.onFinalized(finalizeAction, a -> onFinalize.accept(() -> configureEach(a)));
		}

		@Override
		public <U> void whenElementFinalized(Class<U> type, Action<? super U> finalizeAction) {
			discoveredElements.onFinalized(ofType(type, finalizeAction), a -> onFinalize.accept(() -> configureEach(a)));
		}

		@Override
		public ModelObject<Project> getById(ModelObjectIdentifier identifier) {
			if (identifier.equals(ProjectIdentifier.of(project))) {
				return object;
			}
			throw new RuntimeException();
		}
	}

	public interface ForNamedDomainObjectContainer<ElementType> extends ModelObjectRegistry<ElementType>, ModelMap<ElementType> {}

	public static /*final*/ class ForConfigurationContainer implements ForNamedDomainObjectContainer<Configuration>, HasPublicType {
		private final KnownElements knownElements;
		private final ConfigurationContainer delegate;
		private final DiscoveredElements discoveredElements;
		private final Consumer<Runnable> onFinalize;
		private final ContextualModelObjectIdentifier identifierFactory;

		@Inject
		public ForConfigurationContainer(ConfigurationContainer delegate, KnownElements knownElements, DiscoveredElements discoveredElements, Project project) {
			this.knownElements = knownElements;
			this.delegate = delegate;
			this.discoveredElements = discoveredElements;
			this.onFinalize = it -> project.afterEvaluate(__ -> it.run());
			this.identifierFactory = new ContextualModelObjectIdentifier(ProjectIdentifier.of(project));
		}

		@Override
		@SuppressWarnings("unchecked")
		public <RegistrableType extends Configuration> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			if (type.equals(Configuration.class)) {
				return discoveredElements.discover(identifier, type, () -> knownElements.register(identifier, type, name -> {
					return (NamedDomainObjectProvider<RegistrableType>) registerIfAbsent(delegate, name);
				}));
			} else {
				throw new UnsupportedOperationException("registration type must be Configuration");
			}

			// TODO: Account for ConsumableConfiguration, ResolvableConfiguration, DependencyScopeConfiguration
		}

		@Override
		public <RegistrableType extends Configuration> ModelObject<RegistrableType> register(ElementName name, Class<RegistrableType> type) {
			return identifierFactory.create(name, identifier -> register(identifier, type));
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return new DefaultRegistrableType(Configuration.class);
		}

		@Override
		@SuppressWarnings("UnstableApiUsage")
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(new TypeToken<ForNamedDomainObjectContainer<Configuration>>() {}.getType());
		}

		@Override
		public void configureEach(Action<? super Configuration> configureAction) {
			discoveredElements.onRealized(configureAction, a -> delegate.configureEach(a));
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U> void configureEach(Class<U> type, Action<? super U> configureAction) {
			discoveredElements.onRealized(ofType(type, configureAction), a -> {
				if (Configuration.class.isAssignableFrom(type)) {
					delegate.withType((Class<? extends Configuration>) type).configureEach((Action<? super Configuration>) configureAction);
				} else {
					delegate.configureEach(a);
				}
			});
		}

		@Override
		public void whenElementKnown(Action<? super KnownModelObject<Configuration>> configureAction) {
			discoveredElements.onKnown(configureAction, a -> knownElements.forEach(it -> a.execute(new MyKnownModelObject<>(it))));
		}

		@Override
		public <U> void whenElementKnown(Class<U> type, Action<? super KnownModelObject<U>> configureAction) {
			discoveredElements.onKnown(ofType(new KnownModelObjectTypeOf<>(type), configureAction), a -> knownElements.forEach(it -> a.execute(new MyKnownModelObject<>(it))));
		}

		@Override
		public void whenElementFinalized(Action<? super Configuration> finalizeAction) {
			discoveredElements.onFinalized(finalizeAction, a -> onFinalize.accept(() -> configureEach(a)));
		}

		@Override
		public <U> void whenElementFinalized(Class<U> type, Action<? super U> finalizeAction) {
			discoveredElements.onFinalized(ofType(type, finalizeAction), a -> onFinalize.accept(() -> configureEach(a)));
		}

		@Override
		public ModelObject<Configuration> getById(ModelObjectIdentifier identifier) {
			return knownElements.getById(identifier, Configuration.class);
		}

		private static final class DefaultRegistrableType implements RegistrableTypes {
			private final SupportedType registrableType;

			private DefaultRegistrableType(Class<?> containerType) {
				this.registrableType = instanceOf(containerType);
			}

			@Override
			public boolean canRegisterType(Class<?> type) {
				Objects.requireNonNull(type);
				return registrableType.supports(type);
			}
		}
	}

	public interface ForPolymorphicDomainObjectContainer<ElementType> extends ModelObjectRegistry<ElementType>, ModelMap<ElementType> {}

	public static /*final*/ class ForTaskContainer implements ForPolymorphicDomainObjectContainer<Task>, HasPublicType {
		private final Class<Task> elementType;
		private final KnownElements knownElements;
		private final PolymorphicDomainObjectContainer<Task> delegate;
		private final Consumer<Runnable> onFinalize;
		private final ContextualModelObjectIdentifier identifierFactory;
		private final DiscoveredElements discoveredElements;

		@Inject
		public ForTaskContainer(PolymorphicDomainObjectContainer<Task> delegate, KnownElements knownElements, DiscoveredElements discoveredElements, Project project) {
			this.discoveredElements = discoveredElements;
			this.elementType = Task.class;
			this.knownElements = knownElements;
			this.delegate = delegate;
			this.onFinalize = it -> project.afterEvaluate(__ -> it.run());
			this.identifierFactory = new ContextualModelObjectIdentifier(ProjectIdentifier.of(project));
		}

		@Override
		public <RegistrableType extends Task> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return discoveredElements.discover(identifier, type, () -> knownElements.register(identifier, type, name -> registerIfAbsent(delegate, name, type)));
		}

		@Override
		public <RegistrableType extends Task> ModelObject<RegistrableType> register(ElementName name, Class<RegistrableType> type) {
			return identifierFactory.create(name, identifier -> register(identifier, type));
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return new TaskContainerRegistrableTypes();
		}

		@Override
		@SuppressWarnings("UnstableApiUsage")
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(new TypeToken<ForPolymorphicDomainObjectContainer<Task>>() {}.getType());
		}

		@Override
		public void configureEach(Action<? super Task> configureAction) {
			discoveredElements.onRealized(configureAction, a -> delegate.configureEach(a));
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U> void configureEach(Class<U> type, Action<? super U> configureAction) {
			discoveredElements.onRealized(ofType(type, configureAction), a -> {
				if (elementType.isAssignableFrom(type)) {
					delegate.withType((Class<? extends Task>) type).configureEach((Action<? super Task>) configureAction);
				} else {
					delegate.configureEach(a);
				}
			});
		}

		@Override
		public void whenElementKnown(Action<? super KnownModelObject<Task>> configureAction) {
			discoveredElements.onKnown(configureAction, a -> knownElements.forEach(it -> a.execute(new MyKnownModelObject<>(it))));
		}

		@Override
		public <U> void whenElementKnown(Class<U> type, Action<? super KnownModelObject<U>> configureAction) {
			discoveredElements.onKnown(ofType(new KnownModelObjectTypeOf<>(type), configureAction), a -> knownElements.forEach(it -> a.execute(new MyKnownModelObject<>(it))));
		}

		@Override
		public void whenElementFinalized(Action<? super Task> finalizeAction) {
			discoveredElements.onFinalized(finalizeAction, a -> onFinalize.accept(() -> configureEach(a)));
		}

		@Override
		public <U> void whenElementFinalized(Class<U> type, Action<? super U> finalizeAction) {
			discoveredElements.onFinalized(ofType(type, finalizeAction), a -> onFinalize.accept(() -> configureEach(a)));
		}

		@Override
		public ModelObject<Task> getById(ModelObjectIdentifier identifier) {
			return knownElements.getById(identifier, Task.class);
		}

		private static final class TaskContainerRegistrableTypes implements RegistrableTypes {
			private final SupportedType registrableTypes = SupportedTypes.anySubtypeOf(Task.class);

			@Override
			public boolean canRegisterType(Class<?> type) {
				Objects.requireNonNull(type);
				return registrableTypes.supports(type);
			}
		}
	}

	public interface ContextualModelElementInstantiator {
		<S> Function<KnownElements.KnownElement, S> newInstance(Factory<S> factory);
	}

	public static /*final*/ class ForExtensiblePolymorphicDomainObjectContainer<ElementType> implements ModelObjectRegistry<ElementType>, ModelObjectFactoryRegistry<ElementType>, ModelMap<ElementType>, HasPublicType {
		private final Class<ElementType> elementType;
		private final KnownElements knownElements;
		private final ExtensiblePolymorphicDomainObjectContainer<ElementType> delegate;
		private final Set<Class<? extends ElementType>> creatableTypes = new LinkedHashSet<>();
		private final ManagedFactoryProvider managedFactory;
		private final DiscoveredElements discoveredElements;
		private final ContextualModelElementInstantiator elementInstantiator;
		private final Consumer<Runnable> onFinalize;
		private final ContextualModelObjectIdentifier identifierFactory;

		@Inject
		public ForExtensiblePolymorphicDomainObjectContainer(Class<ElementType> elementType, ExtensiblePolymorphicDomainObjectContainer<ElementType> delegate, Instantiator instantiator, KnownElements knownElements, DiscoveredElements discoveredElements, ContextualModelElementInstantiator elementInstantiator, Project project) {
			this.elementType = elementType;
			this.knownElements = knownElements;
			this.delegate = delegate;
			this.managedFactory = new ManagedFactoryProvider(instantiator);
			this.discoveredElements = discoveredElements;
			this.elementInstantiator = elementInstantiator;
			this.onFinalize = it -> project.afterEvaluate(__ -> it.run());
			this.identifierFactory = new ContextualModelObjectIdentifier(ProjectIdentifier.of(project));
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return discoveredElements.discover(identifier, type, () -> knownElements.register(identifier, type, name -> registerIfAbsent(delegate, name, type)));
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ElementName name, Class<RegistrableType> type) {
			return identifierFactory.create(name, identifier -> register(identifier, type));
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return new DefaultRegistrableTypes();
		}

		@Override
		public <U extends ElementType> void registerFactory(Class<U> type, NamedDomainObjectFactory<? extends U> factory) {
			delegate.registerFactory(type, newFactory(type, factory));
			creatableTypes.add(type);
		}

		private <U extends ElementType> NamedDomainObjectFactory<U> newFactory(Class<U> type, NamedDomainObjectFactory<? extends U> delegate) {
			return name -> knownElements.create(name, type, elementInstantiator.newInstance((Factory<U>) () -> delegate.create(name)));
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

		@Override
		public void configureEach(Action<? super ElementType> configureAction) {
			delegate.configureEach(configureAction);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U> void configureEach(Class<U> type, Action<? super U> configureAction) {
			if (elementType.isAssignableFrom(type)) {
				delegate.withType((Class<? extends ElementType>) type).configureEach((Action<? super ElementType>) configureAction);
			} else {
				delegate.configureEach(ofType(type, configureAction));
			}
		}

		@Override
		public void whenElementKnown(Action<? super KnownModelObject<ElementType>> configureAction) {
			knownElements.forEach(it -> configureAction.execute(new MyKnownModelObject<>(it)));
		}

		@Override
		public <U> void whenElementKnown(Class<U> type, Action<? super KnownModelObject<U>> configureAction) {
			discoveredElements.onKnown(ofType(new KnownModelObjectTypeOf<>(type), configureAction), a -> knownElements.forEach(it -> a.execute(new MyKnownModelObject<>(it))));
		}

		@Override
		public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			discoveredElements.onFinalized(finalizeAction, a -> onFinalize.accept(() -> configureEach(a)));
		}

		@Override
		public <U> void whenElementFinalized(Class<U> type, Action<? super U> finalizeAction) {
			discoveredElements.onFinalized(ofType(type, finalizeAction), a -> onFinalize.accept(() -> configureEach(a)));
		}

		@Override
		public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
			return knownElements.getById(identifier, elementType);
		}

		private final class DefaultRegistrableTypes implements RegistrableTypes {
			@Override
			public boolean canRegisterType(Class<?> type) {
				Objects.requireNonNull(type);
				return getSupportedTypes().anyMatch(it -> it.supports(type));
			}

			private Stream<SupportedType> getSupportedTypes() {
				return getCreatableType().stream().map(SupportedTypes::instanceOf);
			}

			private Set<? extends Class<?>> getCreatableType() {
				return creatableTypes;
			}
		}
	}

	public static final class ModelElementIdentity implements ModelObject<Object> {
		private final ModelObjectIdentifier identifier;
		private final Class<?> implementationType;
		private NamedDomainObjectProvider<?> elementProvider;
		private boolean forcedRealize = false;
		private final ProviderFactory providers;
		private final ElementProvider elementProviderEx;
		private final DiscoveredElements discoveredElements;

		// TODO: Reduce visibility
		ModelElementIdentity(ModelObjectIdentifier identifier, Class<?> implementationType, ProviderFactory providers, ElementProvider elementProvider, DiscoveredElements discoveredElements) {
			this.identifier = identifier;
			this.implementationType = implementationType;
			this.providers = providers;
			this.elementProviderEx = elementProvider;
			this.discoveredElements = discoveredElements;
		}

		public String getName() {
			return ModelObjectIdentifiers.asFullyQualifiedName(identifier).toString();
		}

		public ModelObjectIdentifier getIdentifier() {
			return identifier;
		}

		public boolean instanceOf(Class<?> type) {
			return type.isAssignableFrom(implementationType);
		}

		public void attachProvider(NamedDomainObjectProvider<?> elementProvider) {
			this.elementProvider = elementProvider;
			if (forcedRealize) {
				elementProvider.get();
			}
		}

		@SuppressWarnings("unchecked")
		public <T> ModelObject<T> asModelObject(Class<T> type) {
			return (ModelObject<T>) this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public NamedDomainObjectProvider<Object> asProvider() {
			return (NamedDomainObjectProvider<Object>) elementProviderEx.named(getName(), implementationType);
		}

		@Override
		public Object get() {
			return asProvider().get();
		}

		public void realizeNow() {
			forcedRealize = true;
			if (elementProvider != null) {
				elementProvider.get();
			}
		}

		@Override
		public ModelObject<Object> configure(Action<? super Object> configureAction) {
			discoveredElements.onRealized(configureAction, a -> elementProviderEx.configure(getName(), implementationType, a));
			return this;
		}

		@Override
		public String toString() {
			return "object '" + ModelObjectIdentifiers.asFullyQualifiedName(identifier) + "' (" + implementationType.getSimpleName() + ")";
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

			public Factory(ProviderFactory providers, ElementProvider elementProvider, DiscoveredElements discoveredElements) {
				this.providers = providers;
				this.elementProvider = elementProvider;
				this.discoveredElements = discoveredElements;
			}

			public ModelElementIdentity create(ModelObjectIdentifier identifier, Class<?> implementationType) {
				return new ModelElementIdentity(identifier, implementationType, providers, elementProvider, discoveredElements);
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
			return ModelType.of(it.implementationType);
		}

		@Override
		@SuppressWarnings("unchecked")
		public ModelObject<ElementType> configure(Action<? super ElementType> configureAction) {
			return ((ModelObject<ElementType>) it.asModelObject(it.implementationType)).configure(configureAction);
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
}
