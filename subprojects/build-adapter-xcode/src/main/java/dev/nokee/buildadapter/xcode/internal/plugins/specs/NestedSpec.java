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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import org.gradle.api.tasks.Nested;

import java.util.AbstractMap;
import java.util.Map;

@EqualsAndHashCode
public final class NestedSpec implements XCBuildSpec {
	private final Map<String, XCBuildSpec> values;

	public NestedSpec(Map<String, XCBuildSpec> values) {
		this.values = values;
	}

	@Override
	public XCBuildPlan resolve(ResolveContext context) {
		return new XCBuildPlan() {
			private final Map<String, XCBuildPlan> values = NestedSpec.this.values.entrySet().stream().map(it -> new AbstractMap.SimpleImmutableEntry<>(it.getKey(), it.getValue().resolve(context))).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

			@Nested
			public Map<String, XCBuildPlan> getValue() {
				return values;
			}
		};
	}

	@Override
	public void visit(Visitor visitor) {
		values.forEach((k, v) -> {
			visitor.enterContext(k);
			v.visit(visitor);
			visitor.exitContext();
		});
	}
}
