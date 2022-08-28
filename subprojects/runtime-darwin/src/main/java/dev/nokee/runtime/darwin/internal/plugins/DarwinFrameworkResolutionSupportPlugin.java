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
package dev.nokee.runtime.darwin.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import dev.nokee.core.exec.CachingProcessBuilderEngine;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolExecutionEngine;
import dev.nokee.core.exec.LoggingEngine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.gradle.AdhocArtifactRepository;
import dev.nokee.gradle.AdhocArtifactRepositoryFactory;
import dev.nokee.gradle.AdhocComponentLister;
import dev.nokee.gradle.AdhocComponentListerDetails;
import dev.nokee.gradle.AdhocComponentSupplier;
import dev.nokee.gradle.AdhocComponentSupplierDetails;
import dev.nokee.publishing.internal.metadata.GradleModuleMetadata;
import dev.nokee.runtime.base.internal.plugins.FakeMavenRepositoryPlugin;
import dev.nokee.runtime.base.internal.repositories.NokeeServerService;
import dev.nokee.runtime.base.internal.tools.CommandLineToolDescriptor;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import dev.nokee.runtime.darwin.internal.DarwinLibraryElements;
import dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin;
import dev.nokee.runtime.darwin.internal.XcodeSdk;
import dev.nokee.runtime.darwin.internal.locators.XcodebuildLocator;
import dev.nokee.runtime.darwin.internal.locators.XcrunLocator;
import dev.nokee.runtime.darwin.internal.parsers.TextAPI;
import dev.nokee.runtime.darwin.internal.parsers.XcodebuildParsers;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Attribute.ofAttribute;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Capability.ofCapability;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Component.ofComponent;
import static dev.nokee.runtime.base.internal.plugins.FakeMavenRepositoryPlugin.NOKEE_SERVER_SERVICE_NAME;
import static dev.nokee.runtime.darwin.internal.DarwinArtifactTypes.FRAMEWORK_TYPE;
import static dev.nokee.runtime.darwin.internal.DarwinLibraryElements.FRAMEWORK_BUNDLE;
import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.DESERIALIZED;
import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.SERIALIZED;
import static dev.nokee.utils.ConfigurationUtils.ARTIFACT_TYPE_ATTRIBUTE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class DarwinFrameworkResolutionSupportPlugin implements Plugin<Project> {
	private static final Logger LOGGER = Logging.getLogger(DarwinFrameworkResolutionSupportPlugin.class);
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public DarwinFrameworkResolutionSupportPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(DarwinRuntimePlugin.class);
		project.getPluginManager().apply(CompatibilityRules.class);

		if (SystemUtils.IS_OS_MAC) {
			project.getRepositories().add(createFrameworkRepository(project));
			project.getPluginManager().apply(FakeMavenRepositoryPlugin.class);

			NokeeServerService.Parameters parameters = (NokeeServerService.Parameters)project.getGradle().getSharedServices().getRegistrations().getByName(NOKEE_SERVER_SERVICE_NAME).getParameters();
			parameters.getToolLocators().add(XcrunLocator.class.getCanonicalName());
			parameters.getToolLocators().add(XcodebuildLocator.class.getCanonicalName());

			configure(project.getDependencies());
		} else {
			LOGGER.debug("Darwin framework resolution support is not configured because detected operating system is not macOS.");
		}
	}

	private static AdhocArtifactRepository createFrameworkRepository(Project project) {
		val repository = AdhocArtifactRepositoryFactory.forProject(project).create();
		repository.setName("framework");
		repository.getCacheDirectory().set(project.getLayout().getBuildDirectory().dir("m2/framework"));
		repository.content(content -> content.includeGroup("dev.nokee.framework"));

		val toolRepository = new ToolRepository();
		toolRepository.register("xcrun", new XcrunLocator());
		toolRepository.register("xcodebuild", new XcodebuildLocator());

		val handler = new FrameworkHandler(toolRepository);
		repository.setComponentSupplier(handler);
		repository.setComponentVersionLister(handler);
		return repository;
	}

	public static final class FrameworkHandler implements AdhocComponentSupplier, AdhocComponentLister {
		private final ToolRepository toolRepository;
		private final CommandLineToolExecutionEngine<CachingProcessBuilderEngine.Handle> engine = new CachingProcessBuilderEngine(LoggingEngine.wrap(new ProcessBuilderEngine()));

		public FrameworkHandler(ToolRepository toolRepository) {
			this.toolRepository = toolRepository;
		}

		@Override
		public void execute(AdhocComponentListerDetails details) {
			if (isKnownModule(details.getModuleIdentifier().getName())) {
				details.listed(findVersions(details.getModuleIdentifier().getName()));
			}
		}

		public List<String> findVersions(String moduleName) {
			XcodeSdk sdk = findMacOsSdks();
			return singletonList(findSdkVersion(sdk));
		}

		@Override
		public void execute(AdhocComponentSupplierDetails details) {
			if (isKnownVersion(details.getId().getModule(), details.getId().getVersion())) {
				val frameworkPath = getLocalPath(details.getId().getModule()).toPath();
				details.metadata(metadata(details.getId().getModule(), details.getId().getVersion()));
				details.file(details.getId().getModule() + ".framework.localpath", outStream -> {
					final File localPath = getLocalPath(details.getId().getModule());
					try {
						outStream.write(frameworkPath.toString().getBytes(StandardCharsets.UTF_8));
					} catch (
						IOException e) {
						throw new UncheckedIOException(e);
					}
				});
				findSubFrameworks(frameworkPath).forEach(it -> {
					details.file(it.getFileName() + ".localpath", outStream -> {
						try {
							outStream.write(it.toString().getBytes(StandardCharsets.UTF_8));
						} catch (
							IOException e) {
							throw new UncheckedIOException(e);
						}
					});
				});
			} else {
				LOGGER.info(String.format("The requested module '%s' version '%s' doesn't match current available versions '%s'.", details.getId().getModule(), details.getId().getVersion(), String.join(", ", findVersions(details.getId().getModule()))));
			}
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

		File getLocalPath(String frameworkName) {
			return new File(findSdkPath(findMacOsSdks()), "System/Library/Frameworks/" + frameworkName + ".framework");
		}

		public boolean isKnownModule(String moduleName) {
			if (getLocalPath(moduleName).exists()) {
				return true;
			}
			LOGGER.info(String.format("The requested framework '%s' wasn't found at in '%s/System/Library/Frameworks/'.", moduleName, findSdkPath(findMacOsSdks()).getPath()));
			return false;
		}

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

		Action<GradleModuleMetadata.Builder> metadata(String frameworkName, String version) {
			return builder -> {
				val frameworkPath = getLocalPath(frameworkName).toPath();
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
			};
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

		@SuppressWarnings("deprecation")
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

	public void configure(DependencyHandler dependencies) {
		dependencies.artifactTypes(it -> {
			it.create("localpath", type -> type.getAttributes().attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, SERIALIZED));
		});
		dependencies.registerTransform(DeserializeLocalFramework.class, variantTransform -> {
			variantTransform.getFrom()
				.attribute(ARTIFACT_TYPE_ATTRIBUTE, "localpath")
				.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, FRAMEWORK_BUNDLE))
				.attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, SERIALIZED);
			variantTransform.getTo()
				.attribute(ARTIFACT_TYPE_ATTRIBUTE, FRAMEWORK_TYPE)
				.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, FRAMEWORK_BUNDLE))
				.attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, DESERIALIZED);
		});
	}

	public static abstract class DeserializeLocalFramework implements TransformAction<TransformParameters.None> {
		@InputArtifact
		public abstract Provider<FileSystemLocation> getInputArtifact();

		public void transform(TransformOutputs outputs) {
			try {
				String s = FileUtils.readFileToString(getInputArtifact().get().getAsFile(), Charset.defaultCharset());
				File framework = new File(s);
				File o = outputs.dir(framework.getName());
				if (!o.delete()) {
					throw new RuntimeException("Can't delete file");
				}
				Files.createSymbolicLink(o.toPath(), framework.toPath());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
