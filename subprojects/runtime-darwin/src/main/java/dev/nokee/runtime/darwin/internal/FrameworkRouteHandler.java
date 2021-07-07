package dev.nokee.runtime.darwin.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import dev.nokee.core.exec.*;
import dev.nokee.publishing.internal.metadata.GradleModuleMetadata;
import dev.nokee.runtime.base.internal.repositories.AbstractRouteHandler;
import dev.nokee.runtime.base.internal.tools.CommandLineToolDescriptor;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import dev.nokee.runtime.darwin.internal.parsers.TextAPI;
import dev.nokee.runtime.darwin.internal.parsers.TextAPIReader;
import dev.nokee.runtime.darwin.internal.parsers.XcodebuildParsers;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import lombok.val;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Attribute.ofAttribute;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Capability.ofCapability;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Component.ofComponent;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class FrameworkRouteHandler extends AbstractRouteHandler {
	private static final Logger LOGGER = Logger.getLogger(FrameworkRouteHandler.class.getName());
	public static final String CONTEXT_PATH = "/dev/nokee/framework";
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
		return findMacOsSdks(xcodebuilt, engine);
	}

	public static XcodeSdk findMacOsSdks(CommandLineToolDescriptor xcodebuild, CommandLineToolExecutionEngine<CachingProcessBuilderEngine.Handle> engine) {
		return CommandLineTool.of(xcodebuild.getPath())
			.withArguments("-showsdks")
			.execute(engine)
			.getResult()
			.getStandardOutput()
			.parse(XcodebuildParsers.showSdkParser())
			.stream().filter(it -> it.getIdentifier().startsWith("macosx")).findFirst()
			.orElseThrow(() -> new RuntimeException(String.format("MacOS SDK not found using '%s' version %s", xcodebuild.getPath().getAbsolutePath(), xcodebuild.getVersion())));
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
		val frameworkPath = getLocalPath(frameworkName).toPath();
		val builder = GradleModuleMetadata.builder();
		builder.formatVersion("1.1");
		builder.component(ofComponent("dev.nokee.framework", frameworkName, version, singletonList(ofAttribute("org.gradle.status", "release"))));

		forEachSupportedArchitectures(frameworkPath, supportedArchitecture -> {
			builder.localVariant(framework("default" + supportedArchitecture, frameworkPath, ImmutableList.of(), platformAttributes(supportedArchitecture)));
			builder.localVariant(runtimeEntry("default" + supportedArchitecture, ImmutableList.of(), platformAttributes(supportedArchitecture)));
		});

		findSubFrameworks(frameworkPath).forEach(it -> {
			forEachSupportedArchitectures(it, supportedArchitecture -> {
				builder.localVariant(framework(frameworkName(it) + supportedArchitecture, it, toCapabilities(frameworkName, it), platformAttributes(supportedArchitecture)));
				builder.localVariant(runtimeEntry(frameworkName(it) + supportedArchitecture, toCapabilities(frameworkName, it), platformAttributes(supportedArchitecture)));
			});
		});

		return builder.build();
	}

	private static void forEachSupportedArchitectures(Path frameworkPath, Consumer<? super String> action) {
		List<String> supportedArchitectures = ImmutableList.of(MachineArchitecture.X86_64);
		val tbdPath = frameworkPath.resolve(frameworkName(frameworkPath) + ".tbd");
		if (Files.exists(tbdPath)) {
			try {
				val tbd = TextAPI.read(tbdPath);
				supportedArchitectures = tbd.getTargets().stream().filter(it -> "macos".equals(it.getOperatingSystem())).map(TextAPI.Target::getArchitecture).collect(ImmutableList.toImmutableList());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		supportedArchitectures.forEach(action);
	}

	private static Map<String, Object> platformAttributes(String architecture) {
		val canonicalArchitecture = MachineArchitecture.forName(architecture).getCanonicalName();
		return ImmutableMap.<String, Object>builder()
			.put(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE.getName(), OperatingSystemFamily.MACOS)
			.put(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE.getName(), org.gradle.nativeplatform.OperatingSystemFamily.MACOS)
			.put(MachineArchitecture.ARCHITECTURE_ATTRIBUTE.getName(), canonicalArchitecture)
			.put(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE.getName(), canonicalArchitecture)
			.build();
	}

	private static String frameworkName(Path frameworkPath) {
		return removeExtension(frameworkPath.getFileName().toString());
	}

	private static final String FRAMEWORK_USAGE = Usage.C_PLUS_PLUS_API + "+" + Usage.NATIVE_LINK;

	private static Consumer<GradleModuleMetadata.LocalVariant.Builder> framework(String name, Path frameworkPath, List<GradleModuleMetadata.Capability> capabilities, Map<String, Object> platformAttributes) {
		return builder -> {
			builder.name(name);
			builder.file(it -> it.name(frameworkPath.getFileName().toString() + ".localpath")
				.url(frameworkPath.getFileName().toString() + ".localpath")
				.size(frameworkPath.toString().getBytes().length)
				.sha1(Hashing.sha1().hashString(frameworkPath.toString(), Charset.defaultCharset()).toString())
				.md5(Hashing.md5().hashString(frameworkPath.toString(), Charset.defaultCharset()).toString()));
			builder.attribute(ofAttribute("org.gradle.usage", FRAMEWORK_USAGE))
				.attribute(ofAttribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE.getName(), DarwinLibraryElements.FRAMEWORK_BUNDLE));
			platformAttributes.entrySet().forEach(it -> builder.attribute(ofAttribute(it.getKey(), it.getValue())));
			capabilities.forEach(builder::capability);
		};
	}

	private static Consumer<GradleModuleMetadata.LocalVariant.Builder> runtimeEntry(String name, List<GradleModuleMetadata.Capability> capabilities, Map<String, Object> platformAttributes) {
		return builder -> {
			builder.name(name + "Runtime");
			builder.attribute(ofAttribute("org.gradle.usage", Usage.NATIVE_RUNTIME))
				.attribute(ofAttribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE.getName(), DarwinLibraryElements.FRAMEWORK_BUNDLE));
			platformAttributes.entrySet().forEach(it -> builder.attribute(ofAttribute(it.getKey(), it.getValue())));
			capabilities.forEach(builder::capability);
		};
	}

	List<GradleModuleMetadata.Capability> toCapabilities(String frameworkName, Path subframework) {
		val subframeworkName = frameworkName(subframework);
		return singletonList(ofCapability(frameworkName, subframeworkName.substring(0, subframeworkName.lastIndexOf(".")), findSdkVersion(findMacOsSdks())));
	}

	List<Path> findSubFrameworks(Path frameworkPath) {
		if (!Files.exists(frameworkPath.resolve("Frameworks"))) {
			return emptyList();
		}
		try {
			return Files.walk(frameworkPath.resolve("Frameworks"), 1, FileVisitOption.FOLLOW_LINKS).filter(it -> Files.isDirectory(it) && it.getFileName().toString().endsWith(".framework")).collect(Collectors.toList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
