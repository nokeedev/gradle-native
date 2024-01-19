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

import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.internal.discover.DisRule;
import dev.nokee.model.internal.discover.Discover;
import dev.nokee.model.internal.discover.Discovery;
import dev.nokee.model.internal.type.ModelType;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiscoveryService implements Discovery {
	private final Instantiator instantiator;

	public DiscoveryService(Instantiator instantiator) {
		this.instantiator = instantiator;
	}

	@Override
	public <T> List<DisRule> discover(ModelType<T> discoveringType) {
		return annotationsOn(discoveringType.getConcreteType()).filter(Discover.class::isInstance)
			.flatMap(it -> instantiator.newInstance(((Discover) it).value()).discover(discoveringType).stream()).collect(Collectors.toList());
	}

	// FIXME(discovery): Streamline meta-annotation discovery (most likely in utils)
	private Stream<Annotation> annotationsOn(Class<?> type) {
		Set<Annotation> seen = new LinkedHashSet<>();
		Deque<Annotation> queue = new ArrayDeque<>();

		queue.addAll(Arrays.asList(type.getAnnotations()));
		while (!queue.isEmpty()) {
			Annotation current = queue.removeFirst();
			if (!seen.add(current)) {
				continue;
			}
			queue.addAll(Arrays.asList(current.annotationType().getAnnotations()));
		}
		return seen.stream();
	}
}
