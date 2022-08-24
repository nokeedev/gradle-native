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
package dev.nokee.xcode.project;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import dev.nokee.xcode.objects.PBXObject;
import dev.nokee.xcode.objects.PBXProject;
import lombok.val;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class PBXObjectArchiver {
	private final GidGenerator gidGenerator;
	private final Map<String, PBXObjectCoder<Object>> coders;
	private final StableHasher stableHasher;

	public PBXObjectArchiver() {
		this(new GidGenerator(Collections.emptySet()));
	}

	public PBXObjectArchiver(GidGenerator gidGenerator) {
		this(gidGenerator, ImmutableList.copyOf(PBXObjectCoders.values()), PBXObjectCoders::stableHash);
	}

	@SuppressWarnings("unchecked")
	public PBXObjectArchiver(GidGenerator gidGenerator, Iterable<PBXObjectCoder<?>> coders, StableHasher stableHasher) {
		this.gidGenerator = gidGenerator;
		this.coders = Streams.stream(coders).collect(ImmutableMap.toImmutableMap(it -> it.getType().getSimpleName(), it -> (PBXObjectCoder<Object>) it));
		this.stableHasher = stableHasher;
	}

	public PBXProj encode(PBXProject obj) {
		PBXObjects.Builder objects = PBXObjects.builder();
		PBXObjectReference rootObject = new PBXObjectEncoder(new ConvertContext(objects)).encode(obj);
		return PBXProj.builder().objects(objects.build()).rootObject(rootObject.getGlobalID()).build();
	}

	private final class PBXObjectEncoder {
		private final ConvertContext db;

		private PBXObjectEncoder(ConvertContext db) {
			this.db = db;
		}

		public Object encode(Object o) {
			if (o instanceof PBXObject) {
				return encode((PBXObject) o);
			} else if (coders.containsKey(o.getClass().getSimpleName())) {
				val builder = PBXObjectFields.builder();
				coders.get(o.getClass().getSimpleName()).write(new BaseEncoder(this, builder), o);
				return ImmutableMap.copyOf(builder.build().entrySet());
			} else {
				return o;
			}
		}

		public PBXObjectReference encode(PBXObject o) {
			return db.newObjectIfAbsent(o, obj -> {
				obj.putField("isa", isa(o));
				requireNonNull(coders.get(isa(o)), isa(o)).write(new BaseEncoder(this, obj), o);
			});
		}
	}

	private static final class BaseEncoder implements PBXObjectCoder.Encoder {
		private final PBXObjectEncoder delegate;
		private final PBXObjectFields.Builder builder;

		BaseEncoder(PBXObjectEncoder delegate, PBXObjectFields.Builder builder) {
			this.delegate = delegate;
			this.builder = builder;
		}

		@Override
		public void encode(String key, Object value) {
			if (value instanceof Iterable) {
				builder.putField(key, Streams.stream((Iterable<?>) value).map(this::encode).collect(ImmutableList.toImmutableList()));
			} else {
				builder.putField(key, encode(value));
			}
		}

		private Object encode(Object value) {
			return delegate.encode(value);
		}
	}

	private static String isa(Object o) {
		return o.getClass().getSimpleName();
	}

	private final class ConvertContext {
		private final PBXObjects.Builder builder;
		private final Map<Object, String> knownGlobalIds = new HashMap<>();
		private final Map<String, PBXObjectReference> objects = new LinkedHashMap<>();

		private ConvertContext(PBXObjects.Builder builder) {
			this.builder = builder;
		}

		public PBXObjectReference newObjectIfAbsent(Object o, Consumer<? super PBXObjectFields.Builder> action) {
			return newObjectIfAbsent(knownGlobalIds.computeIfAbsent(o, it -> gidGenerator.generateGid(isa(o), stableHasher.stableHash(o))), action);
		}

		public PBXObjectReference newObjectIfAbsent(String id, Consumer<? super PBXObjectFields.Builder> action) {
			if (objects.containsKey(id)) {
				return objects.get(id);
			} else {
				val result = PBXObjectReference.of(id, action);
				objects.put(id, result);
				builder.add(result);
				return result;
			}
		}
	}

	public interface StableHasher {
		/**
		 * This method is used to generate stable GIDs and must be stable for identical contents.
		 * Returning a constant value is ok but will make the generated project order-dependent.
		 *
		 * @return stable hash
		 */
		int stableHash(Object o);
	}
}
