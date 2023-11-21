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
package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MoreCollectors;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.internal.Factory;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BaseNameUtils;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.tasks.internal.AssetCatalogCompileTask;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.ios.tasks.internal.ProcessPropertyListTask;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import dev.nokee.platform.ios.tasks.internal.StoryboardCompileTask;
import dev.nokee.platform.ios.tasks.internal.StoryboardLinkTask;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.nativeplatform.toolchain.Swiftc;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Set;

import static dev.nokee.language.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.utils.FileCollectionUtils.sourceDirectories;

public /*final*/ abstract class DefaultIosApplicationComponent extends BaseNativeComponent<IosApplication> implements Component
	, NativeSourcesAware
	, ExtensionAwareMixIn
	, DependencyAwareComponentMixIn<NativeComponentDependencies, DefaultNativeComponentDependencies>
	, SourceAwareComponentMixIn<ComponentSources, ComponentSources>
	, VariantAwareComponentMixIn<IosApplication>
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, HasDevelopmentVariant<IosApplication>
{
	@Getter private final Property<GroupId> groupId;
	private final DependencyHandler dependencyHandler;
	private final ProviderFactory providers;
	private final ProjectLayout layout;
	private final ConfigurationContainer configurations;
	@Getter private final Property<String> moduleName;
	private final ModelObjectRegistry<Task> taskRegistry;
	private final ModelObjectRegistry<Artifact> artifactRegistry;

	@Inject
	public DefaultIosApplicationComponent(ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler, Factory<ComponentSources> sourcesFactory, ModelObjectRegistry<Task> taskRegistry, ModelObjectRegistry<Artifact> artifactRegistry) {
		getExtensions().add("sources", sourcesFactory.create());
		this.artifactRegistry = artifactRegistry;
		this.taskRegistry = taskRegistry;
		this.providers = providers;
		this.layout = layout;
		this.configurations = configurations;
		this.dependencyHandler = dependencyHandler;
		this.groupId = objects.property(GroupId.class);
		this.moduleName = objects.property(String.class).convention(getBaseName());
	}

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return VariantAwareComponentMixIn.super.getBuildVariants();
	}

	@Override
	public abstract Property<IosApplication> getDevelopmentVariant();

	@Override
	public VariantView<IosApplication> getVariants() {
		return VariantAwareComponentMixIn.super.getVariants();
	}

	@SuppressWarnings("unchecked")
	protected void onEachVariant(KnownDomainObject<IosApplication> variant) {
		val variantIdentifier = (VariantIdentifier) variant.getIdentifier();
		// Create iOS application specific tasks
		Configuration interfaceBuilderToolConfiguration = configurations.create(variantIdentifier.getFullName() + "InterfaceBuilderTool");
		interfaceBuilderToolConfiguration.getDependencies().add(dependencyHandler.create("dev.nokee.tool:ibtool:latest.release"));
		Provider<CommandLineTool> interfaceBuilderTool = providers.provider(() -> new DescriptorCommandLineTool(interfaceBuilderToolConfiguration.getSingleFile()));

		Provider<CommandLineTool> assetCompilerTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/actool")));
		Provider<CommandLineTool> codeSignatureTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/codesign")));

		String moduleName = BaseNameUtils.from(variantIdentifier).getAsCamelCase();
		Provider<String> identifier = providers.provider(() -> getGroupId().get().get().map(it -> it + "." + moduleName).orElse(moduleName));
		val resources = sourceViewOf(this).named("resources", IosResourceSet.class).get();

		val compileStoryboardTask = taskRegistry.register(variantIdentifier.child(TaskName.of("compileStoryboard")), StoryboardCompileTask.class).configure(task -> {
			task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/storyboards/compiled/main"));
			task.getModule().set(moduleName);
			task.getSources().from(resources.getSources().getAsFileTree().matching(it -> it.include("*.lproj/*.storyboard")));
			task.getInterfaceBuilderTool().set(interfaceBuilderTool);
			task.getInterfaceBuilderTool().finalizeValueOnRead();
		}).asProvider();

		val linkStoryboardTask = taskRegistry.register(variantIdentifier.child(TaskName.of("linkStoryboard")), StoryboardLinkTask.class).configure(task -> {
			task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/storyboards/linked/main"));
			task.getModule().set(moduleName);
			task.getSources().from(compileStoryboardTask.flatMap(StoryboardCompileTask::getDestinationDirectory));
			task.getInterfaceBuilderTool().set(interfaceBuilderTool);
			task.getInterfaceBuilderTool().finalizeValueOnRead();
		}).asProvider();

		val assetCatalogCompileTaskTask = taskRegistry.register(variantIdentifier.child(TaskName.of("compileAssetCatalog")), AssetCatalogCompileTask.class).configure(task -> {
			task.getSource().set(new File(sourceDirectories(resources.getSources()).map(it -> it.stream().collect(MoreCollectors.onlyElement())).get(), "Assets.xcassets"));
			task.getIdentifier().set(identifier);
			task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/assets/main"));
			task.getAssetCompilerTool().set(assetCompilerTool);
		}).asProvider();

		val processPropertyListTask = taskRegistry.register(variantIdentifier.child(TaskName.of("processPropertyList")), ProcessPropertyListTask.class).configure(task -> {
			task.dependsOn(resources.getSources());
			task.getIdentifier().set(identifier);
			task.getModule().set(moduleName);
			task.getSources().from(providers.provider(() -> {
				// TODO: I'm not sure we should jump through some hoops for a missing Info.plist.
				//  I'm under the impression that a missing Info.plist file is an error and should be failing in some way.
				// TODO: Regardless of what we do above, the "skip when empty" should be handled by the task itself
				File plistFile = new File(sourceDirectories(resources.getSources()).map(it -> it.stream().collect(MoreCollectors.onlyElement())).get(), "Info.plist");
				if (plistFile.exists()) {
					return ImmutableList.of(plistFile);
				}
				return ImmutableList.of();
			}));
			task.getOutputFile().set(layout.getBuildDirectory().file("ios/Info.plist"));
		}).asProvider();

		val createApplicationBundleTask = taskRegistry.register(variantIdentifier.child(TaskName.of("createApplicationBundle")), CreateIosApplicationBundleTask.class).configure(task -> {
			Provider<List<? extends Provider<RegularFile>>> binaries = variant.flatMap(application -> application.getBinaries().withType(ExecutableBinaryInternal.class).map(it -> it.getLinkTask().flatMap(LinkExecutable::getLinkedFile)));

			task.getExecutable().set(binaries.flatMap(it -> it.iterator().next())); // TODO: Fix this approximation
			task.getSwiftSupportRequired().convention(false);
			task.getApplicationBundle().set(layout.getBuildDirectory().file("ios/products/main/" + moduleName + "-unsigned.app"));
			task.getSources().from(linkStoryboardTask.flatMap(StoryboardLinkTask::getDestinationDirectory));
			// Linked file is configured in IosApplicationRules
			task.getSources().from(binaries);
			task.getSources().from(assetCatalogCompileTaskTask.flatMap(AssetCatalogCompileTask::getDestinationDirectory));
			task.getSources().from(processPropertyListTask.flatMap(ProcessPropertyListTask::getOutputFile));
		}).asProvider();
		val applicationBundleIdentifier = variantIdentifier.child("applicationBundle");
		// TODO: register `createApplicationBundleTask` inside IosApplicationBundleInternal
		artifactRegistry.register(applicationBundleIdentifier, IosApplicationBundleInternal.class);

		val signApplicationBundleTask = taskRegistry.register(variantIdentifier.child(TaskName.of("signApplicationBundle")), SignIosApplicationBundleTask.class).configure(task -> {
			task.getUnsignedApplicationBundle().set(createApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/main/" + moduleName + ".app"));
			task.getCodeSignatureTool().set(codeSignatureTool);
		}).asProvider();


		val signedApplicationBundleIdentifier = variantIdentifier.child("signedApplicationBundle");
		// TODO: register `signApplicationBundleTask` inside SignedIosApplicationBundleInternal
		artifactRegistry.register(signedApplicationBundleIdentifier, SignedIosApplicationBundleInternal.class);

		variant.configure(application -> {
			application.getBinaries().configureEach(ExecutableBinary.class, binary -> {
				binary.getCompileTasks().configureEach(SourceCompile.class, task -> {
					task.getCompilerArgs().addAll(providers.provider(() -> ImmutableList.of("-target", "x86_64-apple-ios13.2-simulator", "-F", getSdkPath() + "/System/Library/Frameworks")));
					task.getCompilerArgs().addAll(task.getToolChain().map(toolChain -> {
						if (toolChain instanceof Swiftc) {
							return ImmutableList.of("-sdk", getSdkPath());
						}
						return ImmutableList.of("-isysroot", getSdkPath());
					}));
					if (task instanceof ObjectiveCCompile) {
						task.getCompilerArgs().addAll("-fobjc-arc");
					}
				});
				binary.getLinkTask().configure(task -> {
					task.getLinkerArgs().addAll(providers.provider(() -> ImmutableList.of("-target", "x86_64-apple-ios13.2-simulator")));
					task.getLinkerArgs().addAll(task.getToolChain().map(toolChain -> {
						if (toolChain instanceof Swiftc) {
							return ImmutableList.of("-sdk", getSdkPath());
						}
						return ImmutableList.of("-isysroot", getSdkPath());
					}));
					task.getLinkerArgs().addAll("-Xlinker", "-rpath", "-Xlinker", "@executable_path/Frameworks",
						"-Xlinker", "-export_dynamic",
						"-Xlinker", "-no_deduplicate",
						"-Xlinker", "-objc_abi_version", "-Xlinker", "2",
//					"-Xlinker", "-sectcreate", "-Xlinker", "__TEXT", "-Xlinker", "__entitlements", "-Xlinker", createEntitlementTask.get().outputFile.get().asFile.absolutePath
						"-lobjc", "-framework", "UIKit", "-framework", "Foundation");
					// TODO: -lobjc should probably only be present for binary compiling/linking objc binaries
				});
			});
		});

		val bundle = taskRegistry.register(variantIdentifier.child(TaskName.of("bundle")), Task.class).configure(task -> {
			task.dependsOn(variant.map(it -> it.getBinaries().withType(SignedIosApplicationBundleInternal.class).get()));
		});
	}

	public void finalizeValue() {
		//TODO: whenElementKnown(this, this::onEachVariant);
	}
}
