/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.publishing.internal.metadata;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import lombok.val;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class GradleModuleMetadataWriter implements Closeable {
	private static final Type CAPABILITIES_TYPE = new TypeToken<List<GradleModuleMetadata.Capability>>() {}.getType();
	private static final Type EXCLUDES_TYPE = new TypeToken<Set<GradleModuleMetadata.Exclude>>() {}.getType();
	private static final Type ATTRIBUTES_TYPE = new TypeToken<List<GradleModuleMetadata.Attribute>>() {}.getType();
	private static final Type DEPENDENCIES_TYPE = new TypeToken<List<GradleModuleMetadata.Dependency>>() {}.getType();
	private static final Type DEPENDENCY_CONSTRAINTS_TYPE = new TypeToken<List<GradleModuleMetadata.DependencyConstraint>>() {}.getType();
	private static final Type FILES_TYPE = new TypeToken<List<GradleModuleMetadata.File>>() {}.getType();
	private static final Type STRINGS_TYPE = new TypeToken<List<String>>() {}.getType();

	private final Writer writer;

	public GradleModuleMetadataWriter(Writer writer) {
		this.writer = writer;
	}

	public void write(GradleModuleMetadata metadata) throws IOException {
		val gson = new GsonBuilder().setPrettyPrinting()
			.registerTypeAdapter(ATTRIBUTES_TYPE, AttributesSerializer.INSTANCE)
			.registerTypeAdapter(DEPENDENCIES_TYPE, CollectionSerializer.INSTANCE)
			.registerTypeAdapter(EXCLUDES_TYPE, CollectionSerializer.INSTANCE)
			.registerTypeAdapter(CAPABILITIES_TYPE, CollectionSerializer.INSTANCE)
			.registerTypeAdapter(DEPENDENCY_CONSTRAINTS_TYPE, CollectionSerializer.INSTANCE)
			.registerTypeAdapter(FILES_TYPE, CollectionSerializer.INSTANCE)
			.registerTypeAdapter(STRINGS_TYPE, CollectionSerializer.INSTANCE)
			.create();
		writer.write(gson.toJson(metadata));
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	private enum CollectionSerializer implements JsonSerializer<Collection<?>> {
		INSTANCE;

		@Override
		public JsonElement serialize(Collection<?> src, Type typeOfSrc, JsonSerializationContext context) {
			if (src.isEmpty()) {
				return null;
			}
			return context.serialize(src);
		}
	}

	private enum AttributesSerializer implements JsonSerializer<List<GradleModuleMetadata.Attribute>> {
		INSTANCE;

		@Override
		public JsonElement serialize(List<GradleModuleMetadata.Attribute> src, Type typeOfSrc, JsonSerializationContext context) {
			if (src.isEmpty()) {
				return null;
			}
			val map = new JsonObject();
			src.forEach(a -> map.addProperty(a.getName(), a.getValue().toString()));
			return map;
		}
	}
}
