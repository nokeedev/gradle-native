package dev.nokee.platform.nativebase.internal.repositories;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class ToolHandler extends AbstractHandler {
	private static final Logger LOGGER = Logger.getLogger(ToolHandler.class.getName());
	public static final String CONTEXT_PATH = "/dev/nokee/tool/";
	public final XcodeLocator locator = new XcodeLocator();
	private static final Set<String> KNOWN_TOOLS = ImmutableSet.of("ibtool", "actool", "codesign");

	public ToolHandler() {
		super(CONTEXT_PATH);
	}

	@Override
	public boolean isKnownModule(String moduleName) {
		return KNOWN_TOOLS.contains(moduleName);
	}

	@Override
	public boolean isKnownVersion(String moduleName, String version) {
		return locator.findAll().stream().anyMatch(it -> it.getPath().getName().equals(moduleName) && it.getVersion().toString().equals(version));
	}

	@Override
	public List<String> findVersions(String moduleName) {
		return locator.findAll().stream().filter(it -> it.getPath().getName().equals(moduleName)).map(it -> it.getVersion().toString()).collect(Collectors.toList());
	}

	@Override
	public GradleModuleMetadata getGradleModuleMetadata(String moduleName, String version) {
		ToolDescriptor descriptor = locator.findAll().stream().filter(it -> it.getPath().getName().equals(moduleName) && it.getVersion().toString().equals(version)).findFirst().get();

		String content = serialize(descriptor);
		GradleModuleMetadata.Variant.File file = new GradleModuleMetadata.Variant.File(moduleName + ".tooldescriptor", moduleName + ".tooldescriptor", String.valueOf(content.getBytes().length), Hashing.sha1().hashString(content, Charset.defaultCharset()).toString(), Hashing.md5().hashString(content, Charset.defaultCharset()).toString());

		List<GradleModuleMetadata.Variant.Capability> capabilities = singletonList(new GradleModuleMetadata.Variant.Capability("dev.nokee.tool", moduleName, version));

		Map<String, Object> attributes = ImmutableMap.<String, Object>builder().build();
		return GradleModuleMetadata.of(GradleModuleMetadata.Component.of("dev.nokee.tool", moduleName, version), ImmutableList.of(new GradleModuleMetadata.Variant(moduleName, attributes, singletonList(file), capabilities)));
	}

	@Override
	public String handle(String moduleName, String version, String target) {
		if (target.endsWith(".tooldescriptor")) {
			ToolDescriptor descriptor = locator.findAll().stream().filter(it -> it.getPath().getName().equals(moduleName) && it.getVersion().toString().equals(version)).findFirst().get();
			return serialize(descriptor);
		}
		return null;
	}

	private String serialize(ToolDescriptor descriptor) {
		return new GsonBuilder().registerTypeAdapter(File.class, new JsonSerializer<File>() {
			@Override
			public JsonElement serialize(File src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.getAbsolutePath());
			}
		}).create().toJson(descriptor);
	}
}
