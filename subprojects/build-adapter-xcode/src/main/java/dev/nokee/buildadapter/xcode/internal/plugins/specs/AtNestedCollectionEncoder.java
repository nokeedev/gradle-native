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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;

@EqualsAndHashCode
public final class AtNestedCollectionEncoder<IN> implements ValueEncoder<XCBuildSpec, IN> {
	private final Collector<XCBuildPlan, ?, ? extends Collection<XCBuildPlan>> collector;
	private final ValueEncoder<List<XCBuildSpec>, IN> delegate;

	public AtNestedCollectionEncoder(Collector<XCBuildPlan, ?, ? extends Collection<XCBuildPlan>> collector, ValueEncoder<List<XCBuildSpec>, IN> delegate) {
		this.collector = collector;
		this.delegate = delegate;
	}

	@Override
	public XCBuildSpec encode(IN value, Context context) {
		return new Spec(collector, delegate.encode(value, context));
	}

	public static <IN> AtNestedCollectionEncoder<IN> atNestedSet(ValueEncoder<List<XCBuildSpec>, IN> encoder) {
		return new AtNestedCollectionEncoder<>(ImmutableSet.toImmutableSet(), encoder);
	}

	public static <IN> AtNestedCollectionEncoder<IN> atNestedList(ValueEncoder<List<XCBuildSpec>, IN> encoder) {
		return new AtNestedCollectionEncoder<>(ImmutableList.toImmutableList(), encoder);
	}

	@EqualsAndHashCode
	public static class Spec implements XCBuildSpec {
		private final Collector<XCBuildPlan, ?, ? extends Collection<XCBuildPlan>> collector;
		private final List<XCBuildSpec> specs;

		public Spec(Collector<XCBuildPlan, ?, ? extends Collection<XCBuildPlan>> collector, List<XCBuildSpec> specs) {
			this.collector = collector;
			this.specs = specs;
		}

		@Override
		public XCBuildPlan resolve(ResolveContext context) {
			val result = specs.stream().map(it -> it.resolve(context)).collect(collector);
			return new CompositeXCBuildPlan<>(result);
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
	}
}
