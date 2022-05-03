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
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BaseNameUtils;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ConfigurationNamer;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasDevelopmentVariantMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
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
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import groovy.lang.Closure;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
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
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Set;

import static dev.nokee.language.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.core.ModelNodeUtils.instantiate;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;

public class DefaultIosApplicationComponent extends BaseNativeComponent<IosApplication> implements Component
	, DependencyAwareComponent<NativeComponentDependencies>
	, ModelBackedSourceAwareComponentMixIn<ComponentSources, ComponentSources>
	, ModelBackedVariantAwareComponentMixIn<IosApplication>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedHasDevelopmentVariantMixIn<IosApplication>
	, ModelBackedNamedMixIn
{
	@Getter private final Property<GroupId> groupId;
	private final DependencyHandler dependencyHandler;
	private final TaskRegistry taskRegistry;
	private final ProviderFactory providers;
	private final ProjectLayout layout;
	private final ConfigurationContainer configurations;
	@Getter private final Property<String> moduleName;
	private final ModelRegistry registry;

	@Inject
	public DefaultIosApplicationComponent(ComponentIdentifier identifier, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler, TaskRegistry taskRegistry, ModelRegistry registry) {
		super(identifier, objects, taskRegistry, registry);
		this.providers = providers;
		this.layout = layout;
		this.configurations = configurations;
		this.dependencyHandler = dependencyHandler;
		this.groupId = objects.property(GroupId.class);
		this.taskRegistry = taskRegistry;
		this.moduleName = objects.property(String.class).convention(getBaseName());
		this.registry = registry;
	}

	@Override
	public NativeComponentDependencies getDependencies() {
		return ModelProperties.getProperty(this, "dependencies").as(NativeComponentDependencies.class).get();
	}

	@Override
	public void dependencies(Action<? super NativeComponentDependencies> action) {
		action.execute(getDependencies());
	}

	@Override
	public void dependencies(@SuppressWarnings("rawtypes") Closure closure) {
		dependencies(ConfigureUtil.configureUsing(closure));
	}

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return ModelProperties.getProperty(this, "buildVariants").as(set(of(BuildVariant.class))).asProvider();
	}

	@Override
	public Property<IosApplication> getDevelopmentVariant() {
		return ModelBackedHasDevelopmentVariantMixIn.super.getDevelopmentVariant();
	}

	@Override
	@SuppressWarnings("unchecked")
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public VariantView<IosApplication> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}

	protected void onEachVariant(KnownDomainObject<IosApplication> variant) {
		val variantIdentifier = (VariantIdentifier) variant.getIdentifier();
		ConfigurationNamer configurationNamer = ConfigurationNamer.INSTANCE;
		// Create iOS application specific tasks
		Configuration interfaceBuilderToolConfiguration = configurations.create(configurationNamer.determineName(DependencyBucketIdentifier.of(DependencyBucketIdentity.resolvable("interfaceBuilderTool"), variantIdentifier)));
		interfaceBuilderToolConfiguration.getDependencies().add(dependencyHandler.create("dev.nokee.tool:ibtool:latest.release"));
		Provider<CommandLineTool> interfaceBuilderTool = providers.provider(() -> new DescriptorCommandLineTool(interfaceBuilderToolConfiguration.getSingleFile()));

		Provider<CommandLineTool> assetCompilerTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/actool")));
		Provider<CommandLineTool> codeSignatureTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/codesign")));

		String moduleName = BaseNameUtils.from(variantIdentifier).getAsCamelCase();
		Provider<String> identifier = providers.provider(() -> getGroupId().get().get().map(it -> it + "." + moduleName).orElse(moduleName));
		val resources = sourceViewOf(this).named("resources", IosResourceSet.class).get();

		val compileStoryboardTask = taskRegistry.register(TaskIdentifier.of("compileStoryboard", StoryboardCompileTask.class, variantIdentifier), task -> {
			task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/storyboards/compiled/main"));
			task.getModule().set(moduleName);
			task.getSources().from(resources.getAsFileTree().matching(it -> it.include("*.lproj/*.storyboard")));
			task.getInterfaceBuilderTool().set(interfaceBuilderTool);
			task.getInterfaceBuilderTool().finalizeValueOnRead();
		});

		val linkStoryboardTask = taskRegistry.register(TaskIdentifier.of("linkStoryboard", StoryboardLinkTask.class, variantIdentifier), task -> {
			task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/storyboards/linked/main"));
			task.getModule().set(moduleName);
			task.getSources().from(compileStoryboardTask.flatMap(StoryboardCompileTask::getDestinationDirectory));
			task.getInterfaceBuilderTool().set(interfaceBuilderTool);
			task.getInterfaceBuilderTool().finalizeValueOnRead();
		});

		val assetCatalogCompileTaskTask = taskRegistry.register(TaskIdentifier.of("compileAssetCatalog", AssetCatalogCompileTask.class, variantIdentifier), task -> {
			task.getSource().set(new File(resources.getSourceDirectories().getSingleFile(), "Assets.xcassets"));
			task.getIdentifier().set(identifier);
			task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/assets/main"));
			task.getAssetCompilerTool().set(assetCompilerTool);
		});

		val processPropertyListTask = taskRegistry.register(TaskIdentifier.of("processPropertyList", ProcessPropertyListTask.class, variantIdentifier), task -> {
			task.dependsOn(resources.getSourceDirectories());
			task.getIdentifier().set(identifier);
			task.getModule().set(moduleName);
			task.getSources().from(providers.provider(() -> {
				// TODO: I'm not sure we should jump through some hoops for a missing Info.plist.
				//  I'm under the impression that a missing Info.plist file is an error and should be failing in some way.
				// TODO: Regardless of what we do above, the "skip when empty" should be handled by the task itself
				File plistFile = new File(resources.getSourceDirectories().getSingleFile(), "Info.plist");
				if (plistFile.exists()) {
					return ImmutableList.of(plistFile);
				}
				return ImmutableList.of();
			}));
			task.getOutputFile().set(layout.getBuildDirectory().file("ios/Info.plist"));
		});

		val createApplicationBundleTask = taskRegistry.register(TaskIdentifier.of("createApplicationBundle", CreateIosApplicationBundleTask.class, variantIdentifier), task -> {
			Provider<List<? extends Provider<RegularFile>>> binaries = variant.flatMap(application -> application.getBinaries().withType(ExecutableBinaryInternal.class).map(it -> it.getLinkTask().flatMap(LinkExecutable::getLinkedFile)));

			task.getExecutable().set(binaries.flatMap(it -> it.iterator().next())); // TODO: Fix this approximation
			task.getSwiftSupportRequired().convention(false);
			task.getApplicationBundle().set(layout.getBuildDirectory().file("ios/products/main/" + moduleName + "-unsigned.app"));
			task.getSources().from(linkStoryboardTask.flatMap(StoryboardLinkTask::getDestinationDirectory));
			// Linked file is configured in IosApplicationRules
			task.getSources().from(binaries);
			task.getSources().from(assetCatalogCompileTaskTask.flatMap(AssetCatalogCompileTask::getDestinationDirectory));
			task.getSources().from(processPropertyListTask.flatMap(ProcessPropertyListTask::getOutputFile));
		});
		val applicationBundleIdentifier = BinaryIdentifier.of(variantIdentifier, "applicationBundle");
		registry.register(ModelRegistration.builder()
			.withComponent(IsBinary.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(new IdentifierComponent(applicationBundleIdentifier))
			.withComponent(createdUsing(of(IosApplicationBundleInternal.class), () -> {
				return new IosApplicationBundleInternal(createApplicationBundleTask);
			}))
			.build());

		val signApplicationBundleTask = taskRegistry.register(TaskIdentifier.of("signApplicationBundle", SignIosApplicationBundleTask.class, variantIdentifier), task -> {
			task.getUnsignedApplicationBundle().set(createApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/main/" + moduleName + ".app"));
			task.getCodeSignatureTool().set(codeSignatureTool);
		});


		val signedApplicationBundleIdentifier = BinaryIdentifier.of(variantIdentifier, "signedApplicationBundle");
		registry.register(ModelRegistration.builder()
			.withComponent(IsBinary.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(new IdentifierComponent(signedApplicationBundleIdentifier))
			.withComponent(createdUsing(of(SignedIosApplicationBundleInternal.class), () -> {
				return new SignedIosApplicationBundleInternal(signApplicationBundleTask);
			}))
			.build());

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

		val bundle = taskRegistry.register(TaskIdentifier.of(variantIdentifier, "bundle"), task -> {
			task.dependsOn(variant.map(it -> it.getBinaries().withType(SignedIosApplicationBundleInternal.class).get()));
		});
	}

	public void finalizeValue() {
		whenElementKnown(this, this::onEachVariant);
		whenElementKnown(this, this::createBinaries);
		whenElementKnown(this, new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		whenElementKnown(this, new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
	}

	private static void whenElementKnown(Object target, Action<? super KnownDomainObject<IosApplication>> action) {
		instantiate(ModelNodes.of(target), ModelAction.whenElementKnown(ownedBy(ModelNodes.of(target).getId()), IosApplication.class, action));
	}
}
