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

public final class MethodFieldVisitor implements ClassVisitor {
	private final ClassMethodFieldVisitor visitor;

	public MethodFieldVisitor(ClassMethodFieldVisitor visitor) {
		this.visitor = visitor;
	}

	@Override
	public void visitClass(Class<?> type) {
		visitor.visitClass(type);

		for (Constructor<?> constructor : type.getDeclaredConstructors()) {
			visitor.visitConstructor(constructor);
		}

		for (Method method : type.getDeclaredMethods()) {
			visitor.visitMethod(method);
		}

		// No support for field for now

		visitor.visitEnd();
	}

	public interface ClassMethodFieldVisitor {
		void visitClass(Class<?> type);
		void visitConstructor(Constructor<?> constructor);
		void visitMethod(Method method);
		void visitEnd();
	}
}
