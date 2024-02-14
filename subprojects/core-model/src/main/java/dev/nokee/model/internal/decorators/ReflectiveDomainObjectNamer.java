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
package dev.nokee.model.internal.decorators;

import com.google.common.collect.MoreCollectors;
import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.internal.names.ElementName;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public final class ReflectiveDomainObjectNamer implements DomainObjectNamer {
	private final Instantiator instantiator;
	private final DomainObjectNamer defaultNamer;

	public ReflectiveDomainObjectNamer(Instantiator instantiator, DomainObjectNamer defaultNamer) {
		this.instantiator = instantiator;
		this.defaultNamer = defaultNamer;
	}

	@Nullable
	@Override
	public ElementName determineName(Decorator.MethodMetadata method) {
		return method.getAnnotations().flatMap(it -> it instanceof ObjectName ? Stream.of((ObjectName) it) : Stream.empty()).collect(MoreCollectors.toOptional()).map(it -> (DomainObjectNamer) instantiator.newInstance(it.value())).orElse(defaultNamer).determineName(method);
	}
}
