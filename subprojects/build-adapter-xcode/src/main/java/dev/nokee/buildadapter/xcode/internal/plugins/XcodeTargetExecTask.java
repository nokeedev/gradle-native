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
import dev.nokee.xcode.AsciiPropertyListReader;
import dev.nokee.xcode.CompositeXCBuildSettingLayer;
import dev.nokee.xcode.DefaultXCBuildSettings;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettings;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.project.PBXObjectReference;
import dev.nokee.xcode.project.PBXProj;
import dev.nokee.xcode.project.PBXProjReader;
import dev.nokee.xcode.project.PBXProjWriter;
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
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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

		getAllArguments().addAll(getXcodeProject().map(it -> of("-project", it.getLocation().toString())));
		getAllArguments().addAll(getTargetName().map(it -> of("-target", it)));
		getAllArguments().addAll(getSdk().map(sdk -> of("-sdk", sdk)).orElse(of()));
		getAllArguments().addAll(getConfiguration().map(buildType -> of("-configuration", buildType)).orElse(of()));
		getAllArguments().addAll(getBuildSettings().asProvider().map(buildSettingsOverride()).map(toFlags()));

		finalizeValueOnRead(disallowChanges(getAllArguments()));

		this.targetReference = ZipProviderBuilder.newBuilder(objects).value(getXcodeProject()).value(getTargetName())
			.zip(XCProjectReference::ofTarget);
		this.buildSpec = finalizeValueOnRead(objects.property(XCBuildPlan.class).value(getTargetReference().map(XCLoaders.buildSpecLoader()::load).map(spec -> {
			final XCBuildSettings buildSettings = new DefaultXCBuildSettings(new CompositeXCBuildSettingLayer(ImmutableList.of(overrideLayer(), xcodebuildLayer())));

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
		val isolatedProjectLocation = new File(getTemporaryDir(), getXcodeProject().get().getLocation().getFileName().toString());

		val invocation = CommandLineTool.of("xcodebuild").withArguments(it -> {
			it.args(getAllArguments().map(allArguments -> concat(of("-project", isolatedProjectLocation.getAbsolutePath()), skip(allArguments, 2))));
		}).newInvocation(it -> {
			it.withEnvironmentVariables(inherit("PATH").putOrReplace("DEVELOPER_DIR", getXcodeInstallation().get().getDeveloperDirectory()));
			ifPresent(getWorkingDirectory(), it::workingDirectory);
			it.redirectStandardOutput(toFile(new File(getTemporaryDir(), "outputs.txt")));
			it.redirectErrorOutput(toStandardStream());
		});
		workerExecutor.noIsolation().submit(XcodebuildExec.class, spec -> {
			spec.getOutgoingDerivedDataPath().set(getOutputDirectory());
			spec.getXcodeDerivedDataPath().set(getDerivedDataPath());

			spec.getOriginalProjectLocation().set(getXcodeProject().get().getLocation().toFile());
			spec.getIsolatedProjectLocation().set(isolatedProjectLocation);
			spec.getTargetNameToIsolate().set(getTargetName());

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

	public static final class XcodeProjectIsolationRunnable implements Runnable {
		private final FileSystemOperations fileOperations;
		private final Parameters parameters;
		private final Runnable delegate;

		public XcodeProjectIsolationRunnable(FileSystemOperations fileOperations, Parameters parameters, Runnable delegate) {
			this.fileOperations = fileOperations;
			this.parameters = parameters;
			this.delegate = delegate;
		}

		@Override
		public void run() {
			val originalProjectLocation = parameters.getOriginalProjectLocation().get().getAsFile().toPath();
			val isolatedProjectLocation = parameters.getIsolatedProjectLocation().get().getAsFile().toPath();
			fileOperations.sync(spec -> {
				spec.from(originalProjectLocation);
				spec.into(isolatedProjectLocation);
			});

			try {
				PBXProj proj;
				try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(isolatedProjectLocation.resolve("project.pbxproj"))))) {
					proj = reader.read();
				}
				val builder = PBXProj.builder();
				val isolatedProject = builder.rootObject(proj.getRootObject()).objects(o -> {
					for (PBXObjectReference object : proj.getObjects()) {
						if (ImmutableSet.of("PBXNativeTarget", "PBXAggregateTarget", "PBXLegacyTarget").contains(object.isa()) && parameters.getTargetNameToIsolate().get().equals(object.getFields().get("name"))) {
							o.add(PBXObjectReference.of(object.getGlobalID(), entryBuilder -> {
								for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
									if (!entry.getKey().equals("dependencies")) {
										entryBuilder.putField(entry.getKey(), entry.getValue());
									}
								}
							}));
						} else if ("PBXProject".equals(object.isa())) {
							o.add(PBXObjectReference.of(object.getGlobalID(), entryBuilder -> {
								entryBuilder.putField("projectDirPath", originalProjectLocation.getParent().toString());
								for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
									if (!entry.getKey().equals("projectDirPath")) {
										entryBuilder.putField(entry.getKey(), entry.getValue());
									}
								}
							}));
						} else {
							o.add(object);
						}
					}
				}).build();
				try (val writer = new PBXProjWriter(Files.newBufferedWriter(isolatedProjectLocation.resolve("project.pbxproj")))) {
					writer.write(isolatedProject);
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			delegate.run();
		}

		public interface Parameters {
			DirectoryProperty getOriginalProjectLocation();
			DirectoryProperty getIsolatedProjectLocation();
			Property<String> getTargetNameToIsolate();
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
		interface Parameters extends WorkParameters, DerivedDataAssemblingRunnable.Parameters, ProcessExecutionRunnable.Parameters, XcodeProjectIsolationRunnable.Parameters {}

		@Inject
		protected abstract ExecOperations getExecOperations();

		@Inject
		protected abstract FileSystemOperations getFileOperations();

		@Override
		public void execute() {
			derivedDataPath(isolateProject(executeBuild())).run();
		}

		private Runnable executeBuild() {
			return new ProcessExecutionRunnable(getExecOperations(), getParameters());
		}

		private Runnable isolateProject(Runnable action) {
			return new XcodeProjectIsolationRunnable(getFileOperations(), getParameters(), action);
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
