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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public final class SuperClassFirstClassVisitor {
	private final ClassVisitor visitor;

	public SuperClassFirstClassVisitor(ClassVisitor visitor) {
		this.visitor = visitor;
	}

	public void visitClass(Class<?> type) {
		Set<Class<?>> seen = new HashSet<Class<?>>();
		Deque<Class<?>> queue = new ArrayDeque<Class<?>>();

		queue.add(type);
		superClasses(type, queue);
		while (!queue.isEmpty()) {
			Class<?> current = queue.removeFirst();
			if (!seen.add(current)) {
				continue;
			}
			visitor.visitClass(current);
			Collections.addAll(queue, current.getInterfaces());
		}
	}

	private static void superClasses(Class<?> current, Collection<Class<?>> supers) {
		Class<?> superclass = current.getSuperclass();
		while (superclass != null) {
			supers.add(superclass);
			superclass = superclass.getSuperclass();
		}
	}
}
