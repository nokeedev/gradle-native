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

package dev.nokee.model.internal.discover;

import com.google.common.reflect.TypeToken;
import dev.nokee.internal.reflect.MethodFieldVisitor;
import dev.nokee.internal.reflect.NotPrivateOrStaticMethodsVisitor;
import dev.nokee.model.internal.Discoverable;
import dev.nokee.model.internal.DiscoveredElements;
import dev.nokee.model.internal.DiscoveryService;
import dev.nokee.model.internal.ModelObject;
import dev.nokee.model.internal.ModelObjectIdentity;
import dev.nokee.model.internal.decorators.Decorator;
import dev.nokee.model.internal.decorators.DeriveNameFromPropertyNameNamer;
import dev.nokee.model.internal.decorators.NestedObjectNamer;
import dev.nokee.model.internal.decorators.ObjectTypeVisitor;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.ModelTypeHierarchy;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class DiscoverableStrategy implements Discovery {
	@Override
	public <T> List<DiscoveryService.DiscoveredEl> discover(ModelType<T> discoveringType) {
		final List<DiscoveryService.DiscoveredEl> result = new ArrayList<>();
		ModelTypeHierarchy.supertypeFirst(new MethodFieldVisitor(new NotPrivateOrStaticMethodsVisitor(new MethodFieldVisitor.ClassMethodFieldVisitor() {
			@Override
			public void visitClass(Class<?> type) {

			}

			@Override
			public void visitConstructor(Constructor<?> constructor) {

			}

			@Override
			public void visitMethod(Method method) {
				annotationsOn(method).filter(Discoverable.class::isInstance).forEach(annotation -> {
					ModelType<?> returnType = returnTypeOf(method);
					ElementName elementName = elementNameOf(method);

					final DiscoveredElements.ModelObjectIdentifierResolver childIdentifier = ((Supplier<DiscoveredElements.ModelObjectIdentifierResolver>) () -> {
						if (elementName == null) {
							return (DiscoveredElements.ModelObjectIdentifierResolver) identifier -> identifier;
						} else {
							return identifier -> identifier.child(elementName);
						}
					}).get();

					result.add(new DiscoveryService.DiscoveredEl() {
						@Override
						public ElementName getName() {
							return elementName;
						}

						@Override
						public ModelType<?> getType() {
							return unpackDomainObjectType(returnType.getType());
						}

						@Override
						public DiscoveryService.Scope getTarget() {
							return returnType.isSubtypeOf(Provider.class) || returnType.isSubtypeOf(ModelObject.class) ? DiscoveryService.Scope.Registered : DiscoveryService.Scope.Realized;
						}

						@Override
						public List<DiscoveryService.RealizedDiscoveredEl> execute(ModelObjectIdentity<?> identity) {
							if (identity.getType().equals(discoveringType)) {
								return Collections.singletonList(new DiscoveryService.RealizedDiscoveredEl() {
									@Override
									public DiscoveredElements.Element toElement(DiscoveredElements.Element parent) {
										return new DiscoveredElements.Element(ModelObjectIdentity.ofIdentity(childIdentifier.resolve(identity.getIdentifier()), getType()), parent);
									}
								});
							} else {
								return Collections.emptyList();
							}
						}
					});
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
					return ModelType.of(TypeToken.of(discoveringType.getType()).resolveType(m.getGenericReturnType()).getType());
				} catch (NoSuchMethodException e) {
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
		}))).walk(discoveringType);
		return result;
	}
}
