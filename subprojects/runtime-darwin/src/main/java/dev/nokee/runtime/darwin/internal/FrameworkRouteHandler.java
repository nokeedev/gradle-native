package dev.nokee.runtime.darwin.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.nokee.core.exec.*;
import dev.nokee.runtime.base.internal.repositories.AbstractRouteHandler;
import dev.nokee.runtime.base.internal.repositories.GradleModuleMetadata;
import dev.nokee.runtime.base.internal.tools.CommandLineToolDescriptor;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import dev.nokee.runtime.darwin.internal.parsers.XcodebuildParsers;
import dev.nokee.runtime.nativebase.internal.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.nativeplatform.MachineArchitecture;
import org.gradle.nativeplatform.OperatingSystemFamily;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class FrameworkRouteHandler extends AbstractRouteHandler {
	private static final Logger LOGGER = Logger.getLogger(FrameworkRouteHandler.class.getName());
	public static final String CONTEXT_PATH = "/dev/nokee/framework";
	private static final Map<String, Object> CURRENT_PLATFORM_ATTRIBUTES = ImmutableMap.<String, Object>builder()
		.put(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE.getName(), OperatingSystemFamily.MACOS)
		.put(MachineArchitecture.ARCHITECTURE_ATTRIBUTE.getName(), MachineArchitecture.X86_64)
		.build();
	private final ToolRepository toolRepository;
	private final CommandLineToolExecutionEngine<CachingProcessBuilderEngine.Handle> engine = new CachingProcessBuilderEngine(LoggingEngine.wrap(new ProcessBuilderEngine()));

	@Inject
	public FrameworkRouteHandler(ToolRepository toolRepository) {
		this.toolRepository = toolRepository;
	}

	private String findSdkVersion(XcodeSdk sdk) {
		return CommandLineTool.of(toolRepository.findAll("xcrun").iterator().next().getPath()).withArguments("-sdk", sdk.getIdentifier(), "--show-sdk-version").execute(engine).getResult().getStandardOutput().getAsString().trim();
	}

	private File findSdkPath(XcodeSdk sdk) {
		return CommandLineTool.of(toolRepository.findAll("xcrun").iterator().next().getPath()).withArguments("-sdk", sdk.getIdentifier(), "--show-sdk-path").execute(engine).getResult().getStandardOutput().parse(it -> new File(it.trim()));
	}

	private XcodeSdk findMacOsSdks() {
		CommandLineToolDescriptor xcodebuilt = toolRepository.findAll("xcodebuild").iterator().next();
		return CommandLineTool.of(xcodebuilt.getPath())
			.withArguments("-showsdks")
			.execute(engine)
			.getResult()
			.getStandardOutput()
			.parse(XcodebuildParsers.showSdkParser())
			.stream().filter(it -> it.getIdentifier().startsWith("macosx")).findFirst()
			.orElseThrow(() -> new RuntimeException(String.format("MacOS SDK not found using '%s' version %s", xcodebuilt.getPath().getAbsolutePath(), xcodebuilt.getVersion())));
	}

	@Override
	public String getContextPath() {
		return CONTEXT_PATH;
	}

	@Override
	public boolean isKnownModule(String moduleName) {
		if (getLocalPath(moduleName).exists()) {
			return true;
		}
		LOGGER.info(String.format("The requested framework '%s' wasn't found at in '%s/System/Library/Frameworks/'.", moduleName, findSdkPath(findMacOsSdks()).getPath()));
		return false;
	}

	@Override
	public boolean isKnownVersion(String moduleName, String version) {
		XcodeSdk sdk = findMacOsSdks();
		if (!findSdkVersion(sdk).equals(version)) {
			LOGGER.info(String.format("The requested framework '%s' version '%s' doesn't match current SDK version '%s'.", moduleName, version, findSdkVersion(sdk)));
			return false;
		}
		if (!getLocalPath(moduleName).exists()) {
			// TODO: List frameworks?
			LOGGER.info(String.format("The requested framework '%s' wasn't found at in '%s/System/Library/Frameworks/'.", moduleName, findSdkPath(sdk).getPath()));
			return false;
		}
		return true;
	}

	@Override
	public List<String> findVersions(String moduleName) {
		XcodeSdk sdk = findMacOsSdks();
		return singletonList(findSdkVersion(sdk));
	}

	@Override
	public GradleModuleMetadata getResourceMetadata(String moduleName, String version) {
		return getValue(moduleName, version);
	}

	@Override
	public String handle(String moduleName, String version, String target) {
		if (target.endsWith(".framework.localpath")) {
			File localPath = getLocalPath(moduleName);
			target = target.substring(target.lastIndexOf("/") + 1);
			if (target.startsWith(moduleName + ".framework")) {
				return localPath.getPath();
			}
			// Subframework
			target = target.substring(0, target.lastIndexOf(".localpath"));
			return new File(localPath, "Frameworks/" + target).getPath();
		}
		return null;
	}

	File getLocalPath(String frameworkName) {
		return new File(findSdkPath(findMacOsSdks()), "System/Library/Frameworks/" + frameworkName + ".framework");
	}

	GradleModuleMetadata getValue(String frameworkName, String version) {
		GradleModuleMetadata.Variant.File file = GradleModuleMetadata.Variant.File.ofLocalFile(getLocalPath(frameworkName));
		ImmutableList.Builder<GradleModuleMetadata.Variant> l = ImmutableList.<GradleModuleMetadata.Variant>builder()
			.add(compileVariant("compile", file, emptyList()))
			.add(linkVariant("link", file, emptyList()))
			.add(runtimeVariant("runtime", emptyList()));

		findSubFrameworks(getLocalPath(frameworkName)).forEach(it -> {
			l.add(compileVariant("compileCapable", GradleModuleMetadata.Variant.File.ofLocalFile(it), toCapabilities(frameworkName, it)));
			l.add(linkVariant("linkCapable", GradleModuleMetadata.Variant.File.ofLocalFile(it), toCapabilities(frameworkName, it)));
			l.add(runtimeVariant("runtimeCapable", toCapabilities(frameworkName, it)));
		});

		List<GradleModuleMetadata.Variant> v = l.build();
		return GradleModuleMetadata.of(GradleModuleMetadata.Component.of("dev.nokee.framework", frameworkName, version), v);
	}

	List<GradleModuleMetadata.Variant.Capability> toCapabilities(String frameworkName, File subframework) {
		return singletonList(new GradleModuleMetadata.Variant.Capability(frameworkName, subframework.getName().substring(0, subframework.getName().lastIndexOf(".")), findSdkVersion(findMacOsSdks())));
	}

	List<File> findSubFrameworks(File framework) {
		if (!new File(framework, "Frameworks").exists()) {
			return emptyList();
		}
		try {
			return Files.walk(new File(framework, "Frameworks").toPath(), 1, FileVisitOption.FOLLOW_LINKS).filter(it -> Files.isDirectory(it) && it.getFileName().toString().endsWith(".framework")).map(java.nio.file.Path::toFile).collect(Collectors.toList());
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
