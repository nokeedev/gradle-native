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
import dev.nokee.model.internal.decorators.Decorator;
import dev.nokee.model.internal.decorators.DeriveNameFromPropertyNameNamer;
import dev.nokee.model.internal.decorators.NestedObjectNamer;
import dev.nokee.model.internal.decorators.ObjectTypeVisitor;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.Annotations;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.ModelTypeHierarchy;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class DiscoverableStrategy implements Discovery {
	@Override
	public <T> List<DisRule> discover(ModelType<T> discoveringType) {
		final List<GroupRule.Entry> result = new ArrayList<>();
		ModelTypeHierarchy.supertypeFirst(new MethodFieldVisitor(new NotPrivateOrStaticMethodsVisitor(new MethodFieldVisitor.ClassMethodFieldVisitor() {
			@Override
			public void visitClass(Class<?> type) {

			}

			@Override
			public void visitConstructor(Constructor<?> constructor) {

			}

			@Override
			public void visitMethod(Method method) {
				Annotations.forAnnotation(Discoverable.class).findOn(method).forEach(annotation -> {
					ModelType<?> returnType = returnTypeOf(method);
					ElementName elementName = elementNameOf(method);

					// FIXME(discovery): Deal with null ElementName... somewhat fixed
					result.add(new GroupRule.Entry(elementName, unpackDomainObjectType(returnType.getType())));
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
		}))).walk(discoveringType);
		return Collections.singletonList(new RealizeRule(new GroupRule(discoveringType, result)));
	}
}
