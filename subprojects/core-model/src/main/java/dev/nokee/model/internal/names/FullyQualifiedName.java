/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.internal.names;

import dev.nokee.model.internal.core.ModelNode;
import lombok.EqualsAndHashCode;
import org.gradle.api.Namer;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A fully qualified name represent a unique name of a domain object which include the name of the domain object as well as all of its owners.
 * Although the name typically represent the domain object's ownership hierarchy, users should not use it as such.
 * Typically, Gradle uses the fully qualified name in the {@link org.gradle.api.Named} interface.
 * The Nokee plugins always return the fully qualified name when implementing the {@link org.gradle.api.Named} interface.
 *
 * It's important to make a distinction between the fully qualified name, the partially qualified name, and the element name.
 * The fully qualified name is unique among domain objects with the same base type.
 * The partially qualified name is not unique and includes the domain object's ownership hierarchy from an arbitrary ancestor.
 * The element name is not unique and represent only the domain object's name, without any ownership.
 * Note that a partially qualified name using the domain object as the base ancestor will be the same as the element name.
 */
@EqualsAndHashCode
public final class FullyQualifiedName {
	private final String name;

	private FullyQualifiedName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static FullyQualifiedName of(String name) {
		return new FullyQualifiedName(name);
	}

	public static Collector<ModelNode, ?, FullyQualifiedName> toFullyQualifiedName(ElementName elementName) {
		return new Collector<ModelNode, QualifyingName.Builder, FullyQualifiedName>() {
			@Override
			public Supplier<QualifyingName.Builder> supplier() {
				return QualifyingName.Builder::new;
			}

			@Override
			public BiConsumer<QualifyingName.Builder, ModelNode> accumulator() {
				return new QualifyingNameAccumulator();
			}

			@Override
			public BinaryOperator<QualifyingName.Builder> combiner() {
				return (a, b) -> b.prepend(a.build().toString());
			}

			@Override
			public Function<QualifyingName.Builder, FullyQualifiedName> finisher() {
				return builder -> of(elementName.toQualifiedName(builder.build()));
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.emptySet();
			}
		};
	}
}
