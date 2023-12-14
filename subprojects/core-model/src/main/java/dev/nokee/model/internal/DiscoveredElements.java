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

import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import dev.nokee.internal.Factory;
import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.internal.reflect.MethodFieldVisitor;
import dev.nokee.internal.reflect.NotPrivateOrStaticMethodsVisitor;
import dev.nokee.internal.reflect.SuperClassFirstClassVisitor;
import dev.nokee.model.internal.decorators.Decorator;
import dev.nokee.model.internal.decorators.DeriveNameFromPropertyNameNamer;
import dev.nokee.model.internal.decorators.NestedObjectNamer;
import dev.nokee.model.internal.decorators.ObjectTypeVisitor;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.ModelTypeUtils;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Rule;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DiscoveredElements {
	private final Instantiator instantiator;
	private final DomainObjectSet<Element> elements;
	private final Map<Key, ModelObject<?>> objects = new HashMap<>();

	public DiscoveredElements(Instantiator instantiator, ObjectFactory objects) {
		this.instantiator = instantiator;
		this.elements = objects.domainObjectSet(Element.class);
	}

	public Rule ruleFor(Class<Task> baseType) {
		return new Rule() {
			@Override
			public String getDescription() {
				return "discover elements";
			}

			@Override
			public void apply(String domainObjectName) {
				if (!discoverDirectMatch(domainObjectName, baseType)) {
					discoverPrefix(domainObjectName, baseType);
					// TODO: Maybe discover task-name inference
				}
			}
		};
	}

	@EqualsAndHashCode
	private static final class Key {
		private final ModelObjectIdentifier identifier;
		private final ModelType<?> type;

		private Key(ModelObjectIdentifier identifier, ModelType<?> type) {
			this.identifier = identifier;
			this.type = type;
		}
	}

	public <RegistrableType> ModelObject<RegistrableType> discover(ModelObjectIdentifier identifier, Class<RegistrableType> type, Factory<ModelObject<RegistrableType>> factory) {
		elements.addAll(discover(identifier, type));
		final ModelObject<RegistrableType> result = factory.create();
		objects.put(new Key(identifier, ModelType.of(type)), result);
		return result;
	}

	private List<Element> discover(ModelObjectIdentifier identifier, Class<?> type) {
		List<Element> result = new ArrayList<>();
		Element parent = new Element(identifier, ModelType.of(type), null);
		result.add(parent);
		for (DiscoverableElement it : discoverEx(identifier, type)) {
			Element e = it.resolve(identifier, parent);
			result.add(e);
			result.addAll(discover(e));
		}
		return result;
	}

	private List<Element> discover(Element ee) {
		List<Element> result = new ArrayList<>();
		for (DiscoverableElement it : discoverEx(ee.identifier, ee.type.getConcreteType())) {
			Element e = it.resolve(ee.identifier, ee);
			result.add(e);
			result.addAll(discover(e));
		}
		return result;
	}

	private List<DiscoverableElement> discoverEx(ModelObjectIdentifier identifier, Class<?> type) {
		final List<DiscoverableElement> result = new ArrayList<>();
		new SuperClassFirstClassVisitor(new MethodFieldVisitor(new NotPrivateOrStaticMethodsVisitor(new MethodFieldVisitor.ClassMethodFieldVisitor() {
			@Override
			public void visitClass(Class<?> type) {

			}

			@Override
			public void visitConstructor(Constructor<?> constructor) {

			}

			@Override
			public void visitMethod(Method method) {
				annotationsOn(method).filter(Discoverable.class::isInstance).forEach(annotation -> {
					if (((Discoverable) annotation).value().equals(ModelElementDiscovery.None.class)) {
						ModelType<?> returnType = returnTypeOf(method);
						ElementName elementName = elementNameOf(method);

						ModelObjectIdentifierResolver childIdentifier = identifier -> identifier;
						if (elementName != null) {
							childIdentifier = identifier -> identifier.child(elementName);
						}

						result.add(new DiscoverableElement(childIdentifier, unpackDomainObjectType(returnType.getType())));
					} else {
						result.addAll(instantiator.newInstance(((Discoverable) annotation).value()).discover(identifier));
					}
				});
			}

			@Override
			public void visitEnd() {

			}

			@Nullable
			private /*static*/ ElementName elementNameOf(Method method) {
				return new NestedObjectNamer(new DeriveNameFromPropertyNameNamer()).determineName(new Decorator.MethodMetadata() {
					@Override
					public String getName() {
						return method.getName();
					}

					@Override
					public Class<?> getReturnType() {
						return returnTypeOf(method).getRawType();
					}

					@Override
					public Type getGenericReturnType() {
						return returnTypeOf(method).getType();
					}

					@Override
					public Stream<Annotation> getAnnotations() {
						return Arrays.stream(method.getAnnotations());
					}
				});
			}

			private /*static*/ ModelType<?> unpackDomainObjectType(Type objectType) {
				return new ObjectTypeVisitor<>(new ObjectTypeVisitor.Visitor<ModelType<?>>() {
					@Override
					public ModelType<?> visitElementType(Class<?> elementType) {
						return ModelType.of(elementType);
					}

					@Override
					public ModelType<?> visitElementTypeOrObjectType(Class<?> elementType) {
						// TODO: element here goes straight to realized, is this important?
						return ModelType.of(elementType);
					}
				}).visit(objectType);
			}

			private ModelType<?> returnTypeOf(Method method) {
				try {
					Method m = method.getDeclaringClass().getMethod(method.getName());
					return ModelType.of(TypeToken.of(ModelTypeUtils.toUndecoratedType(type)).resolveType(m.getGenericReturnType()).getType());
				} catch (
					NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}

			private Stream<Annotation> annotationsOn(Method method) {
				Set<Annotation> seen = new LinkedHashSet<>();
				Deque<Annotation> queue = new ArrayDeque<>();

				queue.addAll(Arrays.asList(method.getAnnotations()));
				while (!queue.isEmpty()) {
					Annotation current = queue.removeFirst();
					if (!seen.add(current)) {
						continue;
					}
					queue.addAll(Arrays.asList(current.annotationType().getAnnotations()));
				}
				return seen.stream();
			}
		}))).visitClass(type);
		return result;
	}

	public <T> void onRealized(Action<? super T> action, Consumer<? super Action<? super T>> next) {
		next.accept(action);
	}

	public <T> void onKnown(Action<? super KnownModelObject<T>> action, Consumer<? super Action<? super KnownModelObject<T>>> next) {
		next.accept(action);
	}

	public <T> void onFinalized(Action<? super T> action, Consumer<? super Action<? super T>> next) {
		next.accept(action);
	}

	public void discoverAll(Class<?> baseType) {
		for (int i = 0; i < elements.size(); ++i) {
			Element candidate = Iterables.get(elements, i);
			if (candidate.type.isSubtypeOf(baseType)) {
				realize(candidate);
			}
		}
	}

	private boolean discoverDirectMatch(String name, Class<?> baseType) {
		for (int i = 0; i < elements.size(); ++i) {
			Element candidate = Iterables.get(elements, i);
			if (candidate.type.isSubtypeOf(baseType) && ModelObjectIdentifiers.asFullyQualifiedName(candidate.identifier).toString().equals(name)) {
				realize(candidate);
				return true;
			}
		}
		return false;
	}

	private boolean discoverPrefix(String name, Class<?> baseType) {
		for (int i = 0; i < elements.size(); ++i) {
			Element candidate = Iterables.get(elements, i);
			if (candidate.type.isSubtypeOf(baseType) && ModelObjectIdentifiers.asFullyQualifiedName(candidate.identifier).toString().startsWith(name)) {
				realize(candidate);
				return true;
			}
		}
		return false;
	}

	private void realize(Element e) {
		if (e == null) {
			return;
		}

		realize(e.parent);
		objects.get(new Key(e.identifier, e.type)).get(); // realize
	}

	public static final class DiscoverableElement {
		private final ModelObjectIdentifierResolver identifierResolver;
		private final ModelType<?> type;

		public DiscoverableElement(ModelObjectIdentifierResolver identifierResolver, ModelType<?> type) {
			this.identifierResolver = identifierResolver;
			this.type = type;
		}

		public Element resolve(ModelObjectIdentifier baseIdentifier, Element parent) {
			return new Element(identifierResolver.resolve(baseIdentifier), type, parent);
		}
	}

	@EqualsAndHashCode
	private static final class Element {
		private final ModelObjectIdentifier identifier;
		private final ModelType<?> type;
		@Nullable private final Element parent;

		public Element(ModelObjectIdentifier identifier, ModelType<?> type, @Nullable Element parent) {
			this.identifier = identifier;
			this.type = type;
			this.parent = parent;
		}

		@Override
		public String toString() {
			return "Element{" +
				"identifier=" + ModelObjectIdentifiers.asFullyQualifiedName(identifier) +
				", type=" + type +
				", parent=" + parent +
				'}';
		}
	}

	public interface ModelObjectIdentifierResolver {
		ModelObjectIdentifier resolve(ModelObjectIdentifier baseIdentifier);
	}
}
