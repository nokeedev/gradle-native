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
package dev.nokee.model.internal.actions;

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.specs.Spec;

import java.util.Set;

@EqualsAndHashCode
final class AndSpec implements Spec<DomainObjectIdentity>, ModelSpec {
	@EqualsAndHashCode.Include private final Set<ModelSpec> specs;

	public AndSpec(ModelSpec first, ModelSpec second) {
		this(consolidate(first, second));
	}

	public AndSpec(Set<ModelSpec> specs) {
		this.specs = specs;
	}

	@Override
	public ModelSpec and(ModelSpec other) {
		return new AndSpec(consolidate(this, other));
	}

	private static Set<ModelSpec> consolidate(ModelSpec first, ModelSpec second) {
		val builder = ImmutableSet.<ModelSpec>builder();
		if (first instanceof AndSpec) {
			builder.addAll(((AndSpec) first).specs);
		} else {
			builder.add(first);
		}

		if (second instanceof AndSpec) {
			builder.addAll(((AndSpec) second).specs);
		} else {
			builder.add(second);
		}

		return builder.build();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isSatisfiedBy(DomainObjectIdentity element) {
		for (ModelSpec it : specs) {
			if (!((Spec<DomainObjectIdentity>) it).isSatisfiedBy(element)) {
				return false;
			}
		}
		return true;
	}
}
