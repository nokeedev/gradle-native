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
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelNodeBackedKnownDomainObject;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.*;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.runtime.core.Coordinates;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.utils.Cast;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.util.GUtil;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent.getSdkPlatformPath;
import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public class BaseXCTestTestSuiteComponent extends BaseNativeComponent<DefaultXCTestTestSuiteVariant> implements DependencyAwareComponent<NativeComponentDependencies>, BinaryAwareComponent, TestSuiteComponent {
	@Getter private final Property<GroupId> groupId;
	@Getter private final Property<BaseNativeComponent<?>> testedComponent;
	private final TaskRegistry taskRegistry;
	private final BinaryView<Binary> binaries;
	private final ProviderFactory providers;
	private final ProjectLayout layout;
	@Getter private final Property<String> moduleName;
	@Getter private final Property<String> productBundleIdentifier;
	private final SetProperty<BuildVariantInternal> buildVariants;
	private final Property<DefaultXCTestTestSuiteVariant> developmentVariant;

	public BaseXCTestTestSuiteComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, DomainObjectEventPublisher eventPublisher, BinaryViewFactory binaryViewFactory, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory) {
		super(identifier, DefaultXCTestTestSuiteVariant.class, objects, tasks, eventPublisher, taskRegistry, taskViewFactory);
		this.providers = providers;
		this.layout = layout;
		this.taskRegistry = taskRegistry;
		this.binaries = binaryViewFactory.create(identifier);
		this.groupId = objects.property(GroupId.class);
		this.testedComponent = Cast.uncheckedCastBecauseOfTypeErasure(objects.property(BaseNativeComponent.class));
		this.moduleName = configureDisplayName(objects.property(String.class), "moduleName");
		this.productBundleIdentifier = configureDisplayName(objects.property(String.class), "productBundleIdentifier");
		this.buildVariants = objects.setProperty(BuildVariantInternal.class);
		this.developmentVariant = objects.property(DefaultXCTestTestSuiteVariant.class).convention(providers.provider(new BuildableDevelopmentVariantConvention<>(() -> getVariants().get())));

		getDimensions().add(CoordinateSet.of(Coordinates.of(TargetLinkages.BUNDLE)));
		getDimensions().add(CoordinateSet.of(Coordinates.of(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64())));

		// TODO: Move to extension
		getBuildVariants().convention(getFinalSpace().map(DefaultBuildVariant::fromSpace));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return ModelProperties.getProperty(this, "dependencies").as(DefaultNativeComponentDependencies.class).get();
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return buildVariants;
	}

	@Override
	public Provider<DefaultXCTestTestSuiteVariant> getDevelopmentVariant() {
		return developmentVariant;
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaries;
	}

	@Override
	public VariantView<DefaultXCTestTestSuiteVariant> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}

	@Override
	public VariantCollection<DefaultXCTestTestSuiteVariant> getVariantCollection() {
		throw new UnsupportedOperationException("Use 'variants' property instead.");
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
				binary.getBaseName().convention(GUtil.toCamelCase(project.getName()));
			});
		});
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(DefaultXCTestTestSuiteVariant.class)), (entity, variantIdentifier, variantProjection) -> {
			onEachVariant(new ModelNodeBackedKnownDomainObject<>(ModelType.of(DefaultXCTestTestSuiteVariant.class), entity));
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(DefaultXCTestTestSuiteVariant.class)), (entity, variantIdentifier, variantProjection) -> {
			createBinaries((KnownDomainObject<DefaultXCTestTestSuiteVariant>) Cast.uncheckedCast("", new ModelNodeBackedKnownDomainObject<>(ModelType.of(DefaultXCTestTestSuiteVariant.class), entity)));
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(DefaultXCTestTestSuiteVariant.class)), (entity, variantIdentifier, variantProjection) -> {
			new CreateVariantObjectsLifecycleTaskRule(taskRegistry).accept(new ModelNodeBackedKnownDomainObject<>(ModelType.of(DefaultXCTestTestSuiteVariant.class), entity));
		}));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(DefaultXCTestTestSuiteVariant.class)), (entity, variantIdentifier, variantProjection) -> {
			new CreateVariantAssembleLifecycleTaskRule(taskRegistry).accept(new ModelNodeBackedKnownDomainObject<>(ModelType.of(DefaultXCTestTestSuiteVariant.class), entity));
		}));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}

	@Override
	public TestSuiteComponent testedComponent(Object component) {
		if (component instanceof BaseNativeComponent) {
			testedComponent.set((BaseNativeComponent<?>) component);
		}
		throw new IllegalArgumentException("Unsupported tested component type, expecting a BaseNativeComponent");
	}
}
