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
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.nokee.model.internal.TypeFilteringAction.ofType;

public final class ModelMapAdapters {
	private ModelMapAdapters() {}

	private interface ModelMapMixIn<ElementType> extends ModelMap<ElementType> {
		@Override
		default <U> void configureEach(Class<U> type, Action<? super U> configureAction) {
			configureEach(ofType(type, configureAction));
		}

		@Override
		default <U> void whenElementKnown(Class<U> type, Action<? super KnownModelObject<U>> configureAction) {
			whenElementKnown(ofType(new KnownModelObjectTypeOf<>(type), configureAction));
		}

		@Override
		default <U> void whenElementFinalized(Class<U> type, Action<? super U> finalizeAction) {
			whenElementFinalized(ofType(type, finalizeAction));
		}
	}

	// We need this implementation to access a NamedDomainObjectProvider<Project>
	public static /*final*/ class ForProject implements ModelMapMixIn<Project> {
		private final Project project;
		private final KnownElements knownElements;
		private final DiscoveredElements discoveredElements;
		private final ModelObject<Project> object;
		private final Consumer<Runnable> onFinalize;

		@Inject
		public ForProject(NamedDomainObjectSet<Project> delegate, Project project, KnownElements knownElements, DiscoveredElements discoveredElements) {
			this.project = project;
			this.knownElements = knownElements;
			this.discoveredElements = discoveredElements;
			this.object = knownElements.register(ProjectIdentifier.of(project), Project.class, new PolymorphicDomainObjectRegistry<Project>() {
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
		public void whenElementKnown(Action<? super KnownModelObject<Project>> configureAction) {
			discoveredElements.onKnown(configureAction, a -> a.execute(new MyKnownModelObject<>((ModelElementIdentity) (ModelObject<?>) object)));
		}

		@Override
		public void whenElementFinalized(Action<? super Project> finalizeAction) {
			discoveredElements.onFinalized(finalizeAction, a -> {
				onFinalize.accept(() -> configureEach(a));
				knownElements.forEach(it -> it.addFinalizer(a));
			});
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

	public static /*final*/ class ForConfigurationContainer implements ForNamedDomainObjectContainer<Configuration>, HasPublicType, ModelMapMixIn<Configuration> {
		private final PolymorphicDomainObjectRegistry<Configuration> registry;
		private final KnownElements knownElements;
		private final ConfigurationContainer delegate;
		private final DiscoveredElements discoveredElements;
		private final Consumer<Runnable> onFinalize;
		private final ContextualModelObjectIdentifier identifierFactory;

		@Inject
		public ForConfigurationContainer(ConfigurationContainer delegate, KnownElements knownElements, DiscoveredElements discoveredElements, Project project) {
			this.registry = new ConfigurationRegistry(delegate);
			this.knownElements = knownElements;
			this.delegate = delegate;
			this.discoveredElements = discoveredElements;
			this.onFinalize = it -> project.afterEvaluate(__ -> it.run());
			this.identifierFactory = new ContextualModelObjectIdentifier(ProjectIdentifier.of(project));
		}

		@Override
		public <RegistrableType extends Configuration> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return discoveredElements.discover(identifier, type, () -> knownElements.register(identifier, type, registry));
		}

		@Override
		public <RegistrableType extends Configuration> ModelObject<RegistrableType> register(ElementName name, Class<RegistrableType> type) {
			return identifierFactory.create(name, identifier -> register(identifier, type));
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return registry.getRegistrableTypes()::canRegisterType;
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
		public void whenElementKnown(Action<? super KnownModelObject<Configuration>> configureAction) {
			discoveredElements.onKnown(configureAction, a -> knownElements.forEach(it -> a.execute(new MyKnownModelObject<>(it))));
		}

		@Override
		public void whenElementFinalized(Action<? super Configuration> finalizeAction) {
			discoveredElements.onFinalized(new ExecuteOncePerElementAction<>(finalizeAction), a -> {
				onFinalize.accept(() -> configureEach(a));
				knownElements.forEach(it -> it.addFinalizer(a));
			});
		}

		@Override
		public ModelObject<Configuration> getById(ModelObjectIdentifier identifier) {
			return knownElements.getById(identifier, Configuration.class);
		}
	}

	public interface ForPolymorphicDomainObjectContainer<ElementType> extends ModelObjectRegistry<ElementType>, ModelMap<ElementType> {}

	public static /*final*/ class ForTaskContainer implements ForPolymorphicDomainObjectContainer<Task>, HasPublicType, ModelMapMixIn<Task> {
		private final PolymorphicDomainObjectRegistry<Task> registry;
		private final KnownElements knownElements;
		private final PolymorphicDomainObjectContainer<Task> delegate;
		private final Consumer<Runnable> onFinalize;
		private final ContextualModelObjectIdentifier identifierFactory;
		private final DiscoveredElements discoveredElements;

		@Inject
		public ForTaskContainer(TaskContainer delegate, KnownElements knownElements, DiscoveredElements discoveredElements, Project project) {
			this.registry = new TaskRegistry(delegate);
			this.discoveredElements = discoveredElements;
			this.knownElements = knownElements;
			this.delegate = delegate;
			this.onFinalize = it -> project.afterEvaluate(__ -> it.run());
			this.identifierFactory = new ContextualModelObjectIdentifier(ProjectIdentifier.of(project));
		}

		@Override
		public <RegistrableType extends Task> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return discoveredElements.discover(identifier, type, () -> knownElements.register(identifier, type, registry));
		}

		@Override
		public <RegistrableType extends Task> ModelObject<RegistrableType> register(ElementName name, Class<RegistrableType> type) {
			return identifierFactory.create(name, identifier -> register(identifier, type));
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return registry.getRegistrableTypes()::canRegisterType;
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
		public void whenElementKnown(Action<? super KnownModelObject<Task>> configureAction) {
			discoveredElements.onKnown(configureAction, a -> knownElements.forEach(it -> a.execute(new MyKnownModelObject<>(it))));
		}

		@Override
		public void whenElementFinalized(Action<? super Task> finalizeAction) {
			discoveredElements.onFinalized(new ExecuteOncePerElementAction<>(finalizeAction), a -> {
				onFinalize.accept(() -> configureEach(a));
				knownElements.forEach(it -> it.addFinalizer(a));
			});
		}

		@Override
		public ModelObject<Task> getById(ModelObjectIdentifier identifier) {
			return knownElements.getById(identifier, Task.class);
		}
	}

	public interface ContextualModelElementInstantiator {
		<S> Function<KnownElements.KnownElement, S> newInstance(Factory<S> factory);
	}

	public static /*final*/ class ForExtensiblePolymorphicDomainObjectContainer<ElementType> implements ModelObjectRegistry<ElementType>, ModelObjectFactoryRegistry<ElementType>, ModelMapMixIn<ElementType>, HasPublicType {
		private final Class<ElementType> elementType;
		private final ExtensiblePolymorphicDomainObjectContainerRegistry<ElementType> registry;
		private final KnownElements knownElements;
		private final ExtensiblePolymorphicDomainObjectContainer<ElementType> delegate;
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
			this.registry = new ExtensiblePolymorphicDomainObjectContainerRegistry<>(delegate);
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return discoveredElements.discover(identifier, type, () -> knownElements.register(identifier, type, registry));
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ElementName name, Class<RegistrableType> type) {
			return identifierFactory.create(name, identifier -> register(identifier, type));
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return registry.getRegistrableTypes()::canRegisterType;
		}

		@Override
		public <U extends ElementType> void registerFactory(Class<U> type, NamedDomainObjectFactory<? extends U> factory) {
			registry.registerFactory(type, newFactory(type, factory));
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
		public void whenElementKnown(Action<? super KnownModelObject<ElementType>> configureAction) {
			knownElements.forEach(it -> configureAction.execute(new MyKnownModelObject<>(it)));
		}

		@Override
		public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			discoveredElements.onFinalized(new ExecuteOncePerElementAction<>(finalizeAction), a -> {
				onFinalize.accept(() -> configureEach(a));
				knownElements.forEach(it -> it.addFinalizer(finalizeAction));
			});
		}

		@Override
		public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
			return knownElements.getById(identifier, elementType);
		}
	}

	public static final class ModelElementIdentity implements ModelObject<Object> {
		private final List<Action<?>> finalizeActions = new ArrayList<>();
		private final ModelObjectIdentifier identifier;
		private final Class<?> implementationType;
		private NamedDomainObjectProvider<?> elementProvider;
		private boolean forcedRealize = false;
		private final ProviderFactory providers;
		private final ElementProvider elementProviderEx;
		private final DiscoveredElements discoveredElements;
		private final Consumer<Runnable> onFinalize;

		// TODO: Reduce visibility
		ModelElementIdentity(ModelObjectIdentifier identifier, Class<?> implementationType, ProviderFactory providers, ElementProvider elementProvider, DiscoveredElements discoveredElements, Project project) {
			this.identifier = identifier;
			this.implementationType = implementationType;
			this.providers = providers;
			this.elementProviderEx = elementProvider;
			this.discoveredElements = discoveredElements;
			this.onFinalize = it -> project.afterEvaluate(__ -> it.run());
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
			// TODO: notify the action execute for a specific Key
			discoveredElements.onRealized(configureAction, a -> elementProviderEx.configure(getName(), implementationType, a));
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
			return "object '" + ModelObjectIdentifiers.asFullyQualifiedName(identifier) + "' (" + implementationType.getSimpleName() + ")";
		}

		public void addFinalizer(Action<?> finalizeAction) {
			finalizeActions.add(finalizeAction);
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
			private final Project project;

			public Factory(ProviderFactory providers, ElementProvider elementProvider, DiscoveredElements discoveredElements, Project project) {
				this.providers = providers;
				this.elementProvider = elementProvider;
				this.discoveredElements = discoveredElements;
				this.project = project;
			}

			public ModelElementIdentity create(ModelObjectIdentifier identifier, Class<?> implementationType) {
				return new ModelElementIdentity(identifier, implementationType, providers, elementProvider, discoveredElements, project);
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
		public KnownModelObject<ElementType> configure(Action<? super ElementType> configureAction) {
			((ModelObject<ElementType>) it.asModelObject(it.implementationType)).configure(configureAction);
			return this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public KnownModelObject<ElementType> whenFinalized(Action<? super ElementType> finalizeAction) {
			((ModelObject<ElementType>) it.asModelObject(it.implementationType)).whenFinalized(finalizeAction);
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
}
