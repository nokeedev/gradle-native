/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.buildadapter.xcode.internal.files.PreserveLastModifiedFileSystemOperation;
import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildPlan;
import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildSpec;
import dev.nokee.buildadapter.xcode.internal.plugins.xcfilelist.XCFileListReader;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolInvocation;
import dev.nokee.util.provider.ZipProviderBuilder;
import dev.nokee.utils.FileSystemLocationUtils;
import dev.nokee.utils.Optionals;
import dev.nokee.xcode.CompositeXCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettings;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXReference;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.skip;
import static dev.nokee.core.exec.CommandLineToolExecutionEngine.execOperations;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toFile;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toStandardStream;
import static dev.nokee.util.internal.NotPredicate.not;
import static dev.nokee.utils.ProviderUtils.disallowChanges;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;
import static dev.nokee.utils.ProviderUtils.ifPresent;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;

@CacheableTask
public abstract class XcodeTargetExecTask extends DefaultTask implements XcodebuildExecTask, HasConfigurableXcodeInstallation, HasXcodeTargetReference {
	private final WorkerExecutor workerExecutor;
	private final ObjectFactory objects;
	private final Provider<XCTargetReference> targetReference;
	private final Provider<XCBuildPlan> buildSpec;
	private final ConfigurableXCBuildSettings buildSettings;

	@Internal
	public abstract Property<XCProjectReference> getXcodeProject();

	@Internal
	public abstract Property<String> getTargetName();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	@Nested
	public abstract ListProperty<CommandLineArgumentProvider> getArguments();

	@Internal
	public abstract ListProperty<String> getAllArguments();

	@Internal
	public ConfigurableXCBuildSettings getBuildSettings() {
		return buildSettings;
	}

	@Override
	@Internal
	public Provider<XCTargetReference> getTargetReference() {
		return targetReference;
	}

	@Nested
	public Provider<XCBuildPlan> getBuildPlan() {
		return buildSpec;
	}

	@InputFiles
	@PathSensitive(PathSensitivity.ABSOLUTE)
	protected abstract ConfigurableFileCollection getInputFiles();

	@Internal
	protected abstract SetProperty<String> getOutputPaths();

	@Inject
	public XcodeTargetExecTask(WorkerExecutor workerExecutor, ObjectFactory objects, ProviderFactory providers) {
		this.workerExecutor = workerExecutor;
		this.objects = objects;
		this.buildSettings = objects.newInstance(ConfigurableXCBuildSettings.class);

		// Account for build settings overrides
		getArguments().add(new CommandLineArgumentProvider() {
			@Input
			public Provider<List<String>> getAdditionalBuildSettings() {
				return getBuildSettings().asProvider().map(buildSettingsOverride()).map(toFlags());
			}

			@Override
			public Iterable<String> asArguments() {
				return getAdditionalBuildSettings().get();
			}
		});

		getAllArguments().addAll(getXcodeProject().map(it -> of("-project", it.getLocation().toString())));
		getAllArguments().addAll(getTargetName().map(it -> of("-target", it)));
		getAllArguments().addAll(getSdk().map(sdk -> of("-sdk", sdk)).orElse(of()));
		getAllArguments().addAll(getConfiguration().map(buildType -> of("-configuration", buildType)).orElse(of()));
		getAllArguments().addAll(getArguments().map(flatTransformEach(CommandLineArgumentProvider::asArguments)));

		finalizeValueOnRead(disallowChanges(getAllArguments()));

		this.targetReference = ZipProviderBuilder.newBuilder(objects).value(getXcodeProject()).value(getTargetName())
			.zip(XCProjectReference::ofTarget);

		this.buildSpec = finalizeValueOnRead(objects.property(XCBuildPlan.class).value(getTargetReference().map(XCLoaders.buildSpecLoader()::load).map(spec -> {
			final XCBuildSettings buildSettings = getBuildSettings();

			val context = new BuildSettingsResolveContext(FileSystems.getDefault(), buildSettings);
			val fileRefs = XCLoaders.fileReferences().load(getXcodeProject().get());
			return spec.resolve(new XCBuildSpec.ResolveContext() {
				private Path resolve(PBXReference reference) {
					return fileRefs.get(reference).resolve(context);
				}

				@Override
				public FileCollection inputs(PBXReference reference) {
					final Path path = resolve(reference);
					return objects.fileCollection().from((Callable<Object>) () -> {
						if (Files.isDirectory(path)) {
							return objects.fileTree().setDir(path);
						} else {
							return path;
						}
					});
				}

				@Override
				public String getConfiguration() {
					return XcodeTargetExecTask.this.getConfiguration().get();
				}
			});
		})));

		buildSettings.setFrom(xcodebuildLayer());
		buildSettings.from(overrideLayer());

		getInputFiles().from(ZipProviderBuilder.newBuilder(objects).value(getTargetReference()).value(getBuildSettings().asProvider()).zip((targetReference, buildSettings) -> {
			val context = new BuildSettingsResolveContext(FileSystems.getDefault(), buildSettings);
			val fileRefs = XCLoaders.fileReferences().load(targetReference.getProject());
			val target = XCLoaders.pbxtargetLoader().load(targetReference);

			final ImmutableList.Builder<PBXReference> references = ImmutableList.builder();
			for (PBXBuildPhase buildPhase : target.getBuildPhases()) {
				buildPhase.getFiles().stream().flatMap(it -> Optionals.stream(it.getFileRef())).map(PBXReference.class::cast).forEach(references::add);
				if (buildPhase instanceof PBXShellScriptBuildPhase) {
					((PBXShellScriptBuildPhase) buildPhase).getInputPaths().stream()
						.map(String::trim).filter(not(String::isEmpty))
						.map(PBXFileReference::ofAbsolutePath)
						.forEach(references::add);
					((PBXShellScriptBuildPhase) buildPhase).getInputFileListPaths().stream()
						.map(String::trim).filter(not(String::isEmpty))
						.map(PBXFileReference::ofAbsolutePath)
						.map(it -> fileRefs.get(it).resolve(context))
						.map(XcodeTargetExecTask::readFileList)
						.forEach(references::addAll);
				}
			}

			final ImmutableSet.Builder<Path> paths = ImmutableSet.builder();
			for (PBXReference reference : references.build()) {
				paths.add(fileRefs.get(reference).resolve(context));
			}

			return paths.build();
		}));

		getOutputPaths().addAll(ZipProviderBuilder.newBuilder(objects).value(getTargetReference()).value(getBuildSettings().asProvider()).value(getDerivedDataPath().map(FileSystemLocationUtils::asPath)).zip((values) -> {
			final XCTargetReference targetReference = values.get(0);
			final XCBuildSettings buildSettings = values.get(1);
			final Path derivedDataPath = values.get(2);
			val context = new BuildSettingsResolveContext(FileSystems.getDefault(), buildSettings);
			val fileRefs = XCLoaders.fileReferences().load(targetReference.getProject());
			val target = XCLoaders.pbxtargetLoader().load(targetReference);

			final ImmutableList.Builder<PBXReference> references = ImmutableList.builder();
			target.getProductReference().ifPresent(references::add);
			for (PBXBuildPhase buildPhase : target.getBuildPhases()) {
				if (buildPhase instanceof PBXShellScriptBuildPhase) {
					((PBXShellScriptBuildPhase) buildPhase).getOutputPaths().stream()
						.map(String::trim).filter(not(String::isEmpty))
						.map(PBXFileReference::ofAbsolutePath)
						.forEach(references::add);
					((PBXShellScriptBuildPhase) buildPhase).getOutputFileListPaths().stream()
						.map(String::trim).filter(not(String::isEmpty))
						.map(PBXFileReference::ofAbsolutePath)
						.map(it -> fileRefs.get(it).resolve(context))
						.map(XcodeTargetExecTask::readFileList)
						.forEach(references::addAll);
				}
			}
			references.add(PBXFileReference.ofAbsolutePath("$(CONFIGURATION_BUILD_DIR)/$(SWIFT_MODULE_NAME).swiftmodule"));

			final ImmutableSet.Builder<Path> paths = ImmutableSet.builder();
			for (PBXReference reference : references.build()) {
				paths.add(fileRefs.get(reference).resolve(context));
			}

			val productPath = derivedDataPath.resolve("Build/Products");
			final ImmutableList.Builder<String> result = ImmutableList.builder();
			for (Path path : paths.build()) {
				if (path.startsWith(productPath)) {
					result.add(derivedDataPath.relativize(path).toString());
				}
			}

			return result.build();
		}));
	}

	private static List<PBXReference> readFileList(Path path) {
		try (val reader = new XCFileListReader(Files.newBufferedReader(path))) {
			return reader.read().stream().map(PBXFileReference::ofAbsolutePath).collect(ImmutableList.toImmutableList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Transformer<Map<String, XCBuildSetting>, XCBuildSettings> buildSettingsOverride() {
		return buildSettings -> {
			val result = new LinkedHashMap<String, XCBuildSetting>();

			Set<String> buildSettingsToIgnore = ImmutableSet.<String>builder()
				.add("SDKROOT", "DEVELOPER_DIR") // let Xcode dictate the real value
				.add("TARGET_NAME", "TARGETNAME", "PROJECT_NAME") // when using SwiftPM, the override leak into the package causing incoherent builds
				.build();

			// Visit the build settings ignoring the XcodebuildBuildSettingLayer
			new CompositeXCBuildSettingLayer((Iterable<XCBuildSettingLayer>) buildSettings).accept(new XCBuildSettingLayer.Visitor() {
				@Override
				public void visit(XCBuildSettingLayer layer) {
					if (!(layer instanceof XcodebuildBuildSettingLayer)) {
						layer.findAll().forEach((k, v) -> {
							if (!result.containsKey(k) && !buildSettingsToIgnore.contains(k)) {
								result.put(k, v);
							}
						});
					}
				}
			});

			return result;
		};
	}

	private static Transformer<List<String>, Map<String, XCBuildSetting>> toFlags() {
		return buildSettings -> {
			return buildSettings.entrySet().stream()
				// We use XCBuildSetting#toString() to get a representation of the build setting to use on the command line.
				//   Not perfect, but good enough for now.
				.map(it -> it.getKey() + "=" + it.getValue().toString()) //
				.collect(Collectors.toList());
		};
	}

	private XCBuildSettingLayer overrideLayer() {
		return new CompositeXCBuildSettingLayer(ImmutableList.of(derivedDataPathLayer(), shortcutLayer(), disableCodeSigning()));
	}

	private static XCBuildSettingLayer shortcutLayer() {
		return new KnownBuildSettingsLayerBuilder().build();
	}

	private XCBuildSettingLayer derivedDataPathLayer() {
		return new ProvidedBuildSettingsBuilder(objects)
			.derivedDataPath(getDerivedDataPath().map(FileSystemLocationUtils::asPath))
			.configuration(getConfiguration())
			.developerDir(getXcodeInstallation().map(XcodeInstallation::getDeveloperDirectory))
			.platformName(getSdk())
			.targetReference(getTargetReference()) // trigger resolution of projectReference as well
			.build();
	}

	private static XCBuildSettingLayer disableCodeSigning() {
		return new CodeSigningDisabledBuildSettingLayerBuilder().build();
	}

	private XCBuildSettingLayer xcodebuildLayer() {
		return new XcodebuildBuildSettingLayer.Builder(objects)
			.targetReference(getTargetReference())
			.sdk(getSdk())
			.configuration(getConfiguration())
			.developerDir(getXcodeInstallation().map(XcodeInstallation::getDeveloperDirectory))
			.buildSettings(objects.mapProperty(String.class, XCBuildSetting.class).value(getBuildSettings().asProvider().map(buildSettingsOverride())))
			.build();
	}

	@TaskAction
	private void doExec() {
		val invocation = CommandLineTool.of("xcodebuild").withArguments(it -> {
			it.args(getAllArguments().map(allArguments -> concat(of("-project", getXcodeProject().get().getLocation()), skip(allArguments, 2))));
		}).newInvocation(it -> {
			it.withEnvironmentVariables(inherit("PATH").putOrReplace("DEVELOPER_DIR", getXcodeInstallation().get().getDeveloperDirectory()));
			ifPresent(getWorkingDirectory(), it::workingDirectory);
			it.redirectStandardOutput(toFile(new File(getTemporaryDir(), "outputs.txt")));
			it.redirectErrorOutput(toStandardStream());
		});
		workerExecutor.noIsolation().submit(XcodebuildExec.class, spec -> {
			spec.getOutgoingDerivedDataPath().set(getOutputDirectory());
			spec.getXcodeDerivedDataPath().set(getDerivedDataPath());
			spec.getOutputPaths().addAll(getOutputPaths());

			spec.getInvocation().set(invocation);
		});
	}

	public static final class DerivedDataAssemblingRunnable implements Runnable {
		private final FileSystemOperations fileOperations;
		private final Parameters parameters;
		private final Runnable delegate;

		public DerivedDataAssemblingRunnable(FileSystemOperations fileOperations, Parameters parameters, Runnable delegate) {
			this.fileOperations = fileOperations;
			this.parameters = parameters;
			this.delegate = delegate;
		}

		@Override
		public void run() {
			delegate.run();

			new PreserveLastModifiedFileSystemOperation(fileOperations::sync).execute(spec -> {
				spec.from(parameters.getXcodeDerivedDataPath(), it -> {
					for (String outputPath : parameters.getOutputPaths().get()) {
						it.include(outputPath);
						it.include(outputPath + "/**/*");
					}

					// Normalize .modulemap files
					it.eachFile(new Action<FileCopyDetails>() {
						private final Path derivedDataPath = parameters.getXcodeDerivedDataPath().get().getAsFile().toPath();

						@Override
						public void execute(FileCopyDetails details) {
							if (details.getName().endsWith(".modulemap")) {
								details.filter(line -> line.replace(derivedDataPath.resolve(details.getRelativePath().getParent().toString()) + "/", ""));
							}
						}
					});
				});
				spec.into(parameters.getOutgoingDerivedDataPath());
				spec.setIncludeEmptyDirs(false);
			});
		}

		public interface Parameters {
			DirectoryProperty getXcodeDerivedDataPath();
			DirectoryProperty getOutgoingDerivedDataPath();
			ListProperty<String> getOutputPaths();
		}
	}

	public static final class ProcessExecutionRunnable implements Runnable {
		private final ExecOperations execOperations;
		private final Parameters parameters;

		public ProcessExecutionRunnable(ExecOperations execOperations, Parameters parameters) {
			this.execOperations = execOperations;
			this.parameters = parameters;
		}

		@Override
		public void run() {
			parameters.getInvocation().get().submitTo(execOperations(execOperations)).result().assertNormalExitValue();
		}

		public interface Parameters {
			Property<CommandLineToolInvocation> getInvocation();
		}
	}

	public static abstract class XcodebuildExec implements WorkAction<XcodebuildExec.Parameters> {
		interface Parameters extends WorkParameters, DerivedDataAssemblingRunnable.Parameters, ProcessExecutionRunnable.Parameters {}

		@Inject
		protected abstract ExecOperations getExecOperations();

		@Inject
		protected abstract FileSystemOperations getFileOperations();

		@Override
		public void execute() {
			derivedDataPath(executeBuild()).run();
		}

		private Runnable executeBuild() {
			return new ProcessExecutionRunnable(getExecOperations(), getParameters());
		}

		private Runnable derivedDataPath(Runnable action) {
			return new DerivedDataAssemblingRunnable(getFileOperations(), getParameters(), action);
		}
	}

	private List<String> asFlags(Map<String, String> buildSettings) {
		val builder = ImmutableList.<String>builder();
		buildSettings.forEach((k, v) -> builder.add(k + "=" + v));
		return builder.build();
	}
}
