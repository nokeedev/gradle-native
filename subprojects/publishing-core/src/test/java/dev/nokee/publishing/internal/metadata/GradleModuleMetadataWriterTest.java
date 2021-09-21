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

import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Attribute.ofAttribute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GradleModuleMetadataWriterTest {
	@Test
	void serializeAttributesAsJsonMap() throws IOException {
		val output = json(builder -> builder.localVariant(it -> it.name("foo").attribute(ofAttribute("org.gradle.usage", "usage"))));
		assertThat(output, equalTo(content(
			"{",
			"  \"formatVersion\": \"1.1\",",
			"  \"variants\": [",
			"    {",
			"      \"name\": \"foo\",",
			"      \"attributes\": {",
			"        \"org.gradle.usage\": \"usage\"",
			"      }",
			"    }",
			"  ]",
			"}"
		)));
	}

	@Test
	void canSerializeDependencies() throws IOException {
		val output = json(variant(builder -> builder.dependency(it -> it.name("dep1"))));
		assertThat(output, equalTo(content(
			"{",
			"  \"formatVersion\": \"1.1\",",
			"  \"variants\": [",
			"    {",
			"      \"name\": \"foo\",",
			"      \"attributes\": {",
			"        \"org.gradle.usage\": \"usage\"",
			"      },",
			"      \"dependencies\": [",
			"        {",
			"          \"name\": \"dep1\"",
			"        }",
			"      ]",
			"    }",
			"  ]",
			"}"
		)));
	}

	private static Consumer<GradleModuleMetadata.Builder> variant(Consumer<? super GradleModuleMetadata.LocalVariant.Builder> action) {
		return builder -> builder.localVariant(
			((Consumer<GradleModuleMetadata.LocalVariant.Builder>)it -> it.name("foo").attribute(ofAttribute("org.gradle.usage", "usage")))
				.andThen(action));
	}

	private static String json(Consumer<? super GradleModuleMetadata.Builder> action) throws IOException {
		val output = new ByteArrayOutputStream();
		try (val writer = new GradleModuleMetadataWriter(new OutputStreamWriter(output))) {
			val builder = GradleModuleMetadata.builder().formatVersion("1.1");
			action.accept(builder);
			writer.write(builder.build());
		}
		return output.toString(StandardCharsets.UTF_8.name());
	}

	private static String content(String... lines) {
		return String.join("\n", lines);
	}
}
