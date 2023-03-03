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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.nokee.buildadapter.xcode.internal.files.PreserveLastModifiedFileSystemOperation;
import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildPlan;
import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildSpec;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolInvocation;
import dev.nokee.utils.FileSystemLocationUtils;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.xcode.AsciiPropertyListReader;
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
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFiles;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.skip;
import static dev.nokee.buildadapter.xcode.internal.plugins.XCBuildSettingsUtils.codeSigningDisabled;
import static dev.nokee.core.exec.CommandLineToolExecutionEngine.execOperations;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toFile;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toStandardStream;
import static dev.nokee.utils.ProviderUtils.disallowChanges;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;
import static dev.nokee.utils.ProviderUtils.ifPresent;

public abstract class XcodeTargetExecTask extends DefaultTask implements XcodebuildExecTask, HasConfigurableXcodeInstallation, HasXcodeTargetReference {
	private final WorkerExecutor workerExecutor;
	private final Provider<XCTargetReference> targetReference;
	private final Provider<XCBuildPlan> buildSpec;

	@Inject
	protected abstract ExecOperations getExecOperations();

	@Internal
	public abstract Property<XCProjectReference> getXcodeProject();

	//	@Input // FIXME: switch to internal because covered by build spec
	@Internal
	public abstract Property<String> getTargetName();

	@InputFiles
	public abstract ConfigurableFileCollection getInputDerivedData();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	@Internal
	public abstract ListProperty<String> getAllArguments();

	@Internal // FIXME: Should snapshot the props
	public abstract MapProperty<String, String> getAllBuildSettings();

	@Override
	@Internal
	public Provider<XCTargetReference> getTargetReference() {
		return targetReference;
	}

	int i = 0;

	@Nested
	public Provider<XCBuildPlan> getBuildPlan() {
		if (i++ < 2) {
			return getProject().provider(() -> new XCBuildPlan() {});
		}
		return buildSpec;
	}

	@Inject
	public XcodeTargetExecTask(WorkerExecutor workerExecutor, ObjectFactory objects) {
		this.workerExecutor = workerExecutor;

		getAllArguments().addAll(getXcodeProject().map(it -> of("-project", it.getLocation().toString())));
		getAllArguments().addAll(getTargetName().map(it -> of("-target", it)));
		getAllArguments().addAll(getSdk().map(sdk -> of("-sdk", sdk)).orElse(of()));
		getAllArguments().addAll(getConfiguration().map(buildType -> of("-configuration", buildType)).orElse(of()));
		getAllArguments().addAll(getDerivedDataPath().map(FileSystemLocationUtils::asPath)
			.map(derivedDataPath -> of("PODS_BUILD_DIR=" + derivedDataPath.resolve("Build/Products"))));
		getAllArguments().addAll(getDerivedDataPath().map(FileSystemLocationUtils::asPath)
			.map(new DerivedDataPathAsBuildSettings()).map(this::asFlags));
		getAllArguments().addAll(codeSigningDisabled());

		finalizeValueOnRead(disallowChanges(getAllArguments()));

		finalizeValueOnRead(disallowChanges(getAllBuildSettings().value(getAllArguments().map(allArguments -> {
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

		this.targetReference = ProviderUtils.zip(() -> objects.listProperty(Object.class), getXcodeProject(), getTargetName(), XCProjectReference::ofTarget);
		this.buildSpec = finalizeValueOnRead(objects.property(XCBuildPlan.class).value(getTargetReference().map(XCLoaders.buildSpecLoader()::load).map(spec -> {
			val xcodeInstallation = getXcodeInstallation().get();
			final XCBuildSettings buildSettings = new XCBuildSettings() {
				@Override
				public String get(String name) {
					switch (name) {
						case "BUILT_PRODUCT_DIR":
							// TODO: The following is only an approximation of what the BUILT_PRODUCT_DIR would be, use -showBuildSettings
							// TODO: Guard against the missing derived data path
							// TODO: We should map derived data path as a collection of build settings via helper method
							return getDerivedDataPath().dir("Build/Products/" + getConfiguration().get() + "-" + getSdk().get()).get().getAsFile().getAbsolutePath();
						case "DEVELOPER_DIR":
							// TODO: Use -showBuildSettings to get DEVELOPER_DIR value (or we could guess it)
							return xcodeInstallation.getDeveloperDirectory().toString();
						case "SDKROOT":
							// TODO: Use -showBuildSettings to get SDKROOT value (or we could guess it)
							return getSdk().map(it -> {
									if (it.toLowerCase(Locale.ENGLISH).equals("iphoneos")) {
										return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk").toString();
									} else if (it.toLowerCase(Locale.ENGLISH).equals("macosx")) {
										return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk").toString();
									} else if (it.toLowerCase(Locale.ENGLISH).equals("iphonesimulator")) {
										return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk").toString();
									}
									return null;
								})
								// FIXME: Use -showBuildSettings to get default SDKROOT
								.orElse(xcodeInstallation.getDeveloperDirectory().resolve("Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk").toString()).get();
						case "SOURCE_ROOT":
							return getXcodeProject().get().getLocation().getParent().toString();
						case "PODS_ROOT":
							return getXcodeProject().get().getLocation().getParent().resolve("Pods").toString();
						case "PODS_XCFRAMEWORKS_BUILD_DIR":
							return getDerivedDataPath().dir("Build/Products/" + getConfiguration().get() + "-" + getSdk().get() + "/XCFrameworkIntermediates").get().getAsFile().getAbsolutePath();
						case "PODS_PODFILE_DIR_PATH":
							return getProject().getRootDir().getAbsolutePath();
						case "PRODUCT_MODULE_NAME":
							return getTargetName().get();
						case "PODS_CONFIGURATION_BUILD_DIR":
							return getDerivedDataPath().dir("Build/Products/" + getConfiguration().get() + "-" + getSdk().get()).get().getAsFile().getAbsolutePath();
						case "DERIVED_SOURCES_DIR":
							return getDerivedDataPath().dir("Build/Intermediates.noindex/" + getXcodeProject().get().getName() + ".build/" + getConfiguration().get() + "-" + getSdk().get() + "/" + getTargetName().get() + ".build/DerivedSources").get().getAsFile().getAbsolutePath();
						default:
							return new File(getAllBuildSettings().get().get(name)).getAbsolutePath();
					}
				}
			};

			val context = new BuildSettingsResolveContext(buildSettings);
			val fileRefs = XCLoaders.fileReferences().load(getXcodeProject().get()); // FIXME: Do no use
			return spec.resolve(new XCBuildSpec.ResolveContext() {
				private Path resolveex(PBXReference reference) {
					return fileRefs.get(reference).resolve(context);
				}

				@Override
				public FileCollection inputs(PBXReference reference) {
					final Path path = resolveex(reference); // TODO: capture path
					System.out.println("INPUT " + path);
					return objects.fileCollection().from((Callable<Object>) () -> {
						if (Files.isDirectory(path)) {
							return objects.fileTree().setDir(path);
						} else {
							return path;
						}
					});
					// TODO: decide if path is appropriate to use as input
					// TODO: Also remove paths that are within the global output location...
				}

				@Override
				public FileCollection outputs(PBXReference reference) {
					throw new UnsupportedOperationException();
				}
			});

//			FileCollection inputLocations = objects.fileCollection().from(getTargetReference().get().load().getInputFiles().stream()
//				.filter(it -> it.getType() != XCFileReference.XCFileType.BUILT_PRODUCT)
//				.map(it -> it.resolve(context)).collect(Collectors.toList()));
//			return new XCBuildPlan() {
//				@InputFiles
//				public FileCollection getInputFiles() {
//					return inputLocations;
//				}
//			};
		})));
	}

//	private static final class ProcessTransformerAdapter<OUT, IN> implements Transformer<Provider<OUT>, IN> {
//		private final String displayName;
//		private final ProviderFactory providers;
//		private final Transformer<? extends OUT, ? super IN> delegate;
//
//		private ProcessTransformerAdapter(String displayName, ProviderFactory providers, Transformer<? extends OUT, ? super IN> delegate) {
//			this.displayName = displayName;
//			this.providers = providers;
//			this.delegate = delegate;
//		}
//
//		@Override
//		public Provider<OUT> transform(IN in) {
//			@SuppressWarnings("unchecked") final Provider<OUT> result = (Provider<OUT>) providers.of(ValueSourceTransformerAdapter.class, forParameters(it -> {
//				it.getInput().set(in);
//				it.getDisplayName().set(displayName);
//				it.getTransformer().set(delegate);
//			}));
//			return result;
//		}
//	}
//
//	private static final class Bob implements ProviderUtils.Combiner11<XCFileReferencesLoader.XCFileReferences, XCBuildSpec, List<String>, XcodeInstallation, Directory, Path, Directory, Directory, String, String, String, XCBuildPlan>, Serializable {
//		private final String taskName;
//		private final ObjectFactory objects;
//
//		public Bob(String taskName, ObjectFactory objects) {
//			this.taskName = taskName;
//			this.objects = objects;
//		}
//
//		@Override
//		public XCBuildPlan apply(XCFileReferencesLoader.XCFileReferences fileRefs, XCBuildSpec buildSpec, List<String> allArguments, XcodeInstallation xcodeInstallation, Directory workingDirectory, Path projectLocation, Directory derivedDataPath, Directory outputDirectory, String configuration, String sdk, String targetName) {
//			final XCBuildSettings buildSettings = new XCBuildSettings() {
//				private Map<String, String> allBuildSettings;
//
//				@Override
//				public String get(String name) {
//					switch (name) {
//						case "BUILT_PRODUCT_DIR":
//							// TODO: The following is only an approximation of what the BUILT_PRODUCT_DIR would be, use -showBuildSettings
//							// TODO: Guard against the missing derived data path
//							// TODO: We should map derived data path as a collection of build settings via helper method
//							return derivedDataPath.dir("Build/Products/" + configuration + "-" + sdk).getAsFile().getAbsolutePath();
//						case "DEVELOPER_DIR":
//							// TODO: Use -showBuildSettings to get DEVELOPER_DIR value (or we could guess it)
//							return xcodeInstallation.getDeveloperDirectory().toString();
//						case "SDKROOT":
//							// TODO: Use -showBuildSettings to get SDKROOT value (or we could guess it)
//							switch (sdk.toLowerCase(Locale.ENGLISH)) {
//								case "iphoneos":
//									return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk").toString();
//								case "macosx":
//									return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk").toString();
//								case "iphonesimulator":
//									return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk").toString();
//								default:
//									return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk").toString();
//							}
//						case "SOURCE_ROOT":
//							return projectLocation.getParent().toString();
////								return reference.getLocation().getParent().toString();
//						case "DERIVED_SOURCES_DIR":
//							return derivedDataPath.dir("Build/Intermediates.noindex/.build/" + configuration + "-" + sdk + "/" + targetName + ".build/DerivedSources").getAsFile().getAbsolutePath();
//						default:
//							if (allBuildSettings == null) {
////								val start = System.currentTimeMillis();
////								try {
//								allBuildSettings = CommandLineTool.of("xcodebuild").withArguments(it -> {
//										it.args(allArguments);
//										it.args("-showBuildSettings", "-json");
//									}).newInvocation(it -> {
//										it.withEnvironmentVariables(inherit("PATH").putOrReplace("DEVELOPER_DIR", xcodeInstallation.getDeveloperDirectory()));
//										it.workingDirectory(workingDirectory);
////						ifPresent(getWorkingDirectory(), it::workingDirectory);
////					}).submitTo(execOperations(getExecOperations())).result()
//									}).submitTo(processBuilder()).waitFor()
//									.getStandardOutput().parse(output -> {
//										@SuppressWarnings("unchecked")
//										val parsedOutput = (List<ShowBuildSettingsEntry>) new Gson().fromJson(output, new TypeToken<List<ShowBuildSettingsEntry>>() {
//										}.getType());
//										return parsedOutput.get(0).getBuildSettings();
//									});
////								} finally {
////									System.out.println("build settings... " + /*XcodeTargetExecTask.this +*/ " -- " + (System.currentTimeMillis() - start) + " ms");
////					new Throwable("build settings... " + /*XcodeTargetExecTask.this +*/ " -- " + (System.currentTimeMillis() - start) + " ms").printStackTrace();
////								}
//							}
////							System.out.println("RESOLVING all build settings for " + taskName + " of " + name);
//							return new File(allBuildSettings.get(name)).getAbsolutePath();
//					}
//				}
//			};
//			val context = new BuildSettingsResolveContext(buildSettings);
//			val spec = buildSpec.resolve(new XCBuildSpec.ResolveContext() {
//				private Path resolveex(PBXReference reference) {
//					return fileRefs.get(reference).resolve(context);
////					return fileRefs.get(reference).resolve(new XCFileReference.ResolveContext() {
////						@Override
////						public Path getBuiltProductsDirectory() {
////							return Paths.get(buildSettings.get("BUILT_PRODUCTS_DIR"));
////						}
////
////						@Override
////						public Path get(String name) {
////							return Paths.get(buildSettings.get(name));
////						}
////					});
//				}
//
//				@Override
//				public FileCollection inputs(PBXReference reference) {
//					final Path path = resolveex(reference); // TODO: capture path
//					return objects.fileCollection().from((Callable<Object>) () -> {
//						if (Files.isDirectory(path)) {
//							return objects.fileTree().setDir(path);
//						} else {
//							return path;
//						}
//					});
//					// TODO: decide if path is appropriate to use as input
//					// TODO: Also remove paths that are within the global output location...
//				}
//
//				@Override
//				public FileCollection outputs(PBXReference reference) {
//					final Path path = resolveex(reference);
//					return objects.fileCollection().from((Callable<Object>) () -> {
//						if (path.normalize().startsWith(outputDirectory.getAsFile().toPath().normalize())) {
//							return Collections.emptyList();
//						}
//						if (Files.isDirectory(path)) {
//							return objects.fileTree().setDir(path);
//						} else {
//							return path;
//						}
//					});
//				}
//			});
////			flatten(spec).forEach((k, v) -> System.out.println(k + " ==> " + v));
//			return spec;
//		}
//	}
//
//	private static Map<String, ?> flatten(XCBuildSpec spec) {
//		ImmutableMap.Builder<String, Object> result = ImmutableMap.builder();
//		spec.visit(new XCBuildSpec.Visitor() {
//			private final Deque<String> contexts = new ArrayDeque<>();
//
//			@Override
//			public void visitValue(Object value) {
//				result.put(String.join(".", contexts), value);
//			}
//
//			@Override
//			public void enterContext(String namespace) {
//				contexts.addLast(namespace);
//			}
//
//			@Override
//			public void exitContext() {
//				contexts.removeLast();
//			}
//		});
//		return result.build();
//	}

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
			spec.getIncomingDerivedDataPaths().setFrom(getInputDerivedData());

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
			new PreserveLastModifiedFileSystemOperation(fileOperations::copy).execute(spec -> {
				spec.from(parameters.getIncomingDerivedDataPaths());
				spec.into(parameters.getXcodeDerivedDataPath());
				spec.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
			});

			delegate.run();

			new PreserveLastModifiedFileSystemOperation(fileOperations::sync).execute(spec -> {
				spec.from(parameters.getXcodeDerivedDataPath(), it -> it.include("Build/Products/**/*"));
				spec.into(parameters.getOutgoingDerivedDataPath());
			});
		}

		public interface Parameters {
			ConfigurableFileCollection getIncomingDerivedDataPaths();
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
