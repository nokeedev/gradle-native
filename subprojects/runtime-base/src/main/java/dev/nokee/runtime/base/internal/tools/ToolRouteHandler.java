package dev.nokee.runtime.base.internal.tools;

import com.google.common.hash.Hashing;
import com.google.gson.*;
import dev.nokee.publishing.internal.metadata.GradleModuleMetadata;
import dev.nokee.runtime.base.internal.repositories.AbstractRouteHandler;
import lombok.val;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Attribute.ofAttribute;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Capability.ofCapability;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Component.ofComponent;
import static java.util.Collections.singletonList;

public class ToolRouteHandler extends AbstractRouteHandler {
	private static final Logger LOGGER = Logger.getLogger(ToolRouteHandler.class.getName());
	public static final String CONTEXT_PATH = "/dev/nokee/tool";
	private final ToolRepository toolRepository;

	@Inject
	public ToolRouteHandler(ToolRepository toolRepository) {
		this.toolRepository = toolRepository;
	}

	@Override
	public String getContextPath() {
		return CONTEXT_PATH;
	}

	@Override
	public boolean isKnownModule(String moduleName) {
		return toolRepository.getKnownTools().contains(moduleName);
	}

	@Override
	public boolean isKnownVersion(String moduleName, String version) {
		return toolRepository.findAll(moduleName).stream().anyMatch(it -> it.getPath().getName().equals(moduleName) && it.getVersion().equals(version));
	}

	@Override
	public List<String> findVersions(String moduleName) {
		return toolRepository.findAll(moduleName).stream().filter(it -> it.getPath().getName().equals(moduleName)).map(CommandLineToolDescriptor::getVersion).collect(Collectors.toList());
	}

	@Override
	public GradleModuleMetadata getResourceMetadata(String moduleName, String version) {
		CommandLineToolDescriptor descriptor = toolRepository.findAll(moduleName).stream().filter(it -> it.getPath().getName().equals(moduleName) && it.getVersion().equals(version)).findFirst().get();

		String content = serialize(descriptor);
		val builder = GradleModuleMetadata.builder();
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

		return builder.build();
	}

	@Override
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
}
