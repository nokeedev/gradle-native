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
import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Namer;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.nokee.model.internal.SupportedTypes.instanceOf;
import static dev.nokee.utils.NamedDomainObjectCollectionUtils.registerIfAbsent;

public final class ModelMapAdapters {
	private ModelMapAdapters() {}

	public interface ForNamedDomainObjectContainer<ElementType> extends ModelObjectRegistry<ElementType>, ModelMap<ElementType> {}

	public static /*final*/ class ForConfigurationContainer implements ForNamedDomainObjectContainer<Configuration>, HasPublicType {
		private final KnownElements knownElements;
		private final ConfigurationContainer delegate;
		private final Consumer<Runnable> onFinalize;

		@Inject
		public ForConfigurationContainer(ConfigurationContainer delegate, Project project, KnownElements knownElements) {
			this.knownElements = knownElements;
			this.delegate = delegate;
			this.onFinalize = it -> project.afterEvaluate(__ -> it.run());
		}

		@Override
		@SuppressWarnings("unchecked")
		public <RegistrableType extends Configuration> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			if (type.equals(Configuration.class)) {
				return knownElements.register(identifier, type, name -> {
					return (NamedDomainObjectProvider<RegistrableType>) registerIfAbsent(delegate, name);
				});
			} else {
				throw new UnsupportedOperationException("registration type must be Configuration");
			}

			// TODO: Account for ConsumableConfiguration, ResolvableConfiguration, DependencyScopeConfiguration
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return new DefaultRegistrableType(Configuration.class);
		}

		@Override
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(new TypeToken<ForNamedDomainObjectContainer<Configuration>>() {}.getType());
		}

		@Override
		public void configureEach(Action<? super Configuration> configureAction) {
			delegate.configureEach(configureAction);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U> void configureEach(Class<U> type, Action<? super U> configureAction) {
			if (Configuration.class.isAssignableFrom(type)) {
				delegate.withType((Class<? extends Configuration>) type).configureEach((Action<? super Configuration>) configureAction);
			} else {
				delegate.configureEach(it -> {
					if (type.isInstance(it)) {
						configureAction.execute(type.cast(it));
					}
				});
			}
		}

		@Override
		public void whenElementKnow(Action<? super ModelElementIdentity> configureAction) {
			knownElements.forEach(configureAction);
		}

		@Override
		public void whenElementFinalized(Action<? super Configuration> finalizeAction) {
			onFinalize.accept(() -> {
				configureEach(finalizeAction);
			});
		}

		@Override
		public <U> void whenElementFinalized(Class<U> type, Action<? super U> finalizeAction) {
			onFinalize.accept(() -> {
				configureEach(type, finalizeAction);
			});
		}

		@Override
		public ModelObject<Configuration> getById(ModelObjectIdentifier identifier) {
			return knownElements.getById(identifier).asModelObject(Configuration.class);
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

		@Inject
		public ForTaskContainer(PolymorphicDomainObjectContainer<Task> delegate, Project project, KnownElements knownElements) {
			this(Task.class, new Task.Namer(), delegate, project, knownElements);
		}

		private ForTaskContainer(Class<Task> elementType, Namer<Task> namer, PolymorphicDomainObjectContainer<Task> delegate, Project project, KnownElements knownElements) {
			this.elementType = elementType;
			this.knownElements = knownElements;
			this.delegate = delegate;
			this.onFinalize = it -> project.afterEvaluate(__ -> it.run());
		}

		@Override
		public <RegistrableType extends Task> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return knownElements.register(identifier, type, name -> registerIfAbsent(delegate, name, type));
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return new TaskContainerRegistrableTypes();
		}

		@Override
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(new TypeToken<ForPolymorphicDomainObjectContainer<Task>>() {}.getType());
		}

		@Override
		public void configureEach(Action<? super Task> configureAction) {
			delegate.configureEach(configureAction);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U> void configureEach(Class<U> type, Action<? super U> configureAction) {
			if (elementType.isAssignableFrom(type)) {
				delegate.withType((Class<? extends Task>) type).configureEach((Action<? super Task>) configureAction);
			} else {
				delegate.configureEach(it -> {
					if (type.isInstance(it)) {
						configureAction.execute(type.cast(it));
					}
				});
			}
		}

		@Override
		public void whenElementKnow(Action<? super ModelElementIdentity> configureAction) {
			knownElements.forEach(configureAction);
		}

		@Override
		public void whenElementFinalized(Action<? super Task> finalizeAction) {
			onFinalize.accept(() -> {
				configureEach(finalizeAction);
			});
		}

		@Override
		public <U> void whenElementFinalized(Class<U> type, Action<? super U> finalizeAction) {
			onFinalize.accept(() -> {
				configureEach(type, finalizeAction);
			});
		}

		@Override
		public ModelObject<Task> getById(ModelObjectIdentifier identifier) {
			return knownElements.getById(identifier).asModelObject(Task.class);
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

	public static /*final*/ class ForExtensiblePolymorphicDomainObjectContainer<ElementType> implements ModelObjectRegistry<ElementType>, ModelObjectFactoryRegistry<ElementType>, ModelMap<ElementType>, HasPublicType {
		private final Class<ElementType> elementType;
		private final KnownElements knownElements;
		private final ExtensiblePolymorphicDomainObjectContainer<ElementType> delegate;
		private final Set<Class<? extends ElementType>> creatableTypes = new LinkedHashSet<>();
		private final ManagedFactoryProvider managedFactory;
		private final Consumer<Runnable> onFinalize;

		@Inject
		public ForExtensiblePolymorphicDomainObjectContainer(Class<ElementType> elementType, ExtensiblePolymorphicDomainObjectContainer<ElementType> delegate, Instantiator instantiator, Project project, KnownElements knownElements) {
			this.elementType = elementType;
			this.knownElements = knownElements;
			this.delegate = delegate;
			this.managedFactory = new ManagedFactoryProvider(instantiator);
			this.onFinalize = it -> project.afterEvaluate(__ -> it.run());
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return knownElements.register(identifier, type, name -> registerIfAbsent(delegate, name, type));
		}

		@Override
		public RegistrableTypes getRegistrableTypes() {
			return new DefaultRegistrableTypes();
		}

		@Override
		public <U extends ElementType> void registerFactory(Class<U> type, NamedDomainObjectFactory<? extends U> factory) {
			delegate.registerFactory(type, name -> knownElements.create(name, type, element -> {
				return ModelElementSupport.newInstance(new ModelElement() {
					@Override
					public ModelObjectIdentifier getIdentifier() {
						return element.getIdentifier();
					}

					@Override
					public String getName() {
						return element.getName();
					}
				}, (Factory<U>) () -> factory.create(name));
			}));
			creatableTypes.add(type);
		}

		@Override
		public <U extends ElementType> void registerFactory(Class<U> type) {
			registerFactory(type, managedFactory.create(type));
		}

		@Override
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
				delegate.configureEach(it -> {
					if (type.isInstance(it)) {
						configureAction.execute(type.cast(it));
					}
				});
			}
		}

		@Override
		public void whenElementKnow(Action<? super ModelElementIdentity> configureAction) {
			knownElements.forEach(configureAction);
		}

		@Override
		public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
			onFinalize.accept(() -> {
				configureEach(finalizeAction);
			});
		}

		@Override
		public <U> void whenElementFinalized(Class<U> type, Action<? super U> finalizeAction) {
			onFinalize.accept(() -> {
				configureEach(type, finalizeAction);
			});
		}

		@Override
		public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
			return knownElements.getById(identifier).asModelObject(elementType);
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
		NamedDomainObjectProvider<?> elementProvider; // TODO: Reduce visibility
		private boolean forcedRealize = false;

		// TODO: Reduce visibility
		ModelElementIdentity(ModelObjectIdentifier identifier, Class<?> implementationType) {
			this.identifier = identifier;
			this.implementationType = implementationType;
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
			return (NamedDomainObjectProvider<Object>) elementProvider;
		}

		@Override
		public Object get() {
			return elementProvider.get();
		}

		public void realizeNow() {
			forcedRealize = true;
			if (elementProvider != null) {
				elementProvider.get();
			}
		}

		@Override
		public ModelObject<Object> configure(Action<? super Object> configureAction) {
			elementProvider.configure(configureAction);
			return this;
		}

		@Override
		public String toString() {
			return "object '" + ModelObjectIdentifiers.asFullyQualifiedName(identifier) + "' (" + implementationType.getSimpleName() + ")";
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
}
