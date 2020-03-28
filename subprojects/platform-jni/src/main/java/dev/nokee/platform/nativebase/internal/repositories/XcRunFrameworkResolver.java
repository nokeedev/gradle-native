package dev.nokee.platform.nativebase.internal.repositories;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import dev.nokee.platform.nativebase.internal.LibraryElements;
import dev.nokee.platform.nativebase.internal.locators.XcRunLocator;
import org.gradle.api.attributes.Usage;
import org.gradle.nativeplatform.MachineArchitecture;
import org.gradle.nativeplatform.OperatingSystemFamily;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class XcRunFrameworkResolver implements FrameworkResolver {
	private static final Logger LOGGER = Logger.getLogger(XcRunFrameworkResolver.class.getName());
	private static final Map<String, Object> CURRENT_PLATFORM_ATTRIBUTES = ImmutableMap.<String, Object>builder()
		.put(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE.getName(), OperatingSystemFamily.MACOS)
		.put(MachineArchitecture.ARCHITECTURE_ATTRIBUTE.getName(), MachineArchitecture.X86_64)
		.build();
	private final XcRunLocator xcRunLocator;

	public XcRunFrameworkResolver(XcRunLocator xcRunLocator) {
		this.xcRunLocator = xcRunLocator;
	}

	@Nullable
	@Override
	public byte[] resolve(String path) {
		int idx = path.indexOf('/');
		String frameworkName = path.substring(0, idx);
		path = path.substring(idx + 1);

		idx = path.indexOf('/');
		String version = path.substring(0, idx);
		if (!xcRunLocator.findVersion().equals(version)) {
			// TODO: List versions
			LOGGER.info(String.format("The requested framework '%s' version '%s' doesn't match current SDK version '%s'.", frameworkName, version, xcRunLocator.findVersion()));
			return null;
		}

		File localPath = getLocalPath(frameworkName);
		if (!localPath.exists()) {
			// TODO: List frameworks?
			LOGGER.info(String.format("The requested framework '%s' wasn't found at in '%s/System/Library/Frameworks/'.", frameworkName, xcRunLocator.findPath().getPath()));
			return null;
		}

		LOGGER.fine(() -> String.format("Searching for framework '%s' for version '%s'", frameworkName, version));

		if (path.endsWith(".module")) {
			return getValue(frameworkName).getBytes(Charset.defaultCharset());
		} else if (path.endsWith(".module.sha1")) {
			return Hashing.sha1().hashString(getValue(frameworkName), Charset.defaultCharset()).asBytes();
		} else if (path.endsWith(".framework.localpath")) {
			path = path.substring(path.lastIndexOf("/") + 1);
			if (path.startsWith(frameworkName + ".framework")) {
				return localPath.getPath().getBytes(Charset.defaultCharset());
			}
			// Subframework
			path = path.substring(0, path.lastIndexOf(".localpath"));
			return new File(localPath, "Frameworks/" + path).getPath().getBytes(Charset.defaultCharset());
		}
		return null;
	}


	File getLocalPath(String frameworkName) {
		return new File(xcRunLocator.findPath(), "System/Library/Frameworks/" + frameworkName + ".framework");
	}

	String getValue(String frameworkName) {
		GradleModuleMetadata.Variant.File file = GradleModuleMetadata.Variant.File.ofLocalFile(getLocalPath(frameworkName));
		ImmutableList.Builder<GradleModuleMetadata.Variant> l = ImmutableList.<GradleModuleMetadata.Variant>builder()
			.add(compileVariant("compile", GradleModuleMetadata.Variant.File.ofLocalFile(getLocalPath(frameworkName)), emptyList()))
			.add(linkVariant("link", GradleModuleMetadata.Variant.File.ofLocalFile(getLocalPath(frameworkName)), emptyList()))
			.add(runtimeVariant("runtime", emptyList()));

		findSubFrameworks(getLocalPath(frameworkName)).forEach(it -> {
			l.add(compileVariant("compileCapable", GradleModuleMetadata.Variant.File.ofLocalFile(it), toCapabilities(frameworkName, it)));
			l.add(linkVariant("linkCapable", GradleModuleMetadata.Variant.File.ofLocalFile(it), toCapabilities(frameworkName, it)));
			l.add(runtimeVariant("runtimeCapable", toCapabilities(frameworkName, it)));
		});

		List<GradleModuleMetadata.Variant> v = l.build();
		return new Gson().toJson(GradleModuleMetadata.of(v));
	}

	List<GradleModuleMetadata.Variant.Capability> toCapabilities(String frameworkName, File subframework) {
		return singletonList(new GradleModuleMetadata.Variant.Capability(frameworkName, subframework.getName().substring(0, subframework.getName().lastIndexOf(".")), xcRunLocator.findVersion()));
	}

	List<File> findSubFrameworks(File framework) {
		if (!new File(framework, "Frameworks").exists()) {
			return emptyList();
		}
		try {
			return Files.walk(new File(framework, "Frameworks").toPath(), 1, FileVisitOption.FOLLOW_LINKS).filter(it -> Files.isDirectory(it) && it.getFileName().toString().endsWith(".framework")).map(Path::toFile).collect(Collectors.toList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	GradleModuleMetadata.Variant compileVariant(String name, GradleModuleMetadata.Variant.File file, List<GradleModuleMetadata.Variant.Capability> capabilities) {
		Map<String, Object> attributes = ImmutableMap.<String, Object>builder()
			.put("org.gradle.usage", Usage.C_PLUS_PLUS_API)
			.put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE.getName(), LibraryElements.FRAMEWORK_BUNDLE)
			.build();
		return new GradleModuleMetadata.Variant(name, attributes, singletonList(file), capabilities);
	}

	GradleModuleMetadata.Variant linkVariant(String name, GradleModuleMetadata.Variant.File file, List<GradleModuleMetadata.Variant.Capability> capabilities) {
		Map<String, Object> attributes = ImmutableMap.<String, Object>builder()
			.put("org.gradle.usage", Usage.NATIVE_LINK)
			.put(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE.getName(), LibraryElements.FRAMEWORK_BUNDLE)
			.putAll(CURRENT_PLATFORM_ATTRIBUTES)
			.build();
		return new GradleModuleMetadata.Variant(name, attributes, singletonList(file), capabilities);
	}

	GradleModuleMetadata.Variant runtimeVariant(String name, List<GradleModuleMetadata.Variant.Capability> capabilities) {
		Map<String, Object> attributes = ImmutableMap.<String, Object>builder()
			.put("org.gradle.usage", Usage.NATIVE_RUNTIME)
			.putAll(CURRENT_PLATFORM_ATTRIBUTES)
			.build();
		return new GradleModuleMetadata.Variant(name, attributes, emptyList(), capabilities);
	}
}
