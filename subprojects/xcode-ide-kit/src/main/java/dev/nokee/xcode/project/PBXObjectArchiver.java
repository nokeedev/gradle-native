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

import dev.nokee.xcode.objects.PBXProject;
import lombok.val;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.copyOf;

public final class PBXObjectArchiver {
	private final GidGenerator gidGenerator;
	private final CodingKeyCoders coders;

	public PBXObjectArchiver() {
		this(new GidGenerator(Collections.emptySet()));
	}

	public PBXObjectArchiver(GidGenerator gidGenerator) {
		this(gidGenerator, it -> Optional.ofNullable(KeyedCoders.DECODERS.get(it)));
	}

	public PBXObjectArchiver(GidGenerator gidGenerator, CodingKeyCoders coders) {
		this.gidGenerator = gidGenerator;
		this.coders = coders;
	}

	public <T extends PBXProject> PBXProj encode(T obj) {
		PBXObjects.Builder objects = PBXObjects.builder();
		Map<Codeable, String> encodedObjects = new HashMap<>();
		PBXObjectReference rootObject = encodeRefInternal(objects, encodedObjects, (Codeable) obj);
		objects.add(rootObject);
		return PBXProj.builder().objects(objects.build()).rootObject(rootObject.getGlobalID()).build();
	}

	interface KnownGlobalIdentificationCallback {
		void onKnownGlobalId(String gid);
	}

	private <T extends Codeable> PBXObjectReference encodeRefInternal(PBXObjects.Builder objects, Map<Codeable, String> encodedObjects, T obj) {
		assert !encodedObjects.containsKey(obj);
		val context = encodeContextOf(objects, encodedObjects, gid -> encodedObjects.put(obj, gid));
		obj.encode(context);
		context.flushEncoding();

		String gid = context.gid;
		if (gid == null) {
			gid = gidGenerator.generateGid(obj.isa(), obj.stableHash());
			encodedObjects.put(obj, gid);
		}

		PBXObjectReference reference = PBXObjectReference.of(gid, it -> {
			context.map.forEach((a, b) -> it.putField(a, b));
		});
		return reference;
	}

	private MyEncodeContext encodeContextOf(PBXObjects.Builder objects, Map<Codeable, String> encodedObjects, KnownGlobalIdentificationCallback watgid) {
		return new MyEncodeContext(objects, encodedObjects, watgid);
	}

	private class MyEncodeContext implements Codeable.EncodeContext {
		private final PBXObjects.Builder objects;
		private final Map<Codeable, String> encodedObjects;
		private final KnownGlobalIdentificationCallback knownGlobalIdCallback;
		String gid;
		Map<String, Object> map;
		Map<CodingKey, Object> codingMap = new LinkedHashMap<>();

		public MyEncodeContext(PBXObjects.Builder objects, Map<Codeable, String> encodedObjects, KnownGlobalIdentificationCallback knownGlobalIdCallback) {
			this.objects = objects;
			this.encodedObjects = encodedObjects;
			this.knownGlobalIdCallback = knownGlobalIdCallback;
			gid = null;
			map = new LinkedHashMap<>();
		}

		@Override
		public void base(Map<String, ?> fields) {
			val result = new LinkedHashMap<String, Object>(fields);
			result.putAll(map);
			map = result;
		}

		@Override
		public void gid(String globalID) {
			gid = globalID;
			knownGlobalIdCallback.onKnownGlobalId(globalID);
		}

		@Override
		public void noGid() {
			gid = null;
		}

		@Override
		public void tryEncode(Map<CodingKey, ?> data) {
			codingMap.putAll(data);
		}

		public void flushEncoding() {
			codingMap.forEach((key, obj) -> {
				@SuppressWarnings("unchecked")
				final KeyedCoder<Object> coder = (KeyedCoder<Object>) coders.get(key).orElseThrow(() -> new UnsupportedOperationException("Coder for " + key + " (" + key.getClass().getName().substring(key.getClass().getName().lastIndexOf('.') + 1) + ") not found."));
				coder.encode(obj, new KeyedEncoderAdapter() {
					@Override
					protected void tryEncode(String key, Object value) {
						map.put(key, value);
					}

					@Override
					protected String encodeRef(Codeable object) {
						// Skip encoding if object already encoded
						if (encodedObjects.containsKey(object)) {
							return encodedObjects.get(object);
						} else if (object.globalId() != null && encodedObjects.containsValue(object.globalId())) {
							return object.globalId(); // already encoded but the object was different, we really only care about the reference (ex. PBXProject was changed but some other object still reference the old object)
						}
						final PBXObjectReference reference = encodeRefInternal(objects, encodedObjects, object);
						objects.add(reference);
						return reference.getGlobalID();
					}

					@Override
					protected Map<String, ?> encodeObj(Codeable object) {
						val context = encodeContextOf(objects, encodedObjects, gid -> encodedObjects.put(object, gid));
						object.encode(context);
						assert context.gid == null;
						return copyOf(context.map);
					}
				});
			});
			codingMap.clear();
		}
	}
}
