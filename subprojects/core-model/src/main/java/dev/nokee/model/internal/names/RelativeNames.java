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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.core.ModelNode;
import lombok.ToString;
import org.gradle.api.Namer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

@ToString
public final class RelativeNames implements Iterable<RelativeName> {
	private final Map<RelativeName.BaseRef, RelativeName> values;

	private RelativeNames(Map<RelativeName.BaseRef, RelativeName> values) {
		this.values = values;
	}

	public RelativeName get(RelativeName.BaseRef baseRef) {
		return values.get(baseRef);
	}

	@Override
	public Iterator<RelativeName> iterator() {
		return values.values().iterator();
	}

	public static RelativeNames of(Set<RelativeName> names) {
		return new RelativeNames(names.stream().collect(ImmutableMap.toImmutableMap(RelativeName::baseRef, it -> it)));
	}

	public static RelativeNames of(RelativeName firstName, RelativeName... otherNames) {
		return new RelativeNames(Stream.concat(Stream.of(firstName), Arrays.stream(otherNames)).collect(ImmutableMap.toImmutableMap(RelativeName::baseRef, it -> it)));
	}

	public static Collector<ModelNode, ?, RelativeNames> toRelativeNames(ElementName elementName) {
		return new Collector<ModelNode, ImmutableSet.Builder<RelativeName>, RelativeNames>() {
			@Override
			public Supplier<ImmutableSet.Builder<RelativeName>> supplier() {
				return ImmutableSet::builder;
			}

			@Override
			public BiConsumer<ImmutableSet.Builder<RelativeName>, ModelNode> accumulator() {
				return new BiConsumer<ImmutableSet.Builder<RelativeName>, ModelNode>() {
					private final QualifyingName.Builder builder = new QualifyingName.Builder();
					private final QualifyingNameAccumulator accumulator = new QualifyingNameAccumulator();

					@Override
					public void accept(ImmutableSet.Builder<RelativeName> builder, ModelNode parentEntity) {
						builder.add(RelativeName.of(parentEntity, elementName.toQualifiedName(this.builder.build())));
						accumulator.accept(this.builder, parentEntity);
					}
				};
			}

			@Override
			public BinaryOperator<ImmutableSet.Builder<RelativeName>> combiner() {
				return (a, b) -> a.addAll(b.build());
			}

			@Override
			public Function<ImmutableSet.Builder<RelativeName>, RelativeNames> finisher() {
				return builder -> RelativeNames.of(builder.build());
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.emptySet();
			}
		};
	}
}
