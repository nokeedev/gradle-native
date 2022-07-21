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
package dev.nokee.testing.xctest.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantObjectsLifecycleTaskRule;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.utils.Cast;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.util.GUtil;

import java.util.Set;

import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.core.ModelNodeUtils.instantiate;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent.getSdkPlatformPath;
import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public abstract class BaseXCTestTestSuiteComponent extends BaseNativeComponent<DefaultXCTestTestSuiteVariant> implements TestSuiteComponent
	, DependencyAwareComponent<NativeComponentDependencies>
	, BinaryAwareComponent
{
	@Getter private final Property<GroupId> groupId;
	@Getter private final Property<BaseNativeComponent<?>> testedComponent;
	private final TaskRegistry taskRegistry;
	private final ProviderFactory providers;
	private final ProjectLayout layout;
	@Getter private final Property<String> moduleName;
	@Getter private final Property<String> productBundleIdentifier;

	public BaseXCTestTestSuiteComponent(ComponentIdentifier identifier, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, TaskRegistry taskRegistry, ModelRegistry registry) {
		super(identifier, objects, taskRegistry, registry, providers);
		this.providers = providers;
		this.layout = layout;
		this.taskRegistry = taskRegistry;
		this.groupId = objects.property(GroupId.class);
		this.testedComponent = Cast.uncheckedCastBecauseOfTypeErasure(objects.property(BaseNativeComponent.class));
		this.moduleName = configureDisplayName(objects.property(String.class), "moduleName");
		this.productBundleIdentifier = configureDisplayName(objects.property(String.class), "productBundleIdentifier");
	}

	@Override
	public NativeComponentDependencies getDependencies() {
		return ModelProperties.getProperty(this, "dependencies").as(NativeComponentDependencies.class).get();
	}

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return ModelProperties.getProperty(this, "buildVariants").as(set(of(BuildVariant.class))).asProvider();
	}

	@Override
	@SuppressWarnings("unchecked")
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public VariantView<DefaultXCTestTestSuiteVariant> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}

	protected void onEachVariant(KnownDomainObject<DefaultXCTestTestSuiteVariant> variant) {
		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				Provider<String> moduleName = getTestedComponent().flatMap(BaseComponent::getBaseName);
				binary.getCompileTasks().configureEach(SourceCompile.class, task -> {
					task.getCompilerArgs().addAll(providers.provider(() -> ImmutableList.of("-target", "x86_64-apple-ios13.2-simulator", "-F", getSdkPath() + "/System/Library/Frameworks", "-iframework", getSdkPlatformPath() + "/Developer/Library/Frameworks")));
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
					task.getLinkerArgs().addAll(providers.provider(() -> ImmutableList.of(
						"-Xlinker", "-rpath", "-Xlinker", "@executable_path/Frameworks",
						"-Xlinker", "-rpath", "-Xlinker", "@loader_path/Frameworks",
						"-Xlinker", "-export_dynamic",
						"-Xlinker", "-no_deduplicate",
						"-Xlinker", "-objc_abi_version", "-Xlinker", "2",
//					"-Xlinker", "-sectcreate", "-Xlinker", "__TEXT", "-Xlinker", "__entitlements", "-Xlinker", createEntitlementTask.get().outputFile.get().asFile.absolutePath
						"-fobjc-arc", "-fobjc-link-runtime",
						"-bundle_loader", layout.getBuildDirectory().file("exes/main/" + moduleName.get()).get().getAsFile().getAbsolutePath(),
						"-L" + getSdkPlatformPath() + "/Developer/usr/lib", "-F" + getSdkPlatformPath() + "/Developer/Library/Frameworks", "-framework", "XCTest")));
					// TODO: -lobjc should probably only be present for binary compiling/linking objc binaries
				});
			});
		});
	}

	public void finalizeExtension(Project project) {
		// TODO: Use component binary view instead once finish cleanup, it remove one level of indirection
		getVariants().configureEach(variant -> {
			variant.getBinaries().configureEach(BaseNativeBinary.class, binary -> {
				((HasBaseName) binary).getBaseName().convention(GUtil.toCamelCase(project.getName()));
			});
		});
		whenElementKnown(this.getNode(), this::onEachVariant);
		whenElementKnown(this.getNode(), new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);

		whenElementKnown(this.getNode(), new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);
	}

	private static void whenElementKnown(ModelNode target, Action<? super KnownDomainObject<DefaultXCTestTestSuiteVariant>> action) {
		instantiate(target, ModelAction.whenElementKnown(ownedBy(target.getId()), DefaultXCTestTestSuiteVariant.class, action));
	}

	@Override
	public TestSuiteComponent testedComponent(Object component) {
		if (component instanceof BaseNativeComponent) {
			testedComponent.set((BaseNativeComponent<?>) component);
		}
		throw new IllegalArgumentException("Unsupported tested component type, expecting a BaseNativeComponent");
	}
}
