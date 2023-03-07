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
package dev.nokee.xcode;

import dev.nokee.buildadapter.xcode.internal.plugins.specs.NestedMapSpec;
import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildSpec;
import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildSpecCodingKeyCoders;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.project.CodingKey;
import dev.nokee.xcode.project.CodingKeyCoders;
import dev.nokee.xcode.project.Encodeable;
import dev.nokee.xcode.project.KeyedCoder;
import dev.nokee.xcode.project.KeyedEncoderAdapter;
import dev.nokee.xcode.project.coders.BycopyObject;
import dev.nokee.xcode.project.coders.ByrefObject;
import dev.nokee.xcode.project.coders.DefaultBycopyObject;
import lombok.val;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.copyOf;

public final class XCBuildSpecLoader implements XCLoader<XCBuildSpec, XCTargetReference>, Serializable {
	private final XCLoader<PBXTarget, XCTargetReference> loader;

	public XCBuildSpecLoader(XCLoader<PBXTarget, XCTargetReference> loader) {
		this.loader = loader;
	}

	@Override
	public XCBuildSpec load(XCTargetReference reference) {
		final PBXTarget target = loader.load(reference);

		val context = new MyEncodeContext();
		((Encodeable) target).encode(context);

		return context.value();
	}

	private static final class MyEncodeContext implements Encodeable.EncodeContext {
		CodingKeyCoders coders = new XCBuildSpecCodingKeyCoders();
		private final Map<String, XCBuildSpec> map = new LinkedHashMap<>();

		public XCBuildSpec value() {
			return new NestedMapSpec(map);
		}

		@Override
		public void base(Map<String, ?> fields) {
			// ignores fields
		}

		@Override
		public void gid(String globalID) {
			// ignores GID
		}

		@Override
		@SuppressWarnings("unchecked")
		public void tryEncode(Map<CodingKey, ?> data) {
			data.forEach((CodingKey k, Object v) -> {
				coders.get(k).ifPresent(it -> ((KeyedCoder<Object>) it).encode(v, new KeyedEncoderAdapter() {
					@Override
					protected void tryEncode(String key, Object value) {
						assert value instanceof XCBuildSpec;
						map.put(key, (XCBuildSpec) value);
					}

					@Override
					public BycopyObject encodeBycopyObject(Encodeable object) {
						val context = new MyEncodeContext();
						object.encode(context);
						return new DefaultBycopyObject(copyOf(context.map));
					}

					@Override
					public ByrefObject encodeByrefObject(Encodeable object) {
						throw new UnsupportedOperationException();
					}
				}));
			});
		}

		@Override
		public void noGid() {
			// ignores GID
		}
	}
}
