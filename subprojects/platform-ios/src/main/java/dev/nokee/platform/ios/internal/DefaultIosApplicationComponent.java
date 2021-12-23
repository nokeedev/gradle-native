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
import dev.nokee.model.internal.DomainObjectCreated;
import dev.nokee.model.internal.DomainObjectDiscovered;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.tasks.internal.*;
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
import org.gradle.api.tasks.TaskContainer;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Set;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;

public class DefaultIosApplicationComponent extends BaseNativeComponent<DefaultIosApplicationVariant> implements Component
	, DependencyAwareComponent<NativeComponentDependencies>
	, ModelBackedSourceAwareComponentMixIn<ComponentSources>
	, ModelBackedVariantAwareComponentMixIn<DefaultIosApplicationVariant>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedHasDevelopmentVariantMixIn<DefaultIosApplicationVariant>
	, ModelBackedNamedMixIn
{
	@Getter private final Property<GroupId> groupId;
	private final DependencyHandler dependencyHandler;
	private final DomainObjectEventPublisher eventPublisher;
	private final TaskRegistry taskRegistry;
	private final ObjectFactory objects;
	private final ProviderFactory providers;
	private final ProjectLayout layout;
	private final ConfigurationContainer configurations;
	@Getter private final Property<String> moduleName;

	@Inject
	public DefaultIosApplicationComponent(ComponentIdentifier identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler, DomainObjectEventPublisher eventPublisher, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory) {
		super(identifier, DefaultIosApplicationVariant.class, objects, tasks, eventPublisher, taskRegistry, taskViewFactory);
		this.objects = objects;
		this.providers = providers;
		this.layout = layout;
		this.configurations = configurations;
		this.dependencyHandler = dependencyHandler;
		this.eventPublisher = eventPublisher;
		this.groupId = objects.property(GroupId.class);
		this.taskRegistry = taskRegistry;
		this.moduleName = objects.property(String.class).convention(getBaseName());
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
		return ModelProperties.getProperty(this, "buildVariants").as(Provider.class).get();
	}

	@Override
	public Property<DefaultIosApplicationVariant> getDevelopmentVariant() {
		return ModelProperties.getProperty(this, "developmentVariant").as(Property.class).get();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	public VariantView<DefaultIosApplicationVariant> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}

	protected void onEachVariant(KnownDomainObject<DefaultIosApplicationVariant> variant) {
		val variantIdentifier = (VariantIdentifier<?>) variant.getIdentifier();
		ConfigurationNamer configurationNamer = ConfigurationNamer.INSTANCE;
		TaskNamer namer = TaskNamer.INSTANCE;
		// Create iOS application specific tasks
		Configuration interfaceBuilderToolConfiguration = configurations.create(configurationNamer.determineName(DependencyBucketIdentifier.of(DependencyBucketIdentity.resolvable("interfaceBuilderTool"), variantIdentifier)));
		interfaceBuilderToolConfiguration.getDependencies().add(dependencyHandler.create("dev.nokee.tool:ibtool:latest.release"));
		Provider<CommandLineTool> interfaceBuilderTool = providers.provider(() -> new DescriptorCommandLineTool(interfaceBuilderToolConfiguration.getSingleFile()));

		Provider<CommandLineTool> assetCompilerTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/actool")));
		Provider<CommandLineTool> codeSignatureTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/codesign")));

		String moduleName = BaseNameUtils.from(variantIdentifier).getAsCamelCase();
		Provider<String> identifier = providers.provider(() -> getGroupId().get().get().map(it -> it + "." + moduleName).orElse(moduleName));
		val resources = sourceViewOf(this).named("resources", IosResourceSet.class).get();

		val compileStoryboardTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, "compileStoryboard")), StoryboardCompileTask.class, task -> {
			task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/storyboards/compiled/main"));
			task.getModule().set(moduleName);
			task.getSources().from(resources.getAsFileTree().matching(it -> it.include("*.lproj/*.storyboard")));
			task.getInterfaceBuilderTool().set(interfaceBuilderTool);
			task.getInterfaceBuilderTool().finalizeValueOnRead();
		});

		val linkStoryboardTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, "linkStoryboard")), StoryboardLinkTask.class, task -> {
			task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/storyboards/linked/main"));
			task.getModule().set(moduleName);
			task.getSources().from(compileStoryboardTask.flatMap(StoryboardCompileTask::getDestinationDirectory));
			task.getInterfaceBuilderTool().set(interfaceBuilderTool);
			task.getInterfaceBuilderTool().finalizeValueOnRead();
		});

		val assetCatalogCompileTaskTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, "compileAssetCatalog")), AssetCatalogCompileTask.class, task -> {
			task.getSource().set(new File(resources.getSourceDirectories().getSingleFile(), "Assets.xcassets"));
			task.getIdentifier().set(identifier);
			task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/assets/main"));
			task.getAssetCompilerTool().set(assetCompilerTool);
		});

		val processPropertyListTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, "processPropertyList")), ProcessPropertyListTask.class, task -> {
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

		val createApplicationBundleTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, "createApplicationBundle")), CreateIosApplicationBundleTask.class, task -> {
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
		val applicationBundleIdentifier = BinaryIdentifier.of(BinaryName.of("applicationBundle"), IosApplicationBundleInternal.class, variantIdentifier);
		eventPublisher.publish(new DomainObjectDiscovered<>(applicationBundleIdentifier));
		val applicationBundle = new IosApplicationBundleInternal(createApplicationBundleTask);
		eventPublisher.publish(new DomainObjectCreated<>(applicationBundleIdentifier, applicationBundle));

		val signApplicationBundleTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, "signApplicationBundle")), SignIosApplicationBundleTask.class, task -> {
			task.getUnsignedApplicationBundle().set(createApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/main/" + moduleName + ".app"));
			task.getCodeSignatureTool().set(codeSignatureTool);
		});


		val signedApplicationBundleIdentifier = BinaryIdentifier.of(BinaryName.of("signedApplicationBundle"), SignedIosApplicationBundleInternal.class, variantIdentifier);
		eventPublisher.publish(new DomainObjectDiscovered<>(signedApplicationBundleIdentifier));
		val signedApplicationBundle = new SignedIosApplicationBundleInternal(signApplicationBundleTask);
		eventPublisher.publish(new DomainObjectCreated<>(signedApplicationBundleIdentifier, signedApplicationBundle));

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

		val bundle = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, "bundle")), task -> {
			task.dependsOn(variant.map(it -> it.getBinaries().withType(SignedIosApplicationBundleInternal.class).get()));
		});
	}

	public void finalizeValue() {
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(DefaultIosApplicationVariant.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			onEachVariant(knownVariant);
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(DefaultIosApplicationVariant.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			createBinaries(knownVariant);
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(DefaultIosApplicationVariant.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			new CreateVariantObjectsLifecycleTaskRule(taskRegistry).accept(knownVariant);
		}));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(DefaultIosApplicationVariant.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			new CreateVariantAssembleLifecycleTaskRule(taskRegistry).accept(knownVariant);
		}));
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}
}
