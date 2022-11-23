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
	@EqualsAndHashCode.Exclude private final PBXObjects objects;
	@EqualsAndHashCode.Include private final PBXObjectReference reference;
	@EqualsAndHashCode.Exclude private final CodingKeyCoders coders;

	public PBXObjectReferenceKeyedObject(PBXObjects objects, PBXObjectReference reference, CodingKeyCoders coders) {
		assert objects != null;
		assert reference != null;
		assert coders != null;
		this.objects = objects;
		this.reference = reference;
		this.coders = coders;
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
		return new KeyedDecoderAdapter() {
			@Override
			protected <R> R tryDecode(String key, ValueCoder<R> decoder) {
				if (!value.containsKey(key)) {
					return null;
				}

				return decoder.decode(new DecoderAdapter(value.get(key)) {
					@Override
					protected KeyedObject create(String gid) {
						return new PBXObjectReferenceKeyedObject(objects, objects.getById(gid), coders);
					}

					@Override
					protected KeyedObject create(Map<String, ?> value) {
						return new KeyedObject() {
							@Override
							public <T> T tryDecode(CodingKey key) {
								return PBXObjectReferenceKeyedObject.this.tryDecode(key, value);
							}

							@Nullable
							@Override
							public String globalId() {
								return null;
							}

							@Override
							public String isa() {
								return tryDecode(KeyedCoders.ISA);
							}

							@Override
							public void encode(EncodeContext context) {
								context.noGid();
								context.base(value);
							}

							@Override
							public String toString() {
								return String.format("%s gid=none", Optional.ofNullable(value.get("isa")).map(Object::toString).orElse("object"));
							}
						};
					}
				});
			}
		};
	}

	@Nullable
	@Override
	public String globalId() {
		return reference.getGlobalID();
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
