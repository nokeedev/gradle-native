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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public final class CompositeSpec implements XCBuildSpec, Iterable<XCBuildSpec> {
	private final List<XCBuildSpec> specs;

	CompositeSpec(List<XCBuildSpec> specs) {
		this.specs = specs;
	}

	@Override
	public Iterator<XCBuildSpec> iterator() {
		return specs.iterator();
	}

	@Override
	public XCBuildPlan resolve(ResolveContext context) {
		return new CompositeXCBuildPlan(specs.stream().map(it -> it.resolve(context)).collect(Collectors.toList()));
	}

	@Override
	public void visit(Visitor visitor) {
		for (int i = 0; i < specs.size(); i++) {
			XCBuildSpec it = specs.get(i);
			visitor.enterContext(String.valueOf(i));
			it.visit(visitor);
			visitor.exitContext();
		}
	}

	private static final class CompositeXCBuildPlan implements XCBuildPlan, Iterable<XCBuildPlan> {
		private final List<XCBuildPlan> values;

		public CompositeXCBuildPlan(List<XCBuildPlan> values) {
			this.values = values;
		}

		@Override
		public Iterator<XCBuildPlan> iterator() {
			return values.iterator();
		}
	}
}
