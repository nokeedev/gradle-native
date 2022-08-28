/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.runtime.base.internal.tools;

import com.google.common.hash.Hashing;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import dev.nokee.gradle.AdhocComponentLister;
import dev.nokee.gradle.AdhocComponentListerDetails;
import dev.nokee.gradle.AdhocComponentSupplier;
import dev.nokee.gradle.AdhocComponentSupplierDetails;
import dev.nokee.publishing.internal.metadata.GradleModuleMetadata;
import lombok.val;
import org.gradle.api.Action;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Attribute.ofAttribute;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Capability.ofCapability;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Component.ofComponent;
import static java.util.Collections.singletonList;

public class ToolHandler implements AdhocComponentLister, AdhocComponentSupplier {
	private static final Logger LOGGER = Logger.getLogger(ToolHandler.class.getCanonicalName());
	public static final String CONTEXT_PATH = "/dev/nokee/tool";
	private final ToolRepository toolRepository;

	@Inject
	public ToolHandler(ToolRepository toolRepository) {
		this.toolRepository = toolRepository;
	}

	public boolean isKnownModule(String moduleName) {
		return toolRepository.getKnownTools().contains(moduleName);
	}

	public boolean isKnownVersion(String moduleName, String version) {
		return toolRepository.findAll(moduleName).stream().anyMatch(it -> it.getPath().getName().equals(moduleName) && it.getVersion().equals(version));
	}

	public List<String> findVersions(String moduleName) {
		return toolRepository.findAll(moduleName).stream().filter(it -> it.getPath().getName().equals(moduleName)).map(CommandLineToolDescriptor::getVersion).collect(Collectors.toList());
	}

	@SuppressWarnings("deprecation")
	public Action<GradleModuleMetadata.Builder> getResourceMetadata(String moduleName, String version) {
		return builder -> {
			CommandLineToolDescriptor descriptor = toolRepository.findAll(moduleName).stream().filter(it -> it.getPath().getName().equals(moduleName) && it.getVersion().equals(version)).findFirst().get();

			String content = serialize(descriptor);
			builder.formatVersion("1.1");
			builder.component(ofComponent("dev.nokee.tool", moduleName, version, singletonList(ofAttribute("org.gradle.status", "release"))));
			builder.localVariant(it -> {
				it.name(moduleName);
				it.file(fileBuilder -> {
					fileBuilder
						.name(moduleName + ".tooldescriptor")
						.url(moduleName + ".tooldescriptor")
						.size(content.getBytes().length)
						.sha1(Hashing.sha1().hashString(content, Charset.defaultCharset()).toString())
						.md5(Hashing.md5().hashString(content, Charset.defaultCharset()).toString());
				});
				it.capability(ofCapability("dev.nokee.tool", moduleName, version));
			});
		};
	}

	public String handle(String moduleName, String version, String target) {
		if (target.endsWith(".tooldescriptor")) {
			CommandLineToolDescriptor descriptor = toolRepository.findAll(moduleName).stream().filter(it -> it.getPath().getName().equals(moduleName) && it.getVersion().equals(version)).findFirst().get();
			return serialize(descriptor);
		}
		return null;
	}

	private String serialize(CommandLineToolDescriptor descriptor) {
		return new GsonBuilder().registerTypeAdapter(File.class, new JsonSerializer<File>() {
			@Override
			public JsonElement serialize(File src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.getAbsolutePath());
			}
		}).create().toJson(descriptor);
	}

	@Override
	public void execute(AdhocComponentListerDetails details) {
		if (isKnownModule(details.getModuleIdentifier().getName())) {
			details.listed(findVersions(details.getModuleIdentifier().getName()));
		}
	}

	@Override
	public void execute(AdhocComponentSupplierDetails details) {
		if (isKnownVersion(details.getId().getModule(), details.getId().getVersion())) {
			details.metadata(getResourceMetadata(details.getId().getModule(), details.getId().getVersion()));

			details.file(details.getId().getModule() + ".tooldescriptor", outStream -> {
				val moduleName = details.getId().getModule();
				val version = details.getId().getVersion();
				CommandLineToolDescriptor descriptor = toolRepository.findAll(moduleName).stream().filter(it -> it.getPath().getName().equals(moduleName) && it.getVersion().equals(version)).findFirst().get();
				String content = serialize(descriptor);
				try {
					outStream.write(content.getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		} else {
			LOGGER.info(String.format("The requested module '%s' version '%s' doesn't match current available versions '%s'.", details.getId().getModule(), details.getId().getVersion(), String.join(", ", findVersions(details.getId().getModule()))));
		}
	}
}
