package dev.nokee.testing.xctest.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactories;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantObjectsLifecycleTaskRule;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.utils.Cast;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.util.GUtil;

import javax.inject.Inject;

import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent.getSdkPlatformPath;

public class BaseXCTestTestSuiteComponent extends BaseNativeComponent<DefaultXCTestTestSuiteVariant> implements DependencyAwareComponent<NativeComponentDependencies>, BinaryAwareComponent {
	private final DefaultNativeComponentDependencies dependencies;
	@Getter private final Property<GroupId> groupId;
	@Getter private final Property<BaseNativeComponent<?>> testedComponent;
	private final TaskRegistry taskRegistry;
	private final XCTestTestSuiteComponentVariants componentVariants;
	private final BinaryView<Binary> binaries;
	private final ProviderFactory providers;
	private final ProjectLayout layout;

	@Inject
	public BaseXCTestTestSuiteComponent(ComponentIdentifier<?> identifier, NamingScheme names, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler) {
		super(identifier, names, DefaultXCTestTestSuiteVariant.class, objects, providers, tasks, layout, configurations);
		this.providers = providers;
		this.layout = layout;
		this.componentVariants = new XCTestTestSuiteComponentVariants(objects, this, dependencyHandler, configurations);
		this.binaries = Cast.uncheckedCastBecauseOfTypeErasure(objects.newInstance(VariantAwareBinaryView.class, new DefaultMappingView<>(getVariantCollection().getAsView(DefaultXCTestTestSuiteVariant.class), Variant::getBinaries)));
		this.taskRegistry = new TaskRegistryImpl(tasks);
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(configurations), names::getConfigurationName), new DefaultDependencyFactory(dependencyHandler))));
		this.dependencies = objects.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
		this.groupId = objects.property(GroupId.class);
		this.testedComponent = Cast.uncheckedCastBecauseOfTypeErasure(objects.property(BaseNativeComponent.class));
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));

		// TODO: Move to extension
		getBuildVariants().convention(providers.provider(this::createBuildVariants));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		getDimensions().disallowChanges(); // Let's disallow changing them for now.
	}

	private Iterable<BuildVariantInternal> createBuildVariants() {
		return ImmutableList.of(DefaultBuildVariant.of(DefaultBinaryLinkage.BUNDLE, DefaultOperatingSystemFamily.forName("ios"), DefaultMachineArchitecture.X86_64));
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return componentVariants.getBuildVariants();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaries;
	}

	@Override
	public VariantCollection<DefaultXCTestTestSuiteVariant> getVariantCollection() {
		return componentVariants.getVariantCollection();
	}

	protected void onEachVariant(KnownVariant<DefaultXCTestTestSuiteVariant> variant) {
		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				Provider<String> moduleName = getTestedComponent().map(it -> it.getNames().getBaseName().getAsCamelCase());
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
		getVariantCollection().whenElementKnown(this::onEachVariant);
		getVariantCollection().whenElementKnown(this::createBinaries);
		getVariantCollection().whenElementKnown(new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		getVariantCollection().whenElementKnown(new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);

		componentVariants.calculateVariants();

		getVariantCollection().disallowChanges();
	}
}
