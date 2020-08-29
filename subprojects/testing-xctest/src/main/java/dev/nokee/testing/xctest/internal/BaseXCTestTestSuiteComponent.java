package dev.nokee.testing.xctest.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.ios.internal.IosApplicationOutgoingDependencies;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeDependenciesBuilderFactory;
import dev.nokee.platform.nativebase.internal.dependencies.NativeComponentDependenciesFactory;
import dev.nokee.platform.nativebase.internal.dependencies.NativeComponentDependenciesInternal;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.utils.Cast;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.util.GUtil;

import javax.inject.Inject;

import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.testing.xctest.internal.UnitTestXCTestTestSuiteComponentImpl.getSdkPlatformPath;

public class BaseXCTestTestSuiteComponent extends BaseNativeComponent<DefaultXCTestTestSuiteVariant> implements DependencyAwareComponent<NativeComponentDependencies>, BinaryAwareComponent {
	private final NativeComponentDependenciesInternal dependencies;
	@Getter private final Property<GroupId> groupId;
	@Getter private final Property<BaseNativeComponent<?>> testedComponent;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;
	private final DefaultNativeDependenciesBuilderFactory dependenciesBuilderFactory;

	@Inject
	public BaseXCTestTestSuiteComponent(ComponentIdentifier identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler, NativeComponentDependenciesFactory dependenciesFactory, DefaultNativeDependenciesBuilderFactory dependenciesBuilderFactory) {
		super(NamingScheme.fromIdentifier(identifier, identifier.getProjectIdentifier().getName()), DefaultXCTestTestSuiteVariant.class, objects, providers, tasks, layout, configurations);
		this.dependencyHandler = dependencyHandler;
		this.dependenciesBuilderFactory = dependenciesBuilderFactory;
		this.dependencies = dependenciesFactory.create(identifier);
		this.groupId = objects.property(GroupId.class);
		this.testedComponent = Cast.uncheckedCastBecauseOfTypeErasure(objects.property(BaseNativeComponent.class));
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));

		// TODO: Move to extension
		getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		getDimensions().disallowChanges(); // Let's disallow changing them for now.
	}

	private Iterable<BuildVariantInternal> createBuildVariants() {
		return ImmutableList.of(DefaultBuildVariant.of(DefaultBinaryLinkage.BUNDLE, DefaultOperatingSystemFamily.forName("ios"), DefaultMachineArchitecture.X86_64));
	}

	@Override
	public NativeComponentDependenciesInternal getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super NativeComponentDependencies> action) {
		action.execute(dependencies);
	}

	@Override
	protected DefaultXCTestTestSuiteVariant createVariant(String name, BuildVariantInternal buildVariant, VariantComponentDependencies<?> variantDependencies) {
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultXCTestTestSuiteVariant result = getObjects().newInstance(DefaultXCTestTestSuiteVariant.class, name, names, buildVariant, variantDependencies);
		return result;
	}

	@Override
	protected VariantComponentDependencies<NativeComponentDependencies> newDependencies(NamingScheme names, BuildVariantInternal buildVariant) {
		val identifier = new VariantIdentifier(names.getUnambiguousDimensionsAsString(), new ComponentIdentifier(names.getComponentName(), new ProjectIdentifier("")));
		val builder = dependenciesBuilderFactory.create().withParentDependencies(getDependencies()).withIdentifier(identifier);

		boolean hasSwift = !getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		if (hasSwift) {
			builder.withSwiftModules();
		} else {
			builder.withNativeHeaders();
		}

		builder.withOutgoingDependencies(variantDependencies -> {
			return new IosApplicationOutgoingDependencies(buildVariant, variantDependencies, getObjects());
		});

		return builder.build();
	}

	@Override
	protected void onEachVariant(BuildVariantInternal buildVariant, VariantProvider<DefaultXCTestTestSuiteVariant> variant, NamingScheme names) {
		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				Provider<String> moduleName = getTestedComponent().map(it -> it.getNames().getBaseName().getAsCamelCase());
				binary.getCompileTasks().configureEach(SourceCompile.class, task -> {
					task.getCompilerArgs().addAll(getProviders().provider(() -> ImmutableList.of("-target", "x86_64-apple-ios13.2-simulator", "-F", getSdkPath() + "/System/Library/Frameworks", "-iframework", getSdkPlatformPath() + "/Developer/Library/Frameworks")));
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
					task.getLinkerArgs().addAll(getProviders().provider(() -> ImmutableList.of("-target", "x86_64-apple-ios13.2-simulator")));
					task.getLinkerArgs().addAll(task.getToolChain().map(toolChain -> {
						if (toolChain instanceof Swiftc) {
							return ImmutableList.of("-sdk", getSdkPath());
						}
						return ImmutableList.of("-isysroot", getSdkPath());
					}));
					task.getLinkerArgs().addAll(getProviders().provider(() -> ImmutableList.of(
						"-Xlinker", "-rpath", "-Xlinker", "@executable_path/Frameworks",
						"-Xlinker", "-rpath", "-Xlinker", "@loader_path/Frameworks",
						"-Xlinker", "-export_dynamic",
						"-Xlinker", "-no_deduplicate",
						"-Xlinker", "-objc_abi_version", "-Xlinker", "2",
//					"-Xlinker", "-sectcreate", "-Xlinker", "__TEXT", "-Xlinker", "__entitlements", "-Xlinker", createEntitlementTask.get().outputFile.get().asFile.absolutePath
						"-fobjc-arc", "-fobjc-link-runtime",
						"-bundle_loader", getLayout().getBuildDirectory().file("exes/main/" + moduleName.get()).get().getAsFile().getAbsolutePath(),
						"-L" + getSdkPlatformPath() + "/Developer/usr/lib", "-F" + getSdkPlatformPath() + "/Developer/Library/Frameworks", "-framework", "XCTest")));
					// TODO: -lobjc should probably only be present for binary compiling/linking objc binaries
				});
			});
		});
	}

	@Override
	public void finalizeExtension(Project project) {
		getVariants().configureEach(variant -> {
			variant.getBinaries().configureEach(BaseNativeBinary.class, binary -> {
				binary.getBaseName().convention(GUtil.toCamelCase(project.getName()));
			});
		});
		super.finalizeExtension(project);
	}
}
