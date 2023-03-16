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
import dev.nokee.xcode.project.coders.BycopyObject;
import dev.nokee.xcode.project.coders.ByrefObject;
import dev.nokee.xcode.project.coders.DefaultBycopyObject;
import dev.nokee.xcode.project.coders.DefaultByrefObject;
import lombok.val;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.copyOf;

public final class PBXObjectArchiver {
	private final GidGenerator gidGenerator;
	private final CodingKeyCoders coders;

	public PBXObjectArchiver() {
		this(new GidGenerator(Collections.emptySet()));
	}

	public PBXObjectArchiver(GidGenerator gidGenerator) {
		this(gidGenerator, new PBXObjectCodingKeyCoders());
	}

	public PBXObjectArchiver(GidGenerator gidGenerator, CodingKeyCoders coders) {
		this.gidGenerator = gidGenerator;
		this.coders = coders;
	}

	public <T extends PBXProject> PBXProj encode(T obj) {
		Map<Encodeable, String> encodedObjects = new HashMap<>();
		Map<String, Encodeable> objectsToEncode = new HashMap<>();
		Map<String, PBXObjectReference> objectsToRef = new LinkedHashMap<>();

		val rootObjectGid = globalIdOf(encodedObjects, (Encodeable) obj);
		mark(objectsToRef, encodedObjects, objectsToEncode, rootObjectGid, (Encodeable) obj);

		PBXObjects.Builder objects = PBXObjects.builder();
		for (PBXObjectReference value : objectsToRef.values()) {
			objects.add(value);
		}
		return PBXProj.builder().objects(objects.build()).rootObject(rootObjectGid).build();
	}

	private <T extends Encodeable> PBXObjectReference encodeRefInternal(Map<String, PBXObjectReference> objectsToRef, String globalId, Map<Encodeable, String> encodedObjects, T obj, Map<String, Encodeable> objectsToEncode) {
		val context = encodeContextOf(objectsToRef, encodedObjects, objectsToEncode);
		obj.encode(context);
		context.flushEncoding();

		PBXObjectReference reference = PBXObjectReference.of(globalId, it -> {
			context.map.forEach((a, b) -> it.putField(a, b));
		});
		return reference;
	}

	private MyEncodeContext encodeContextOf(Map<String, PBXObjectReference> objectsToRef, Map<Encodeable, String> encodedObjects, Map<String, Encodeable> objectsToEncode) {
		return new MyEncodeContext(objectsToRef, encodedObjects, objectsToEncode);
	}

	private String globalIdOf(Map<Encodeable, String> encodedObjects, Encodeable object) {
		// TODO: Every created object should have a globalId (if loaded) or a unique placeholder globalId (if created)
		//   The idea is if the following:
		//    - If the models point to the same object, all object will be the same so we can match them together
		//    - If the models point to the same object but we mutate part of it, the objects won't be the same but the unique placeholder globalId will be the same so we can match them together and pick the newest one
		//    - If the models point to the same object already with a globalId, regardless if we mutate it or not the globalId will be kept
		if (object.globalId() == null) {
			return encodedObjects.computeIfAbsent(object, it -> gidGenerator.generateGid(it.isa(), it.stableHash()));
		} else {
			return object.globalId();
		}
	}

	private void mark(Map<String, PBXObjectReference> objectsToRef, Map<Encodeable, String> encodedObjects, Map<String, Encodeable> objectsToEncode, String globalId, Encodeable object) {
		val prevObj = objectsToEncode.get(globalId);
		if (prevObj == null) {
			objectsToEncode.put(globalId, object);
			objectsToRef.put(globalId, encodeRefInternal(objectsToRef, globalId, encodedObjects, object, objectsToEncode));
		} else if (prevObj.age() >= object.age()) {
			// nothing to do, already visited and latest version found
		} else {
			objectsToEncode.put(globalId, object); // revisit with a newer version
			objectsToRef.put(globalId, encodeRefInternal(objectsToRef, globalId, encodedObjects, object, objectsToEncode));
		}
	}

	private class MyEncodeContext implements Codeable.EncodeContext {
		private final Map<String, PBXObjectReference> objectsToRef;
		private final Map<Encodeable, String> encodedObjects;
		private final Map<String, Encodeable> objectsToEncode;
		String gid;
		Map<String, Object> map;
		Map<CodingKey, Object> codingMap = new LinkedHashMap<>();

		public MyEncodeContext(Map<String, PBXObjectReference> objectsToRef, Map<Encodeable, String> encodedObjects, Map<String, Encodeable> objectsToEncode) {
			this.objectsToRef = objectsToRef;
			this.encodedObjects = encodedObjects;
			this.objectsToEncode = objectsToEncode;
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
					public ByrefObject encodeByrefObject(Encodeable object) {
						val globalId = globalIdOf(encodedObjects, object);
						mark(objectsToRef, encodedObjects, objectsToEncode, globalId, object);
						return new DefaultByrefObject(globalId);
					}

					@Override
					public BycopyObject encodeBycopyObject(Encodeable object) {
						val context = encodeContextOf(objectsToRef, encodedObjects, objectsToEncode);
						object.encode(context);
						assert context.gid == null;
						return new DefaultBycopyObject(copyOf(context.map));
					}
				});
			});
			codingMap.clear();
		}
	}
}
