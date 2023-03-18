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

import dev.nokee.xcode.project.Encodeable;
import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode
public final class AtNestedMapEncoder<IN extends Encodeable> implements ValueEncoder<XCBuildSpec, IN> {
	private final ValueEncoder<Encodeable, IN> delegate;

	public AtNestedMapEncoder(ValueEncoder<Encodeable, IN> delegate) {
		this.delegate = delegate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public XCBuildSpec encode(IN value, Context context) {
		return new Spec((Map<String, XCBuildSpec>) context.encodeBycopyObject(delegate.encode(value, context)).asMap());
	}

	@EqualsAndHashCode
	public static final class Spec implements XCBuildSpec {
		private final Map<String, XCBuildSpec> values;

		public Spec(Map<String, XCBuildSpec> values) {
			this.values = values;
		}

		@Override
		public XCBuildPlan resolve(ResolveContext context) {
			final Map<String, XCBuildPlan> values = new HashMap<>(this.values.size());
			for (Map.Entry<String, XCBuildSpec> entry : this.values.entrySet()) {
				values.put(entry.getKey(), entry.getValue().resolve(context));
			}
			return new MapXCBuildPlan(values);
		}

		@Override
		public void visit(Visitor visitor) {
			values.forEach((k, v) -> {
				visitor.enterContext(k);
				v.visit(visitor);
				visitor.exitContext();
			});
		}

		// We implement a Map so Gradle can present the change in a much nicer way
		@EqualsAndHashCode
		private static final class MapXCBuildPlan implements XCBuildPlan, Map<String, XCBuildPlan> {
			private final Map<String, XCBuildPlan> values;

			public MapXCBuildPlan(Map<String, XCBuildPlan> values) {
				this.values = values;
			}

			@Override
			public int size() {
				return values.size();
			}

			@Override
			public boolean isEmpty() {
				return values.isEmpty();
			}

			@Override
			public boolean containsKey(Object key) {
				return values.containsKey(key);
			}

			@Override
			public boolean containsValue(Object value) {
				return values.containsValue(value);
			}

			@Override
			public XCBuildPlan get(Object key) {
				return values.get(key);
			}

			@Override
			public XCBuildPlan put(String key, XCBuildPlan value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public XCBuildPlan remove(Object key) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void putAll(Map<? extends String, ? extends XCBuildPlan> m) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Set<String> keySet() {
				return values.keySet();
			}

			@Override
			public Collection<XCBuildPlan> values() {
				return values.values();
			}

			@Override
			public Set<Entry<String, XCBuildPlan>> entrySet() {
				return values.entrySet();
			}
		}
	}
}
