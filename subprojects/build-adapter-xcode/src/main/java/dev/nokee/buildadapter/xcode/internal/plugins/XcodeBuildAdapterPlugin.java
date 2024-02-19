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
import dev.nokee.buildadapter.xcode.internal.DefaultGradleProjectPathService;
import dev.nokee.buildadapter.xcode.internal.DefaultXCProjectReferenceToStringer;
import dev.nokee.buildadapter.xcode.internal.GradleBuildLayout;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VFSOverlayAction;
import dev.nokee.buildadapter.xcode.internal.rules.AttachXCTargetToVariantRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCProjectsDiscoveryRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetComponentDiscoveryRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetVariantDiscoveryRule;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeBuildLayoutRule;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeProjectPathRule;
import dev.nokee.model.internal.ModelObject;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.utils.FileSystemLocationUtils;
import dev.nokee.xcode.XCDependenciesLoader;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.RegularFile;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.build.event.BuildEventsListenerRegistry;
import org.gradle.process.CommandLineArgumentProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.util.internal.OutOfDateReasonSpec.because;
import static dev.nokee.utils.BuildServiceUtils.registerBuildServiceIfAbsent;
import static dev.nokee.utils.CallableUtils.ofSerializableCallable;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;
import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;
import static dev.nokee.utils.TransformerUtils.transformEach;

public class XcodeBuildAdapterPlugin implements Plugin<Settings> {
	private static final Logger LOGGER = Logging.getLogger(XcodeBuildAdapterPlugin.class);
	private final ProviderFactory providers;
	private final ObjectFactory objects;
	private final BuildInputService buildInputs;

	@Inject
	public XcodeBuildAdapterPlugin(ProviderFactory providers, ObjectFactory objects) {
		this.providers = providers;
		this.objects = objects;
		this.buildInputs = new BuildInputService(providers);
	}

	@Override
	public void apply(Settings settings) {
		settings.getPluginManager().apply("dev.nokee.model-base");
		settings.getPluginManager().apply("dev.nokee.cocoapods-support");
		settings.getGradle().rootProject(new RedirectProjectBuildDirectoryToRootBuildDirectory());

		val listenerRegistry = ((GradleInternal) settings.getGradle()).getServices().get(BuildEventsListenerRegistry.class);
		val service = registerBuildServiceIfAbsent(settings.getGradle(), XCLoaderService.class);
		listenerRegistry.onTaskCompletion(service);
		service.get(); // hypothetically, make sure the service clear the cache even if configuration phase fails

		final XcodeBuildAdapterExtension extension = settings.getExtensions().create("xcodeBuildAdapter", XcodeBuildAdapterExtension.class);

		new XCProjectsDiscoveryRule(providers, objects).execute(settings);
		extension.getProjects().configureEach(new XcodeProjectPathRule(new DefaultGradleProjectPathService(settings.getSettingsDir().toPath())));
		extension.getProjects().configureEach(new XcodeBuildLayoutRule(GradleBuildLayout.forSettings(settings), providers, objects));

		registerBuildServiceIfAbsent(settings, XcodeDependenciesService.class, parameters -> {
			parameters.getProjectReferences().putAll(providers.provider(() -> {
				return ImmutableMap.copyOf(extension.getProjects().stream().map(it -> new HashMap.SimpleImmutableEntry<>(it.getProjectLocation().get(), it.getProjectPath().get().toString())).collect(Collectors.toList()));
			}));
		});
	}

	private static Provider<String> fromCommandLine(ProviderFactory providers, String name) {
		// TODO: I'm not convince forUseAtConfigurationTime is required here
		return forUseAtConfigurationTime(providers.systemProperty(name)).orElse(forUseAtConfigurationTime(providers.gradleProperty(name)));
	}

	interface XCConfiguration extends Named {}

	public static Action<Project> forXcodeProject(XCProjectReference reference, Action<? super XcodebuildExecTask> action) {
		return project -> {
			project.getPluginManager().apply(ComponentModelBasePlugin.class);

			val buildInputs = new BuildInputService(project.getProviders());

			project.setDescription(new DefaultXCProjectReferenceToStringer(project.getRootDir().toPath()).toString(reference));

			@SuppressWarnings("unchecked")
			final Provider<XcodeDependenciesService> service = (Provider<XcodeDependenciesService>) project.getGradle().getSharedServices().getRegistrations().getByName(XcodeDependenciesService.class.getSimpleName()).getService();

			new XCTargetComponentDiscoveryRule(model(project, registryOf(Component.class)), buildInputs.capture("loads all targets from " + project, (Transformer<Iterable<XCTargetReference>, XCProjectReference> & Serializable) XCLoaders.allTargetsLoader()::load)::transform).execute(project);
			model(project, registryOf(Task.class)).register(ProjectIdentifier.of(project).child(TaskName.of("inspect")), InspectXcodeTask.class)
				.configure(task -> {
					task.getXcodeProject().set(reference);
					task.getXCLoaderService().set(service);
					task.usesService(service);
				});

			model(project, mapOf(Component.class)).configureEach(XCProjectAdapterSpec.class, component -> {
				component.getDimensions().newAxis(XCConfiguration.class).value(component.getConfigurations().map(transformEach(it -> project.getObjects().named(XCConfiguration.class, it))));
			});
			model(project, mapOf(Component.class)).configureEach(XCProjectAdapterSpec.class, new XCTargetVariantDiscoveryRule(buildInputs.capture("loads target configurations", (Transformer<Iterable<String>, XCTargetReference> & Serializable) it -> XCLoaders.targetConfigurationsLoader().load(it))::transform, fromCommandLine(project.getProviders(), "configuration")::getOrNull));
			model(project, mapOf(Component.class)).configureEach(XCProjectAdapterSpec.class, new AttachXCTargetToVariantRule());

			model(project, mapOf(Component.class)).configureEach(XCProjectAdapterSpec.class, component -> {
				model(project, registryOf(Task.class)).register(component.getIdentifier().child(TaskName.lifecycle()), XcodeTargetLifecycleTask.class)
					.configure(task -> {
						final XCTargetReference targetReference = component.getTarget().get();
						task.getConfigurationFlag().convention(project.getProviders().systemProperty("configuration").orElse(project.getProviders().gradleProperty("configuration")).orElse(new DefaultProviderFactory(project.getProviders()).provider(ofSerializableCallable(() -> targetReference.load(XCLoaders.defaultTargetConfigurationLoader())))));
						task.dependsOn((Callable<Object>) task.getConfigurationFlag().map(configuration -> {
							return component.getVariants().filter(it -> it.getConfiguration().equals(configuration)).map(transformEach(XCTargetAdapterSpec::getTargetTask)).flatMap(it -> {
								final SetProperty<XcodeTargetExecTask> result = project.getObjects().setProperty(XcodeTargetExecTask.class);
								it.forEach(result::add);
								return result;
							}).get();
						})::get);
						task.setGroup("Xcode Target");
						task.setDescription(String.format("Builds %s.", String.format("target '%s' of %s", component.getTarget().get().getName(), component)));
					});
			});

			final ModelObject<XCProjectAdapterSpec> projectComponent = model(project, mapOf(Component.class)).register("main", XCProjectAdapterSpec.class);
			projectComponent.configure(it -> it.getProjectLocation().set(reference));

			// Derived Data
			model(project, mapOf(Variant.class)).configureEach(XCTargetAdapterSpec.class, variant -> {
				variant.getDerivedData().configure(bucket -> {
					bucket.getAsConfiguration().attributes(attributes -> {
						attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-derived-data"));
						attributes.attribute(Attribute.of("dev.nokee.xcode.configuration", String.class), variant.getConfiguration());
					});
				});
				variant.getDerivedData().configure(bucket -> {
					bucket.getAsConfiguration().getDependencies().addAllLater(finalizeValueOnRead(project.getObjects().listProperty(Dependency.class).value(service.map(it -> {
							return it.load(variant.getTarget().get()).getDependencies().stream().filter(XCDependenciesLoader.CoordinateDependency.class::isInstance).map(dep -> ((XCDependenciesLoader.CoordinateDependency) dep).getCoordinate()).collect(Collectors.toList());
						}).map(transformEach(asDependency(project)))
					)).orElse(Collections.emptyList()));
				});

				variant.getAssembleDerivedDataDirectoryTask().configure(task -> {
					task.parameters(parameters -> {
						parameters.getIncomingDerivedDataPaths().from(variant.getDerivedData());
						parameters.getXcodeDerivedDataPath().set(project.getLayout().getBuildDirectory().dir("tmp-derived-data/" + variant.getTarget().get().getName() + "-" + variant.getConfiguration()));
					});
				});

				variant.getDerivedDataElements().configure(bucket -> {
					bucket.getAsConfiguration().attributes(attributes -> {
						attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-derived-data"));
						attributes.attribute(Attribute.of("dev.nokee.xcode.configuration", String.class), variant.getConfiguration());
					});
				});
				variant.getDerivedDataElements().configure(bucket -> {
					bucket.getAsConfiguration().outgoing(outgoing -> {
						outgoing.capability("net.nokeedev.xcode:" + project.getName() + "-" + variant.getTarget().get().getName() + ":1.0");
						outgoing.artifact(variant.getTargetTask().flatMap(XcodeTargetExecTask::getOutputDirectory));
					});
				});

				variant.getTargetTask().configure(task -> {
					task.dependsOn(variant.getAssembleDerivedDataDirectoryTask());
					task.getDerivedDataPath().set(variant.getAssembleDerivedDataDirectoryTask().flatMap(it -> it.getParameters().getXcodeDerivedDataPath()));
				});
			});

			// Remote Swift Packages
			model(project, mapOf(Variant.class)).configureEach(XCTargetAdapterSpec.class, variant -> {
				variant.getRemoteSwiftPackages().configure(bucket -> {
					bucket.getAsConfiguration().attributes(attributes -> {
						attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-swift-packages"));
						attributes.attribute(Attribute.of("dev.nokee.xcode.configuration", String.class), variant.getConfiguration());
					});
				});
				variant.getRemoteSwiftPackages().configure(bucket -> {
					bucket.getAsConfiguration().getDependencies().addAllLater(finalizeValueOnRead(project.getObjects().listProperty(Dependency.class).value(service.map(it -> {
							return it.load(variant.getTarget().get()).getDependencies().stream().filter(XCDependenciesLoader.CoordinateDependency.class::isInstance).map(dep -> ((XCDependenciesLoader.CoordinateDependency) dep).getCoordinate()).collect(Collectors.toList());
						}).map(transformEach(asDependency(project)))
					)).orElse(Collections.emptyList()));
				});

				variant.getGenerateRemoteSwiftPackagesTask().configure(task -> {
					task.parameters(parameters -> {
						parameters.getProject().from(reference);
						parameters.getTargetName().set(variant.getTarget().map(XCTargetReference::getName));
						parameters.getManifestFile().set(project.getLayout().getBuildDirectory().file(temporaryDirectoryPath(task) + "/remote-swift-packages.manifest"));
					});
				});

				variant.getRemoteSwiftPackagesElements().configure(bucket -> {
					bucket.getAsConfiguration().attributes(attributes -> {
						attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-swift-packages"));
						attributes.attribute(Attribute.of("dev.nokee.xcode.configuration", String.class), variant.getConfiguration());
					});
				});
				variant.getRemoteSwiftPackagesElements().configure(bucket -> {
					bucket.getAsConfiguration().outgoing(outgoing -> {
						outgoing.capability("net.nokeedev.xcode:" + project.getName() + "-" + variant.getTarget().get().getName() + ":1.0");
						outgoing.artifact(variant.getGenerateRemoteSwiftPackagesTask().flatMap(it -> it.getParameters().getManifestFile()));
					});
				});
			});

			model(project, mapOf(Variant.class)).configureEach(XCTargetAdapterSpec.class, variant -> {
				variant.getVirtualFileSystemOverlays().configure(bucket -> {
					bucket.getAsConfiguration().attributes(attributes -> {
						attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-overlays"));
						attributes.attribute(Attribute.of("dev.nokee.xcode.configuration", String.class), variant.getConfiguration());
					});
				});
				variant.getVirtualFileSystemOverlays().configure(bucket -> {
					bucket.getAsConfiguration().getDependencies().addAllLater(finalizeValueOnRead(project.getObjects().listProperty(Dependency.class).value(service.map(it -> {
							return it.load(variant.getTarget().get()).getDependencies().stream().filter(XCDependenciesLoader.CoordinateDependency.class::isInstance).map(dep -> ((XCDependenciesLoader.CoordinateDependency) dep).getCoordinate()).collect(Collectors.toList());
						}).map(transformEach(asDependency(project)))
					)).orElse(Collections.emptyList()));
				});

				variant.getMergeVirtualFileSystemOverlaysTask().configure(task -> {
					task.parameters(parameters -> {
						parameters.getSources().from(variant.getVirtualFileSystemOverlays());
						parameters.getDerivedDataPath().set(variant.getAssembleDerivedDataDirectoryTask().flatMap(it -> it.getParameters().getXcodeDerivedDataPath()));
						parameters.getOutputFile().set(project.getLayout().getBuildDirectory().file(temporaryDirectoryPath(task) + "/all-products-headers.yaml"));
					});
				});

				variant.getGenerateVirtualSystemOverlaysTask().configure(task -> {
					task.dependsOn(variant.getTargetTask());
					task.parameters(parameters -> {
						parameters.overlays(new VFSOverlayAction(project.getObjects(), variant.getTarget(), variant.getTargetTask().flatMap(it -> it.getBuildSettings().asProvider()), variant.getTargetTask().flatMap(it -> it.getDerivedDataPath().getLocationOnly().map(FileSystemLocationUtils::asPath))));
						parameters.getOutputFile().set(variant.getTarget().flatMap(targetName -> project.getLayout().getBuildDirectory().file(temporaryDirectoryPath(task) + "/" + targetName + ".yaml")));
					});
				});

				variant.getVirtualFileSystemOverlaysElements().configure(bucket -> {
					bucket.getAsConfiguration().attributes(attributes -> {
						attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-overlays"));
						attributes.attribute(Attribute.of("dev.nokee.xcode.configuration", String.class), variant.getConfiguration());
					});
				});
				variant.getVirtualFileSystemOverlaysElements().configure(bucket -> {
					bucket.getAsConfiguration().outgoing(outgoing -> {
						outgoing.capability("net.nokeedev.xcode:" + project.getName() + "-" + variant.getTarget().get().getName() + ":1.0");
						outgoing.artifact(variant.getGenerateVirtualSystemOverlaysTask().flatMap(it -> it.getParameters().getOutputFile()));
					});
				});

				variant.getTargetTask().configure(task -> {
					task.getArguments().add(new CommandLineArgumentProvider() {
						@InputFile
						@PathSensitive(PathSensitivity.ABSOLUTE)
						public Provider<RegularFile> getAllProductsHeaders() {
							return variant.getMergeVirtualFileSystemOverlaysTask().flatMap(it -> it.getParameters().getOutputFile());
						}

						@Override
						public Iterable<String> asArguments() {
							val allProductsHeaders = getAllProductsHeaders().get().getAsFile().getAbsolutePath();
							ImmutableList.Builder<String> builder = ImmutableList.builder();
							builder.add("OTHER_CFLAGS=$(inherited) -ivfsoverlay \"" + allProductsHeaders + "\"");
							builder.add("OTHER_SWIFT_FLAGS=$(inherited) -vfsoverlay \"" + allProductsHeaders + "\"");
							return builder.build();
						}
					});
				});
			});

			model(project, mapOf(Variant.class)).configureEach(XCTargetAdapterSpec.class, variant -> {
				variant.getIsolateTargetTask().configure(task -> {
					task.parameters(parameters -> {
						parameters.getOriginalProject().from(reference);
						parameters.getIsolatedProjectLocation().set(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task) + "/" + reference.getLocation().getFileName().toString()));
						parameters.getIsolations().create(XCTargetIsolationTask.IsolateTargetSpec.class, it -> {
							it.getTargetNameToIsolate().set(variant.getTarget().map(XCTargetReference::getName));
						});
						parameters.getIsolations().create(XCTargetIsolationTask.AddPackageProductDependenciesSpec.class, it -> {
							it.getTargetNameToIsolate().set(variant.getTarget().map(XCTargetReference::getName));
							it.getPackageProductDependencies().addAll(variant.getRemoteSwiftPackages().flatMap(t -> t.getAsConfiguration().getElements()).map(t -> {
								return t.stream().map(FileSystemLocationUtils::asPath).flatMap(a -> {
									try (val inStream = Files.newInputStream(a)) {
										return SerializationUtils.<List<XCSwiftPackageProductDependency>>deserialize(inStream).stream();
									} catch (IOException e) {
										throw new UncheckedIOException(e);
									}
								}).collect(Collectors.toList());
							}));
						});
					});
				});

				variant.getTargetTask().configure(task -> {
					task.dependsOn(variant.getIsolateTargetTask());
					task.getXcodeProject().set(variant.getIsolateTargetTask().flatMap(it -> it.getParameters().getIsolatedProjectLocation()).map(it -> XCProjectReference.of(it.getAsFile().toPath())));
				});
			});

			model(project, mapOf(Variant.class)).configureEach(XCTargetAdapterSpec.class, variant -> {
				variant.getTargetTask().configure(task -> {
					task.getTargetName().set(variant.getTarget().map(XCTargetReference::getName));
					task.getOutputs().upToDateWhen(because(String.format("a shell script build phase of %s has no inputs or outputs defined", reference.ofTarget(variant.getTarget().get().getName())), everyShellScriptBuildPhaseHasDeclaredInputsAndOutputs()));
					task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("derivedData/" + task.getName()));
					task.getConfiguration().set(variant.getConfiguration());
					task.getBuildSettings().from(ImmutableMap.of("SRCROOT", reference.getLocation().getParent().toString()));
					action.execute(task);

					task.setGroup("Xcode Target");
					task.setDescription(String.format("Builds %s.", String.format("target variant '%s:%s' of %s", variant.getTarget().get().getName(), variant.getConfiguration(), project)));
				});
			});
		};
	}

	public static Spec<HasXcodeTargetReference> everyShellScriptBuildPhaseHasDeclaredInputsAndOutputs() {
		return new XcodeTargetExecTaskOutOfDateSpec(XCLoaders.pbxtargetLoader(), new XcodeTargetExecTaskOutOfDateSpec.DefaultPredicate());
	}

	public static Transformer<Dependency, XcodeDependenciesService.Coordinate> asDependency(Project project) {
		return it -> {
			val dep = (ProjectDependency) project.getDependencies().create(project.project(it.projectPath.toString()));
			dep.capabilities(capabilities -> {
				capabilities.requireCapability("net.nokeedev.xcode:" + it.projectName + "-" + it.capabilityName + ":1.0");
			});
			return dep;
		};
	}
}
