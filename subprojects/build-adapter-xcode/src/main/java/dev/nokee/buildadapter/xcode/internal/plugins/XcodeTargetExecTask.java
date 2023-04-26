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
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolInvocation;
import dev.nokee.util.provider.ZipProviderBuilder;
import dev.nokee.utils.FileSystemLocationUtils;
import dev.nokee.xcode.CompositeXCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettings;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.objects.files.PBXReference;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.Transformer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
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
			.targetReference(getTargetReference())
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
				spec.from(parameters.getXcodeDerivedDataPath(), it -> it.include("Build/Products/**/*"));
				spec.into(parameters.getOutgoingDerivedDataPath());
			});
		}

		public interface Parameters {
			DirectoryProperty getXcodeDerivedDataPath();
			DirectoryProperty getOutgoingDerivedDataPath();
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
