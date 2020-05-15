package dev.nokee.runtime.base.internal.tools;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.gson.*;
import dev.nokee.runtime.base.internal.repositories.AbstractRouteHandler;
import dev.nokee.runtime.base.internal.repositories.GradleModuleMetadata;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
		return toolRepository.findAll(moduleName).stream().anyMatch(it -> it.getPath().getName().equals(moduleName) && it.getVersion().toString().equals(version));
	}

	@Override
	public List<String> findVersions(String moduleName) {
		return toolRepository.findAll(moduleName).stream().filter(it -> it.getPath().getName().equals(moduleName)).map(it -> it.getVersion().toString()).collect(Collectors.toList());
	}

	@Override
	public GradleModuleMetadata getResourceMetadata(String moduleName, String version) {
		CommandLineToolDescriptor descriptor = toolRepository.findAll(moduleName).stream().filter(it -> it.getPath().getName().equals(moduleName) && it.getVersion().toString().equals(version)).findFirst().get();

		String content = serialize(descriptor);
		GradleModuleMetadata.Variant.File file = new GradleModuleMetadata.Variant.File(moduleName + ".tooldescriptor", moduleName + ".tooldescriptor", String.valueOf(content.getBytes().length), Hashing.sha1().hashString(content, Charset.defaultCharset()).toString(), Hashing.md5().hashString(content, Charset.defaultCharset()).toString());

		List<GradleModuleMetadata.Variant.Capability> capabilities = singletonList(new GradleModuleMetadata.Variant.Capability("dev.nokee.tool", moduleName, version));

		Map<String, Object> attributes = ImmutableMap.<String, Object>builder().build();
		return GradleModuleMetadata.of(GradleModuleMetadata.Component.of("dev.nokee.tool", moduleName, version), ImmutableList.of(new GradleModuleMetadata.Variant(moduleName, attributes, singletonList(file), capabilities)));
	}

	@Override
	public String handle(String moduleName, String version, String target) {
		if (target.endsWith(".tooldescriptor")) {
			CommandLineToolDescriptor descriptor = toolRepository.findAll(moduleName).stream().filter(it -> it.getPath().getName().equals(moduleName) && it.getVersion().toString().equals(version)).findFirst().get();
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
