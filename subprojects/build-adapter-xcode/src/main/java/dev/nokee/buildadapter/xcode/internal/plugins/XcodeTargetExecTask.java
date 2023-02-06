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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildSpec;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolInvocation;
import dev.nokee.utils.DeferredUtils;
import dev.nokee.utils.FileSystemLocationUtils;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.utils.TransformerUtils;
import dev.nokee.utils.internal.ValueSourceTransformerAdapter;
import dev.nokee.xcode.AsciiPropertyListReader;
import dev.nokee.xcode.XCBuildSettings;
import dev.nokee.xcode.XCFileReferencesLoader;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.project.PBXObjectReference;
import dev.nokee.xcode.project.PBXProj;
import dev.nokee.xcode.project.PBXProjReader;
import dev.nokee.xcode.project.PBXProjWriter;
import groovy.lang.Closure;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RelativePath;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.skip;
import static dev.nokee.buildadapter.xcode.internal.plugins.XCBuildSettingsUtils.codeSigningDisabled;
import static dev.nokee.core.exec.CommandLineToolExecutionEngine.execOperations;
import static dev.nokee.core.exec.CommandLineToolExecutionEngine.processBuilder;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toFile;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toStandardStream;
import static dev.nokee.utils.ProviderUtils.disallowChanges;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;
import static dev.nokee.utils.ProviderUtils.forParameters;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;
import static dev.nokee.utils.ProviderUtils.ifPresent;
import static dev.nokee.utils.TransformerUtils.ofSerializableTransformer;

public abstract class XcodeTargetExecTask extends DefaultTask implements XcodebuildExecTask, HasConfigurableXcodeInstallation, HasXcodeTargetReference {
	private final WorkerExecutor workerExecutor;
	private final Provider<XCTargetReference> targetReference;
	private final Provider<XCBuildSpec> buildSpec;

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

//	@Internal // FIXME: Should snapshot the props
//	public abstract MapProperty<String, String> getAllBuildSettings();

	@Override
	@Internal
	public Provider<XCTargetReference> getTargetReference() {
		return targetReference;
	}

	@Nested
	public Provider<XCBuildSpec> getBuildSpec() {
		// FIXME: ensure it's resolved only once!
		return buildSpec;
	}

	public static abstract class AllBuildSettingsValueSource implements ValueSource<Map<String, String>, AllBuildSettingsValueSource.Parameters> {
		public interface Parameters extends ValueSourceParameters {
			ListProperty<String> getAllArguments();
			Property<XcodeInstallation> getXcodeInstallation();
			DirectoryProperty getWorkingDirectory();
		}

		@Inject
		protected abstract ExecOperations getExecOperations();

		@Nullable
		@Override
		public Map<String, String> obtain() {
			val start = System.currentTimeMillis();
			try {
				return CommandLineTool.of("xcodebuild").withArguments(it -> {
						it.args(getParameters().getAllArguments());
						it.args("-showBuildSettings", "-json");
					}).newInvocation(it -> {
						it.withEnvironmentVariables(inherit("PATH").putOrReplace("DEVELOPER_DIR", getParameters().getXcodeInstallation().get().getDeveloperDirectory()));
						ifPresent(getParameters().getWorkingDirectory(), it::workingDirectory);
					}).submitTo(execOperations(getExecOperations())).result()
					.getStandardOutput().parse(output -> {
						@SuppressWarnings("unchecked")
						val parsedOutput = (List<ShowBuildSettingsEntry>) new Gson().fromJson(output, new TypeToken<List<ShowBuildSettingsEntry>>() {
						}.getType());
						return parsedOutput.get(0).getBuildSettings();
					});
			} finally {
				new Throwable("build settings... " + " -- " + (System.currentTimeMillis() - start) + " ms").printStackTrace();
			}
		}
	}

	@Inject
	public XcodeTargetExecTask(WorkerExecutor workerExecutor, ObjectFactory objects, ProviderFactory providers) {
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
//		finalizeValueOnRead(disallowChanges(getAllBuildSettings().value(ProviderUtils.zip(() -> objects.listProperty(Object.class), getAllArguments(), getXcodeInstallation(), getWorkingDirectory(), new TriFunction<List<String>, XcodeInstallation, Directory, Map<String, String>>() {
//			@Override
//			public Map<String, String> apply(List<String> allArguments, XcodeInstallation xcodeInstallation, Directory workingDirectory) {
//				val start = System.currentTimeMillis();
//				try {
//					return CommandLineTool.of("xcodebuild").withArguments(it -> {
//							it.args(allArguments);
//							it.args("-showBuildSettings", "-json");
//						}).newInvocation(it -> {
//							it.withEnvironmentVariables(inherit("PATH").putOrReplace("DEVELOPER_DIR", xcodeInstallation.getDeveloperDirectory()));
//							it.workingDirectory(workingDirectory);
////						ifPresent(getWorkingDirectory(), it::workingDirectory);
////					}).submitTo(execOperations(getExecOperations())).result()
//						}).submitTo(processBuilder()).waitFor()
//						.getStandardOutput().parse(output -> {
//							@SuppressWarnings("unchecked")
//							val parsedOutput = (List<ShowBuildSettingsEntry>) new Gson().fromJson(output, new TypeToken<List<ShowBuildSettingsEntry>>() {
//							}.getType());
//							return parsedOutput.get(0).getBuildSettings();
//						});
//				} finally {
//					System.out.println("build settings... " + /*XcodeTargetExecTask.this +*/ " -- " + (System.currentTimeMillis() - start) + " ms");
////					new Throwable("build settings... " + /*XcodeTargetExecTask.this +*/ " -- " + (System.currentTimeMillis() - start) + " ms").printStackTrace();
//				}
//			}
//		}))));

		this.targetReference = ProviderUtils.zip(() -> objects.listProperty(Object.class), getXcodeProject(), getTargetName(), XCProjectReference::ofTarget);
		this.buildSpec = finalizeValueOnRead(objects.property(XCBuildSpec.class).value(ProviderUtils.zip(() -> objects.listProperty(Object.class), getXcodeProject().flatMap(new ProcessTransformerAdapter<>("load all file references", providers, ofSerializableTransformer(XCLoaders.fileReferencesLoader()::load))), targetReference.flatMap(new ProcessTransformerAdapter<>("load build spec", providers, ofSerializableTransformer(it -> XCLoaders.buildSpecLoader().load(it)))), getAllArguments(), getXcodeInstallation(), getWorkingDirectory(), getXcodeProject().map(XCProjectReference::getLocation), getDerivedDataPath(), getOutputDirectory().getLocationOnly(), getConfiguration(), getSdk(), getTargetName(), new Bob(toString(), objects))));
	}

	private static final class ProcessTransformerAdapter<OUT, IN> implements Transformer<Provider<OUT>, IN> {
		private final String displayName;
		private final ProviderFactory providers;
		private final Transformer<? extends OUT, ? super IN> delegate;

		private ProcessTransformerAdapter(String displayName, ProviderFactory providers, Transformer<? extends OUT, ? super IN> delegate) {
			this.displayName = displayName;
			this.providers = providers;
			this.delegate = delegate;
		}

		@Override
		public Provider<OUT> transform(IN in) {
			@SuppressWarnings("unchecked") final Provider<OUT> result = (Provider<OUT>) providers.of(ValueSourceTransformerAdapter.class, forParameters(it -> {
				it.getInput().set(in);
				it.getDisplayName().set(displayName);
				it.getTransformer().set(delegate);
			}));
			return result;
		}
	}

	private static final class Bob implements ProviderUtils.Combiner11<XCFileReferencesLoader.XCFileReferences, XCBuildSpec, List<String>, XcodeInstallation, Directory, Path, Directory, Directory, String, String, String, XCBuildSpec>, Serializable {
		private final String taskName;
		private final ObjectFactory objects;

		public Bob(String taskName, ObjectFactory objects) {
			this.taskName = taskName;
			this.objects = objects;
		}
		@Override
		public XCBuildSpec apply(XCFileReferencesLoader.XCFileReferences fileRefs, XCBuildSpec buildSpec, List<String> allArguments, XcodeInstallation xcodeInstallation, Directory workingDirectory, Path projectLocation, Directory derivedDataPath, Directory outputDirectory, String configuration, String sdk, String targetName) {
			final XCBuildSettings buildSettings = new XCBuildSettings() {
				private Map<String, String> allBuildSettings;

				@Override
				public String get(String name) {
					switch (name) {
						case "BUILT_PRODUCT_DIR":
							// TODO: The following is only an approximation of what the BUILT_PRODUCT_DIR would be, use -showBuildSettings
							// TODO: Guard against the missing derived data path
							// TODO: We should map derived data path as a collection of build settings via helper method
							return derivedDataPath.dir("Build/Products/" + configuration + "-" + sdk).getAsFile().getAbsolutePath();
						case "DEVELOPER_DIR":
							// TODO: Use -showBuildSettings to get DEVELOPER_DIR value (or we could guess it)
							return xcodeInstallation.getDeveloperDirectory().toString();
						case "SDKROOT":
							// TODO: Use -showBuildSettings to get SDKROOT value (or we could guess it)
							switch (sdk.toLowerCase(Locale.ENGLISH)) {
								case "iphoneos":
									return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk").toString();
								case "macosx":
									return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk").toString();
								case "iphonesimulator":
									return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk").toString();
								default:
									return xcodeInstallation.getDeveloperDirectory().resolve("Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk").toString();
							}
						case "SOURCE_ROOT":
							return projectLocation.getParent().toString();
//								return reference.getLocation().getParent().toString();
						case "DERIVED_SOURCES_DIR":
							return derivedDataPath.dir("Build/Intermediates.noindex/.build/" + configuration + "-" + sdk + "/" + targetName + ".build/DerivedSources").getAsFile().getAbsolutePath();
						default:
							if (allBuildSettings == null) {
//								val start = System.currentTimeMillis();
//								try {
									allBuildSettings = CommandLineTool.of("xcodebuild").withArguments(it -> {
											it.args(allArguments);
											it.args("-showBuildSettings", "-json");
										}).newInvocation(it -> {
											it.withEnvironmentVariables(inherit("PATH").putOrReplace("DEVELOPER_DIR", xcodeInstallation.getDeveloperDirectory()));
											it.workingDirectory(workingDirectory);
//						ifPresent(getWorkingDirectory(), it::workingDirectory);
//					}).submitTo(execOperations(getExecOperations())).result()
										}).submitTo(processBuilder()).waitFor()
										.getStandardOutput().parse(output -> {
											@SuppressWarnings("unchecked")
											val parsedOutput = (List<ShowBuildSettingsEntry>) new Gson().fromJson(output, new TypeToken<List<ShowBuildSettingsEntry>>() {
											}.getType());
											return parsedOutput.get(0).getBuildSettings();
										});
//								} finally {
//									System.out.println("build settings... " + /*XcodeTargetExecTask.this +*/ " -- " + (System.currentTimeMillis() - start) + " ms");
//					new Throwable("build settings... " + /*XcodeTargetExecTask.this +*/ " -- " + (System.currentTimeMillis() - start) + " ms").printStackTrace();
//								}
							}
//							System.out.println("RESOLVING all build settings for " + taskName + " of " + name);
							return new File(allBuildSettings.get(name)).getAbsolutePath();
					}
				}
			};
			val context = new BuildSettingsResolveContext(buildSettings);
			val spec = buildSpec.resolve(new XCBuildSpec.ResolveContext() {
				@Override
				public FileTree resolve(PBXReference reference) {
					throw new UnsupportedOperationException();
				}

				private Path resolveex(PBXReference reference) {
					return fileRefs.get(reference).resolve(context);
//					return fileRefs.get(reference).resolve(new XCFileReference.ResolveContext() {
//						@Override
//						public Path getBuiltProductsDirectory() {
//							return Paths.get(buildSettings.get("BUILT_PRODUCTS_DIR"));
//						}
//
//						@Override
//						public Path get(String name) {
//							return Paths.get(buildSettings.get(name));
//						}
//					});
				}

				@Override
				public FileCollection inputs(PBXReference reference) {
					final Path path = resolveex(reference); // TODO: capture path
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
					final Path path = resolveex(reference);
					return objects.fileCollection().from((Callable<Object>) () -> {
						if (path.normalize().startsWith(outputDirectory.getAsFile().toPath().normalize())) {
							return Collections.emptyList();
						}
						if (Files.isDirectory(path)) {
							return objects.fileTree().setDir(path);
						} else {
							return path;
						}
					});
				}
			});
//			flatten(spec).forEach((k, v) -> System.out.println(k + " ==> " + v));
			return spec;
		}
	}

	private static Map<String, ?> flatten(XCBuildSpec spec) {
		ImmutableMap.Builder<String, Object> result = ImmutableMap.builder();
		spec.visit(new XCBuildSpec.Visitor() {
			private final Deque<String> contexts = new ArrayDeque<>();

			@Override
			public void visitValue(Object value) {
				result.put(String.join(".", contexts), value);
			}

			@Override
			public void enterContext(String namespace) {
				contexts.addLast(namespace);
			}

			@Override
			public void exitContext() {
				contexts.removeLast();
			}
		});
		return result.build();
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
			spec.getIncomingDerivedDataPaths().setFrom(getInputDerivedData());

			spec.getOriginalProjectLocation().set(getXcodeProject().get().getLocation().toFile());
			spec.getIsolatedProjectLocation().set(isolatedProjectLocation);
			spec.getTargetNameToIsolate().set(getTargetName());

			spec.getInvocation().set(invocation);
		});
	}

	interface IsolationParameters {
		DirectoryProperty getOriginalProjectLocation();
		DirectoryProperty getIsolatedProjectLocation();
		Property<String> getTargetNameToIsolate();
	}

	interface ExecutionParameters {
		Property<CommandLineToolInvocation> getInvocation();
	}

	interface DerivedDataParameters {
		ConfigurableFileCollection getIncomingDerivedDataPaths();
		DirectoryProperty getXcodeDerivedDataPath();
		DirectoryProperty getOutgoingDerivedDataPath();
	}

	public static abstract class XcodebuildExec implements WorkAction<XcodebuildExec.Parameters> {
		interface Parameters extends WorkParameters, DerivedDataParameters, ExecutionParameters, IsolationParameters {}

		@Inject
		protected abstract ExecOperations getExecOperations();

		@Inject
		protected abstract FileSystemOperations getFileOperations();

		@Override
		public void execute() {
			derivedDataPath(getParameters(), () -> {
				isolateProject(getParameters(), () -> {
					getParameters().getInvocation().get().submitTo(execOperations(getExecOperations())).result().assertNormalExitValue();
				});
			});
		}

		private void isolateProject(IsolationParameters parameters, Runnable action) {
			val originalProjectLocation = parameters.getOriginalProjectLocation().get().getAsFile().toPath();
			val isolatedProjectLocation = parameters.getIsolatedProjectLocation().get().getAsFile().toPath();
//			preserveLastModified(getFileOperations()::sync, spec -> {
			getFileOperations().sync(spec -> {
				spec.from(originalProjectLocation);
				spec.into(isolatedProjectLocation);
			});

			// FIXME: Should only rewrite the project if it actually changed...
			try {
//				val lastModTime = Files.getLastModifiedTime(isolatedProjectLocation.resolve("project.pbxproj"));
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
//				Files.setLastModifiedTime(isolatedProjectLocation.resolve("project.pbxproj"), lastModTime);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			action.run();
		}

		private void derivedDataPath(DerivedDataParameters parameters, Runnable action) {
			preserveLastModified(getFileOperations()::copy, spec -> {
//			getFileOperations().copy(spec -> {
				spec.from(parameters.getIncomingDerivedDataPaths());
				spec.into(parameters.getXcodeDerivedDataPath());
				spec.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
			});

			action.run();

			preserveLastModified(getFileOperations()::sync, spec -> {
//			getFileOperations().sync(spec -> {
				spec.from(parameters.getXcodeDerivedDataPath(), it -> it.include("Build/Products/**/*"));
				spec.into(parameters.getOutgoingDerivedDataPath());
			});
		}

		private static void preserveLastModified(Consumer<Action<? super CopySpec>> syncCommand, Action<? super CopySpec> action) {
			val synchedFiles = new HashSet<Blah>();
			syncCommand.accept(spec -> {
				val newSpec = new DelegateCopySpec(spec);
				action.execute(newSpec);

				final Path destPath = DeferredUtils.<Path>unpack(it -> {
					val r = DeferredUtils.unpack(it);
					if (r instanceof File) {
						return ((File) r).toPath();
					} else if (r instanceof Path) {
						return (Path) r;
					} else if (r instanceof FileSystemLocation) {
						return ((FileSystemLocation) r).getAsFile().toPath();
					} else {
						return r;
					}
				}).until(Path.class).execute(newSpec.destPath);

				spec.eachFile(it -> {
					RelativePath path = it.getRelativePath();
					do {
						synchedFiles.add(new Blah(it.getFile().toPath(), destPath.resolve(path.getPathString())));
					} while ((path = path.getParent()) != null);
				});
			});

			synchedFiles.forEach(Blah::restore);
		}

		@EqualsAndHashCode
		private static final class Blah {
			private final Path sourcePath;
			private final Path destinationPath;

			private Blah(Path sourcePath, Path destinationPath) {
				this.sourcePath = sourcePath;
				this.destinationPath = destinationPath;
			}

			public void restore() {
				if (Files.exists(destinationPath)) {
					try {
						Files.setLastModifiedTime(destinationPath, Files.getLastModifiedTime(sourcePath));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		@SuppressWarnings("rawtypes")
		private static final class DelegateCopySpec implements CopySpec {
			@Delegate private final CopySpec delegate;
			private Object destPath;

			private DelegateCopySpec(CopySpec delegate) {
				this.delegate = delegate;
			}

			@Override
			public CopySpec into(Object destPath) {
				this.destPath = destPath;
				return delegate.into(destPath);
			}

			@Override
			public CopySpec into(Object destPath, Closure configureClosure) {
				this.destPath = destPath;
				return delegate.into(destPath, configureClosure);
			}

			@Override
			public CopySpec into(Object destPath, Action<? super CopySpec> copySpec) {
				this.destPath = destPath;
				return delegate.into(destPath, copySpec);
			}
		}
	}

	private List<String> asFlags(Map<String, String> buildSettings) {
		val builder = ImmutableList.<String>builder();
		buildSettings.forEach((k, v) -> builder.add(k + "=" + v));
		return builder.build();
	}
}
