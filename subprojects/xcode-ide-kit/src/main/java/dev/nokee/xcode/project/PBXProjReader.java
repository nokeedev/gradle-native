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

import dev.nokee.xcode.JavaPropertyListReader;
import dev.nokee.xcode.PropertyListReader;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;

public final class PBXProjReader implements Closeable {
	private final JavaPropertyListReader delegate;

	public PBXProjReader(PropertyListReader delegate) {
		this.delegate = new JavaPropertyListReader(delegate);
	}

	public PBXProj read() {
		PBXProj.Builder builder = PBXProj.builder();
		delegate.readDocument(doc -> {
			doc.readDict(new BiConsumer<String, JavaPropertyListReader.ValueReader>() {
				@Override
				public void accept(String key, JavaPropertyListReader.ValueReader dict) {
					switch (key) {
						case "objects":
							builder.objects(readObjects(dict));
							break;
						case "rootObject":
							builder.rootObject(dict.readString());
							break;
						default:
							dict.skipObject();
					}
				}

				private PBXObjects readObjects(JavaPropertyListReader.ValueReader reader) {
					final PBXObjects.Builder builder = PBXObjects.builder();
					reader.readDict(readObjectReference(builder));
					return builder.build();
				}
			});
		});

		return builder.build();
	}

	private BiConsumer<String, JavaPropertyListReader.ValueReader> readObjectReference(PBXObjects.Builder builder) {
		return (gid, reader) -> {
			final Map<String, Object> object = reader.readDict();
			builder.add(PBXObjectReference.of(gid, it -> object.forEach(it::putField)));
		};
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
//
//	public PBXProj read() {
//		assertEquals(Event.DOCUMENT_START, delegate.next());
//		assertEquals(DICTIONARY_START, delegate.next());
//
//		String rootObject = null;
//		final PBXObjects.Builder builder = PBXObjects.builder();
//		for (Event e = delegate.next(); e != DICTIONARY_END; e = delegate.next()) {
//			assertEquals(DICTIONARY_KEY, e);
//			switch (delegate.readDictionaryKey()) {
//				case "objects":
//					assertEquals(DICTIONARY_START, delegate.next());
//					for (Event ee = delegate.next(); ee != DICTIONARY_END; ee = delegate.next()) {
//						builder.add(readObjectReference());
//					}
//					break;
//				case "rootObject":
//					delegate.next();
//					rootObject = delegate.readString();
//					break;
//				default:
//					skipValue();
//			}
//		}
//		assertEquals(Event.DOCUMENT_END, delegate.next());
//
//		return PBXProj.builder().objects(builder.build()).rootObject(rootObject).build();
//	}
//
//	private PBXObjectReference readObjectReference() {
////		assertEquals(DICTIONARY_KEY, delegate.next());
//		final String gid = delegate.readDictionaryKey();
//		assertEquals(DICTIONARY_START, delegate.next());
//		final Map<String, Object> object = readDictionary();
//		return PBXObjectReference.of(gid, builder -> object.forEach(builder::putField));
//	}
//
//	private Object readValue() {
//		val tag = delegate.next();
//		return readValueUsingTag(tag);
//	}
//
//	private Object readValueUsingTag(Event tag) {
//		switch (tag) {
//			case DICTIONARY_START:
//				return readDictionary();
//			case ARRAY_START:
//				final ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();
//				for (Event e = delegate.next(); e != ARRAY_END; e = delegate.next()) {
//					listBuilder.add(readValueUsingTag(e));
//				}
//				return listBuilder.build();
//			case BOOLEAN:
//				return delegate.readBoolean();
//			case STRING:
//				return delegate.readString();
//			case INTEGER:
//				return delegate.readInteger();
//			default:
//				throw new UnsupportedOperationException(tag.name());
//		}
//	}
//
//	private void skipValue() {
//		readValue();
//	}
//
//	private Map<String, Object> readDictionary() {
//		final ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();
//		for (Event e = delegate.next(); e != DICTIONARY_END; e = delegate.next()) {
//			assertEquals(DICTIONARY_KEY, e);
//			val key = delegate.readDictionaryKey();
//			val value = readValue();
//			mapBuilder.put(key, value);
//		}
//		return mapBuilder.build();
//	}
//
//	private static <T> void assertEquals(T expected, T actual) {
//		if (!expected.equals(actual)) {
//			throw new IllegalStateException("expecting <" + expected + "> but actual is <" + actual + ">");
//		}
//	}
//
//	private static <T> void assertContains(T actual, Object... values) {
//		for (Object t : values) {
//			if (actual.equals(t)) {
//				return;
//			}
//		}
//		throw new IllegalStateException("expecting <" + Arrays.stream(values).map(Object::toString).collect(Collectors.joining(",")) + "> but actual is <" + actual + ">");
//	}
}
