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
import dev.nokee.model.internal.names.ElementName;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Namer;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Function;

import static dev.nokee.utils.NamedDomainObjectCollectionUtils.registerIfAbsent;

public final class ModelMapAdapters {
	private ModelMapAdapters() {}

	private static final class KnownElement implements ModelElement {
		private final String name;
		private final DomainObjectSet<ModelElementIdentity> identifiers;

		public KnownElement(String name, ObjectFactory objects) {
			this.name = name;
			this.identifiers = objects.domainObjectSet(ModelElementIdentity.class);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public ModelObjectIdentifier getIdentifier() {
			assert identifiers.size() >= 1; // TODO: Need to figure out
			return identifiers.iterator().next().identifier;
		}
	}

	private static final class KnownElements {
		private final DomainObjectSet<ModelElementIdentity> knownElements;
		private final NamedDomainObjectSet<KnownElement> mapping;

		public KnownElements(ObjectFactory objects) {
			this.knownElements = objects.domainObjectSet(ModelElementIdentity.class);
			this.mapping = objects.namedDomainObjectSet(KnownElement.class);

			knownElements.all(identity -> {
				KnownElement element = mapping.findByName(identity.getName());
				if (element == null) {
					element = new KnownElement(identity.getName(), objects);
					mapping.add(element);
				}

				element.identifiers.add(identity);
			});
		}

		// TODO: Consider using NamedDomainObjectRegistry instead of Function
		public <S> ModelObject<S> register(ModelObjectIdentifier identifier, Class<S> type, Function<? super String, ? extends NamedDomainObjectProvider<S>> factory) {
			ModelElementIdentity identity = new ModelElementIdentity(identifier, type);
			knownElements.add(identity);
			identity.attachProvider(factory.apply(identity.getName()));
			return identity.asModelObject(type);
		}

		public <S> S create(String name, Class<S> type, NamedDomainObjectFactory<? extends S> factory) {
			KnownElement element = mapping.findByName(name);
			if (element == null) {
				knownElements.add(new ModelElementIdentity(new DefaultModelObjectIdentifier(ElementName.of(name)), type));
				element = Objects.requireNonNull(mapping.findByName(name));
			}

			return ModelElementSupport.newInstance(element, (Factory<S>) () -> factory.create(name));
		}

		public void forEach(Action<? super ModelElementIdentity> configureAction) {
			knownElements.configureEach(configureAction);
		}
	}

	public interface ForNamedDomainObjectContainer<ElementType> extends ModelObjectRegistry<ElementType>, ModelMap<ElementType> {}

	public static /*final*/ class ForConfigurationContainer implements ForNamedDomainObjectContainer<Configuration>, HasPublicType {
		private final KnownElements knownElements;
		private final ConfigurationContainer delegate;

		@Inject
		public ForConfigurationContainer(ConfigurationContainer delegate, ObjectFactory objects) {
			this.knownElements = new KnownElements(objects);
			this.delegate = delegate;
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
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(new TypeToken<ForNamedDomainObjectContainer<Configuration>>() {}.getType());
		}

		@Override
		public void configureEach(Action<? super Configuration> configureAction) {
			delegate.configureEach(configureAction);
		}

		@Override
		public void whenElementKnow(Action<? super ModelElementIdentity> configureAction) {
			knownElements.forEach(configureAction);
		}
	}

	public static /*final*/ class ForPolymorphicDomainObjectContainer<ElementType> implements ModelObjectRegistry<ElementType>, ModelMap<ElementType>, HasPublicType {
		private final Class<ElementType> elementType;
		private final KnownElements knownElements;
		private final PolymorphicDomainObjectContainer<ElementType> delegate;

		@Inject
		public ForPolymorphicDomainObjectContainer(Class<ElementType> elementType, Namer<ElementType> namer, PolymorphicDomainObjectContainer<ElementType> delegate, ObjectFactory objects) {
			this.elementType = elementType;
			this.knownElements = new KnownElements(objects);
			this.delegate = delegate;

			delegate.configureEach(it -> {
				KnownElement element = knownElements.mapping.findByName(namer.determineName(it));
				if (element != null) {
					ModelElementSupport.injectIdentifier(it, element);
				}
			});
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return knownElements.register(identifier, type, name -> registerIfAbsent(delegate, name, type));
		}

		@Override
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(new TypeToken<ForPolymorphicDomainObjectContainer<ElementType>>() {}.where(new TypeParameter<ElementType>() {}, elementType).getType());
		}

		@Override
		public void configureEach(Action<? super ElementType> configureAction) {
			delegate.configureEach(configureAction);
		}

		@Override
		public void whenElementKnow(Action<? super ModelElementIdentity> configureAction) {
			knownElements.forEach(configureAction);
		}
	}

	public static /*final*/ class ForExtensiblePolymorphicDomainObjectContainer<ElementType> implements ModelObjectRegistry<ElementType>, ModelObjectFactoryRegistry<ElementType>, ModelMap<ElementType>, HasPublicType {
		private final Class<ElementType> elementType;
		private final KnownElements knownElements;
		private final ExtensiblePolymorphicDomainObjectContainer<ElementType> delegate;

		@Inject
		public ForExtensiblePolymorphicDomainObjectContainer(Class<ElementType> elementType, ExtensiblePolymorphicDomainObjectContainer<ElementType> delegate, ObjectFactory objects) {
			this.elementType = elementType;
			this.knownElements = new KnownElements(objects);
			this.delegate = delegate;
		}

		@Override
		public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentifier identifier, Class<RegistrableType> type) {
			return knownElements.register(identifier, type, name -> registerIfAbsent(delegate, name, type));
		}

		@Override
		public <U extends ElementType> void registerFactory(Class<U> type, NamedDomainObjectFactory<? extends U> factory) {
			delegate.registerFactory(type, name -> knownElements.create(name, type, factory));
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
		public void whenElementKnow(Action<? super ModelElementIdentity> configureAction) {
			knownElements.forEach(configureAction);
		}
	}

	public static final class ModelElementIdentity implements ModelObject<Object> {
		private final ModelObjectIdentifier identifier;
		private final Class<?> implementationType;
		private NamedDomainObjectProvider<?> elementProvider;

		private ModelElementIdentity(ModelObjectIdentifier identifier, Class<?> implementationType) {
			this.identifier = identifier;
			this.implementationType = implementationType;
		}

		public String getName() {
			return ModelObjectIdentifiers.asFullyQualifiedName(identifier).toString();
		}

		public ModelObjectIdentifier getIdentifier() {
			return identifier;
		}

		public Class<?> getType() {
			return implementationType;
		}

		public void attachProvider(NamedDomainObjectProvider<?> elementProvider) {
			this.elementProvider = elementProvider;
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

		@Override
		public ModelObject<Object> configure(Action<? super Object> configureAction) {
			elementProvider.configure(configureAction);
			return this;
		}
	}
}
