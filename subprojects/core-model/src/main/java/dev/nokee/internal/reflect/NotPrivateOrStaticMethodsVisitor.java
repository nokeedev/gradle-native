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
package dev.nokee.internal.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class NotPrivateOrStaticMethodsVisitor implements MethodFieldVisitor.ClassMethodFieldVisitor {
	private final MethodFieldVisitor.ClassMethodFieldVisitor visitor;

	public NotPrivateOrStaticMethodsVisitor(MethodFieldVisitor.ClassMethodFieldVisitor visitor) {
		this.visitor = visitor;
	}

	@Override
	public void visitClass(Class<?> type) {
		visitor.visitClass(type);
	}

	@Override
	public void visitConstructor(Constructor<?> constructor) {
		if (Modifier.isPrivate(constructor.getModifiers())) {
			// skip
		} else {
			visitor.visitConstructor(constructor);
		}
	}

	@Override
	public void visitMethod(Method method) {
		if (Modifier.isPrivate(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
			// skip
		} else {
			visitor.visitMethod(method);
		}
	}

	@Override
	public void visitEnd() {
		visitor.visitEnd();
	}
}
