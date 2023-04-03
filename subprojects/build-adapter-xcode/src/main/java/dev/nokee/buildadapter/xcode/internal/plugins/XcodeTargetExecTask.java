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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MoreCollectors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.nokee.buildadapter.xcode.internal.files.PreserveLastModifiedFileSystemOperation;
import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildPlan;
import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildSpec;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlayWriter;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolArguments;
import dev.nokee.core.exec.CommandLineToolInvocation;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.util.internal.NotPredicate;
import dev.nokee.utils.FileSystemLocationUtils;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.xcode.AsciiPropertyListReader;
import dev.nokee.xcode.DefaultXCBuildSettingLayer;
import dev.nokee.xcode.DefaultXCBuildSettings;
import dev.nokee.xcode.ProvidedMapAdapter;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettingLiteral;
import dev.nokee.xcode.XCBuildSettings;
import dev.nokee.xcode.XCBuildSettingsEmptyLayer;
import dev.nokee.xcode.XCFileReference;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.buildphase.PBXHeadersBuildPhase;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.objects.targets.ProductTypes;
import dev.nokee.xcode.objects.targets.TargetDependenciesAwareBuilder;
import dev.nokee.xcode.project.CodeableXCRemoteSwiftPackageReference;
import dev.nokee.xcode.project.CodeableXCSwiftPackageProductDependency;
import dev.nokee.xcode.project.PBXObjectArchiver;
import dev.nokee.xcode.project.PBXProj;
import dev.nokee.xcode.project.PBXProjReader;
import dev.nokee.xcode.project.PBXProjWriter;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.skip;
import static dev.nokee.core.exec.CommandLineToolExecutionEngine.execOperations;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toFile;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toNullStream;
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

	@Inject
	protected abstract ExecOperations getExecOperations();

	@Internal
	public abstract Property<XCProjectReference> getXcodeProject();

	@Internal
	public abstract Property<String> getTargetName();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	@Internal
	public abstract ListProperty<String> getAllArguments();

	@Override
	@Internal
	public Provider<XCTargetReference> getTargetReference() {
		return targetReference;
	}

	@OutputFile
	public abstract RegularFileProperty getVfsOverlayFile();

	@Internal
	public abstract ConfigurableFileCollection getVfsOverlays();

	@OutputFile
	public abstract RegularFileProperty getOutputRemoteSwiftPackages();

	@Internal
	public abstract ConfigurableFileCollection getRemoteSwiftPackages();

//	@Internal
//	public abstract MapProperty<String, String> getAllBuildSettings();

	@Nested
	public Provider<XCBuildPlan> getBuildPlan() {
		return buildSpec;
	}

	@Inject
	public XcodeTargetExecTask(WorkerExecutor workerExecutor, ObjectFactory objects, ProviderFactory providers) {
		this.workerExecutor = workerExecutor;
		this.objects = objects;

		getAllArguments().addAll(getXcodeProject().map(it -> of("-project", it.getLocation().toString())));
		getAllArguments().addAll(getTargetName().map(it -> of("-target", it)));
		getAllArguments().addAll(getSdk().map(sdk -> of("-sdk", sdk)).orElse(of()));
		getAllArguments().addAll(getConfiguration().map(buildType -> of("-configuration", buildType)).orElse(of()));
		getAllArguments().addAll(providers.provider(() -> {
			ImmutableSet.Builder<String> builder = ImmutableSet.builder();
			builder.add("SDKROOT", "DEVELOPER_DIR"); // let Xcode dictate the real value
			builder.add("TARGET_NAME", "TARGETNAME", "PROJECT_NAME"); // when using SwiftPM, the override leak into the package causing incoherent builds
			Set<String> buildSettingsToIgnore = builder.build();
			return overrideLayer(new XCBuildSettingsEmptyLayer()).findAll().entrySet().stream() //
				.filter(new NotPredicate<>(it -> buildSettingsToIgnore.contains(it.getKey()))) //

				// We use XCBuildSetting#toString() to get a representation of the build setting to use on the command line.
				//   Not perfect, but good enough for now.
				.map(it -> it.getKey() + "=" + it.getValue().toString()) //
				.collect(Collectors.toList());
		}));
		getAllArguments().addAll(getVfsOverlays().getElements().map(overlays -> {
			if (overlays.isEmpty()) {
				return ImmutableList.of();
			} else {
				Path allProductsHeaders = getTemporaryDir().toPath().resolve("all-products-headers.yaml");
				try (val writer = new VirtualFileSystemOverlayWriter(Files.newBufferedWriter(allProductsHeaders))) {
					val directories = ImmutableList.<VirtualFileSystemOverlay.VirtualDirectory>builder();
					for (FileSystemLocation overlay : overlays) {
						try (val inStream = Files.newInputStream(overlay.getAsFile().toPath())) {
							val v = SerializationUtils.<VirtualFileSystemOverlay>deserialize(inStream);
							v.forEach(it -> {
								directories.add(new VirtualFileSystemOverlay.VirtualDirectory(getDerivedDataPath().get().getAsFile().getAbsolutePath() + "/" + it.getName(), ImmutableList.copyOf(it)));
							});
						}
					}
					writer.write(new VirtualFileSystemOverlay(directories.build()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				ImmutableList.Builder<String> builder = ImmutableList.builder();
				builder.add("OTHER_CFLAGS=$(inherited) -ivfsoverlay \"" + allProductsHeaders + "\"");
				builder.add("OTHER_SWIFT_FLAGS=$(inherited) -vfsoverlay \"" + allProductsHeaders + "\"");
				return builder.build();
			}
		}));

		finalizeValueOnRead(disallowChanges(getAllArguments()));

//		val layers = overrideLayer(xcodebuildLayer());
//		getAllBuildSettings().putAll(providers.provider(() -> {
//			final XCBuildSettings buildSettings = new DefaultXCBuildSettings(layers);
//			return layers.findAll().values().stream().collect(Collectors.toMap(XCBuildSetting::getName, it -> it.evaluate(new DefaultXCBuildSettingEvaluationContext(buildSettings))));
//		}));
//		getAllBuildSettings().finalizeValueOnRead();

		this.targetReference = ProviderUtils.zip(() -> objects.listProperty(Object.class), getXcodeProject(), getTargetName(), XCProjectReference::ofTarget);
		this.buildSpec = finalizeValueOnRead(objects.property(XCBuildPlan.class).value(getTargetReference().map(XCLoaders.buildSpecLoader()::load).map(spec -> {
			final XCBuildSettings buildSettings = new DefaultXCBuildSettings(overrideLayer(xcodebuildLayer()));

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
	}

	private XCBuildSettingLayer overrideLayer(XCBuildSettingLayer delegate) {
		return derivedDataPathLayer(shortcutLayer(disableCodeSigning(delegate)));
	}

	private static XCBuildSettingLayer shortcutLayer(XCBuildSettingLayer delegate) {
		return new KnownBuildSettingsLayerBuilder().next(delegate).build();
	}

	private XCBuildSettingLayer derivedDataPathLayer(XCBuildSettingLayer delegate) {
		return new ProvidedBuildSettingsBuilder(objects)
			.next(delegate)
			.derivedDataPath(getDerivedDataPath().map(FileSystemLocationUtils::asPath))
			.configuration(getConfiguration())
			.developerDir(getXcodeInstallation().map(XcodeInstallation::getDeveloperDirectory))
			.platformName(getSdk())
			.targetReference(getTargetReference())
			.build();
	}

	private static XCBuildSettingLayer disableCodeSigning(XCBuildSettingLayer delegate) {
		return new CodeSigningDisabledBuildSettingLayerBuilder().next(delegate).build();
	}

	private XCBuildSettingLayer xcodebuildLayer() {
		val effectiveBuildSettings = finalizeValueOnRead(disallowChanges(objects.mapProperty(String.class, String.class)
			.value(getAllArguments().map(allArguments -> {
				return CommandLineTool.of("xcodebuild").withArguments(it -> {
						it.args(allArguments);
						it.args("-showBuildSettings", "-json");
					}).newInvocation(it -> {
						it.withEnvironmentVariables(inherit("PATH").putOrReplace("DEVELOPER_DIR", getXcodeInstallation().get().getDeveloperDirectory()));
						ifPresent(getWorkingDirectory(), it::workingDirectory);
					}).submitTo(execOperations(getExecOperations())).result()
					.getStandardOutput().parse(output -> {
						@SuppressWarnings("unchecked")
						val parsedOutput = (List<ShowBuildSettingsEntry>) new Gson().fromJson(output, new TypeToken<List<ShowBuildSettingsEntry>>() {}.getType());
						return parsedOutput.get(0).getBuildSettings();
					});
			}))));

		return new DefaultXCBuildSettingLayer(new ProvidedMapAdapter<>(effectiveBuildSettings.map(it -> {
			ImmutableMap.Builder<String, XCBuildSetting> builder = ImmutableMap.builder();
			it.forEach((key, value) -> {
				builder.put(key, new XCBuildSettingLiteral(key, value));
			});
			return builder.build();
		})), new XCBuildSettingsEmptyLayer());
	}

	private static final class ShowBuildSettingsEntry {
		private final Map<String, String> buildSettings;

		private ShowBuildSettingsEntry(Map<String, String> buildSettings) {
			this.buildSettings = buildSettings;
		}

		public Map<String, String> getBuildSettings() {
			return buildSettings;
		}
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
			spec.getVirtualFileSystemOverlayFile().set(getVfsOverlayFile());

			spec.getOutputRemoteSwiftPackages().set(getOutputRemoteSwiftPackages());
			spec.getRemoteSwiftPackages().from(getRemoteSwiftPackages());

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

			PBXProj p = null;
			try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(originalProjectLocation.resolve("project.pbxproj"))))) {
				p = reader.read();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			final PBXProj proj = p;

			PBXProject project = XCLoaders.pbxprojectLoader().load(XCProjectReference.of(originalProjectLocation));
			val newProject = project.toBuilder().projectDirPath(originalProjectLocation.getParent().toString())
				.targets(project.getTargets().stream()
					.filter(target -> target.getName().equals(parameters.getTargetNameToIsolate().get()))
					.map(target -> {
						val builder = target.toBuilder();
						((TargetDependenciesAwareBuilder<?>) builder).dependencies(of());


						if (target instanceof PBXNativeTarget) {
							for (File remoteSwiftPackage : parameters.getRemoteSwiftPackages()) {
								try {
									List<String> gids = SerializationUtils.deserialize(Files.newInputStream(remoteSwiftPackage.toPath()));
									for (String gid : gids) {
										val productName = (String) proj.getObjects().getById(gid).getFields().get("productName");
										val gidPackageRef = proj.getObjects().getById(gid).getFields().get("package");
										val packageRef = project.getPackageReferences().stream().map(CodeableXCRemoteSwiftPackageReference.class::cast).filter(it -> it.globalId().equals(gidPackageRef)).collect(MoreCollectors.onlyElement());
										((PBXNativeTarget.Builder) builder).packageProductDependency(it -> it.productName(productName).packageReference(packageRef));
									}
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
						}

						return builder.build();
					}).collect(Collectors.toList()))
				.build();

			try (val writer = new PBXProjWriter(Files.newBufferedWriter(isolatedProjectLocation.resolve("project.pbxproj")))) {
				writer.write(new PBXObjectArchiver().encode(newProject));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			delegate.run();

			val values = new CommandLineToolInvocation.Builder()
				.commandLine(new CommandLine(CommandLineTool.of(parameters.getInvocation().get().getExecutable().getLocation()), CommandLineToolArguments.of(ImmutableList.<String>builder().addAll(parameters.getInvocation().get().getArguments().get()).add("-showBuildSettings").add("-json").build())))
				.workingDirectory(parameters.getInvocation().get().getWorkingDirectory())
				.withEnvironmentVariables(parameters.getInvocation().get().getEnvironmentVariables())
				.redirectErrorOutput(toNullStream())
				.redirectStandardOutput(toNullStream())
				.buildAndSubmit(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(output -> {
					@SuppressWarnings("unchecked")
					val parsedOutput = (List<ShowBuildSettingsEntry>) new Gson().fromJson(output, new TypeToken<List<ShowBuildSettingsEntry>>() {}.getType());
					return parsedOutput.get(0).getBuildSettings();
				});
			XCBuildSettings buildSettings = new DefaultXCBuildSettings(new DefaultXCBuildSettingLayer(values.entrySet().stream().collect(Collectors.toMap(it -> it.getKey(), it -> new XCBuildSettingLiteral(it.getKey(), it.getValue()))), new XCBuildSettingsEmptyLayer()));
			val fileRefs = XCLoaders.fileReferences().load(XCProjectReference.of(parameters.getOriginalProjectLocation().get().getAsFile().toPath()));
			val target = XCLoaders.pbxtargetLoader().load(XCProjectReference.of(parameters.getOriginalProjectLocation().get().getAsFile().toPath()).ofTarget(parameters.getTargetNameToIsolate().get()));
			List<VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry> remapping = Stream.of(target).filter(PBXNativeTarget.class::isInstance)
				.filter(it -> it.getProductType().map(ProductTypes.FRAMEWORK::equals).orElse(false))
				.flatMap(it -> it.getBuildPhases().stream())
				.filter(PBXHeadersBuildPhase.class::isInstance)
				.flatMap(it -> it.getFiles().stream())
				.filter(it -> it.getFileRef().isPresent())
				.filter(it -> it.getSettings().containsKey("ATTRIBUTES"))
				.filter(it -> it.getSettings().get("ATTRIBUTES") instanceof List)
				.filter(it -> ((List<?>) it.getSettings().get("ATTRIBUTES")).contains("Public"))
				.map(it -> it.getFileRef().get())
				// TODO: Be careful about file that may be generated...
				.map(it -> fileRefs.get((PBXReference) it))
				.map(it -> it.resolve(new BuildSettingsResolveContext(FileSystems.getDefault(), buildSettings)))
				.map(it -> new VirtualFileSystemOverlay.VirtualDirectory.RemappedFile(it.getFileName().toString(), it.toString()))
				.collect(Collectors.toList());
			List<VirtualFileSystemOverlay.VirtualDirectory> virtualDirectories = new ArrayList<>();
			if (!remapping.isEmpty()) {
				virtualDirectories.add(new VirtualFileSystemOverlay.VirtualDirectory(parameters.getXcodeDerivedDataPath().get().getAsFile().toPath().relativize(XCFileReference.builtProduct("$(PUBLIC_HEADERS_FOLDER_PATH)").resolve(new BuildSettingsResolveContext(FileSystems.getDefault(), buildSettings))).toString(), remapping));
			}

			try (val outStream = Files.newOutputStream(parameters.getVirtualFileSystemOverlayFile().get().getAsFile().toPath())) {
				SerializationUtils.serialize(new VirtualFileSystemOverlay(virtualDirectories), outStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}


			ArrayList<String> packagesGids = new ArrayList<>();
			if (target instanceof PBXNativeTarget) {
				((PBXNativeTarget) target).getPackageProductDependencies().stream().map(it -> ((CodeableXCSwiftPackageProductDependency) it).globalId()).forEach(packagesGids::add);
			}

			try (val outStream = Files.newOutputStream(parameters.getOutputRemoteSwiftPackages().get().getAsFile().toPath())) {
				SerializationUtils.serialize(packagesGids, outStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public interface Parameters {
			DirectoryProperty getOriginalProjectLocation();
			DirectoryProperty getIsolatedProjectLocation();
			Property<String> getTargetNameToIsolate();
			RegularFileProperty getVirtualFileSystemOverlayFile();
			Property<CommandLineToolInvocation> getInvocation();
			DirectoryProperty getXcodeDerivedDataPath();

			RegularFileProperty getOutputRemoteSwiftPackages();
			ConfigurableFileCollection getRemoteSwiftPackages();
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
