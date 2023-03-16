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

import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.copyOf;

@EqualsAndHashCode
public final class PBXObjectReferenceKeyedObject implements KeyedObject {
	@EqualsAndHashCode.Exclude private final long age;
	@EqualsAndHashCode.Exclude private final PBXObjects objects;
	@EqualsAndHashCode.Include private final PBXObjectReference reference;
	@EqualsAndHashCode.Exclude private final CodingKeyCoders coders;

	public PBXObjectReferenceKeyedObject(PBXObjects objects, PBXObjectReference reference, CodingKeyCoders coders) {
		this(objects, reference, coders, System.nanoTime());
	}

	private PBXObjectReferenceKeyedObject(PBXObjects objects, PBXObjectReference reference, CodingKeyCoders coders, long age) {
		assert objects != null;
		assert reference != null;
		assert coders != null;
		this.objects = objects;
		this.reference = reference;
		this.coders = coders;
		this.age = age;
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return tryDecode(key, copyOf(reference.getFields().entrySet()));
	}

	@SuppressWarnings("unchecked")
	private <T> T tryDecode(CodingKey key, Map<String, ?> fields) {
		final KeyedCoder<?> coder = coders.get(key).orElseThrow(() -> new UnsupportedOperationException("No decoder for " + key + "."));
		return (T) coder.decode(decodeContextOf(fields));
	}

	private KeyedDecoder decodeContextOf(Map<String, ?> value) {
		return new KeyedDecoderAdapter(value) {
			@Override
			public KeyedObject decodeBycopyObject(Map<String, ?> object) {
				return new KeyedObject() {
					@Override
					public <T> T tryDecode(CodingKey key) {
						return PBXObjectReferenceKeyedObject.this.tryDecode(key, object);
					}

					@Nullable
					@Override
					public String globalId() {
						return null;
					}

					@Override
					public long age() {
						return PBXObjectReferenceKeyedObject.this.age;
					}

					@Override
					public String isa() {
						return tryDecode(KeyedCoders.ISA);
					}

					@Override
					public void encode(EncodeContext context) {
						context.noGid();
						context.base(object);
					}

					@Override
					public String toString() {
						return String.format("%s gid=none", Optional.ofNullable(object.get("isa")).map(Object::toString).orElse("object"));
					}
				};
			}

			@Override
			public KeyedObject decodeByrefObject(String gid) {
				// keep the same age for all object decoded from this object
				return new PBXObjectReferenceKeyedObject(objects, objects.getById(gid), coders, age);
			}
		};
	}

	@Nullable
	@Override
	public String globalId() {
		return reference.getGlobalID();
	}

	@Override
	public long age() {
		return age;
	}

	@Override
	public String isa() {
		return reference.isa();
	}

	@Override
	public void encode(EncodeContext context) {
		context.gid(reference.getGlobalID());
		context.base(copyOf(reference.getFields().entrySet()));
	}

	@Override
	public String toString() {
		return String.format("%s gid=%s", reference.isa(), reference.getGlobalID());
	}
}
