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
import dev.nokee.xcode.objects.PBXObject;
import dev.nokee.xcode.objects.PBXProject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;
import static java.util.function.Function.identity;

public final class PBXObjectUnarchiver {
	private final Map<String, PBXObjectCoder<?>> coders;

	public PBXObjectUnarchiver() {
		this(ImmutableList.copyOf(PBXObjectCoders.values()));
	}

	public PBXObjectUnarchiver(Iterable<PBXObjectCoder<?>> coders) {
		this.coders = stream(coders).collect(ImmutableMap.toImmutableMap(it -> it.getType().getSimpleName(), identity()));
	}

	public PBXProject decode(PBXProj proj) {
		return new PBXObjectDecoder(proj.getObjects()).decode(proj.getRootObject());
	}

	private final class PBXObjectDecoder {
		private final Map<String, PBXObject> decodedObjects = new HashMap<>();
		private final PBXObjects objects;

		public PBXObjectDecoder(PBXObjects objects) {
			this.objects = objects;
		}

		public <T extends PBXObject> T decode(String gid) {
			assert gid != null;
			return decode(objects.getById(gid));
		}

		@SuppressWarnings("unchecked")
		private <T extends PBXObject> T decode(PBXObjectReference objectRef) {
			// DO NOT USE computeIfAbsent as it's not reentrant
			T result = (T) decodedObjects.get(objectRef.getGlobalID());
			if (result == null) {
				result = (T) Objects.requireNonNull(coders.get(objectRef.isa()), "missing coder for '" + objectRef.isa() + "'").read(new BaseDecoder(this, objectRef.getFields()));
				decodedObjects.put(objectRef.getGlobalID(), result);
			}
			return result;
		}
	}

	private final class BaseDecoder implements PBXObjectCoder.Decoder {
		private final PBXObjectDecoder delegate;
		private final PBXObjectFields object;

		public BaseDecoder(PBXObjectDecoder delegate, PBXObjectFields object) {
			this.delegate = delegate;
			this.object = object;
		}

		@Override
		public <S extends PBXObject> void decodeObjectsIfPresent(String key, Consumer<? super Iterable<S>> consumer) {
			Optional.ofNullable(object.get(key)).ifPresent(it -> consumer.accept(stream((Iterable<?>) it)
				.map(Object::toString).map(delegate::<S>decode).collect(toImmutableList())));
		}

		@Override
		@SuppressWarnings("unchecked")
		public <S extends PBXObject> void decodeObjectIfPresent(String key, Consumer<? super S> consumer) {
			Optional.ofNullable(object.get(key)).ifPresent(it -> consumer.accept((S) delegate.decode(it.toString())));
		}

		@Override
		public void decodeStringIfPresent(String key, Consumer<? super String> consumer) {
			Optional.ofNullable(object.get(key)).ifPresent(it -> consumer.accept(it.toString()));
		}

		@Override
		@SuppressWarnings("unchecked")
		public void decodeMapIfPresent(String key, Consumer<? super Map<String, Object>> consumer) {
			Optional.ofNullable(object.get(key)).ifPresent(it -> consumer.accept((Map<String, Object>) it));
		}

		@Override
		public void decodeArrayIfPresent(String key, Consumer<? super Iterable<?>> consumer) {
			Optional.ofNullable(object.get(key)).ifPresent(it -> consumer.accept((Iterable<?>) it));
		}
	}
}
