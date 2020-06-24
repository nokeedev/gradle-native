package dev.nokee.testing.xctest.internal;

import com.google.common.base.Preconditions;
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
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent.getSdkPlatformPath;

public abstract class BaseXCTestTestSuiteComponent extends BaseNativeComponent<DefaultXCTestTestSuiteVariant> implements DependencyAwareComponent<NativeComponentDependencies>, BinaryAwareComponent {
	private final DefaultNativeComponentDependencies dependencies;

	@Inject
	public BaseXCTestTestSuiteComponent(NamingScheme names) {
		super(names, DefaultXCTestTestSuiteVariant.class);
		this.dependencies = getObjects().newInstance(DefaultNativeComponentDependencies.class, names);
		getDimensions().convention(ImmutableSet.of(DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE, DefaultBinaryLinkage.DIMENSION_TYPE));

		// TODO: Move to extension
		getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		getDimensions().disallowChanges(); // Let's disallow changing them for now.
	}

	public abstract Property<GroupId> getGroupId();

	public abstract Property<BaseNativeComponent<?>> getTestedComponent();

	private Iterable<BuildVariant> createBuildVariants() {
		return ImmutableList.of(DefaultBuildVariant.of(DefaultOperatingSystemFamily.forName("ios"), DefaultMachineArchitecture.X86_64, DefaultBinaryLinkage.BUNDLE));
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super NativeComponentDependencies> action) {
		action.execute(dependencies);
	}

	@Override
	protected Provider<DefaultXCTestTestSuiteVariant> getDefaultVariant() {
		// By default, we should filter for the variant targeting the simulator
		// Here we assume only one variant that target the simulator ;-)
		return getProviders().provider(() -> {
			List<BaseNativeVariant> variants = getVariants().get().stream().map(it -> {
				Preconditions.checkArgument(it instanceof BaseNativeVariant);
				return (BaseNativeVariant) it;
			}).collect(Collectors.toList());

			if (variants.isEmpty()) {
				return null;
			}
			return (DefaultXCTestTestSuiteVariant)one(variants);
		});
	}

	@Override
	protected DefaultXCTestTestSuiteVariant createVariant(String name, BuildVariant buildVariant, AbstractBinaryAwareNativeComponentDependencies variantDependencies) {
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultXCTestTestSuiteVariant result = getObjects().newInstance(DefaultXCTestTestSuiteVariant.class, name, names, buildVariant, variantDependencies);
		return result;
	}

	@Override
	protected AbstractBinaryAwareNativeComponentDependencies newDependencies(NamingScheme names, BuildVariant buildVariant) {
		AbstractNativeComponentDependencies variantDependencies = getDependencies();
		if (getBuildVariants().get().size() > 1) {
			variantDependencies = variantDependencies.extendsWith(names);
		}

		SwiftModuleIncomingDependencies incomingSwiftDependencies = null;
		HeaderIncomingDependencies incomingHeaderDependencies = null;
		boolean hasSwift = !getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		if (hasSwift) {
			incomingSwiftDependencies = getObjects().newInstance(DefaultSwiftModuleIncomingDependencies.class, names, variantDependencies);
			incomingHeaderDependencies = getObjects().newInstance(NoHeaderIncomingDependencies.class);
		} else {
			incomingHeaderDependencies = getObjects().newInstance(DefaultHeaderIncomingDependencies.class, names, variantDependencies, buildVariant);
			incomingSwiftDependencies = getObjects().newInstance(NoSwiftModuleIncomingDependencies.class);
		}

		NativeIncomingDependencies incoming = getObjects().newInstance(NativeIncomingDependencies.class, names, buildVariant, variantDependencies, incomingSwiftDependencies, incomingHeaderDependencies);
		NativeOutgoingDependencies outgoing = getObjects().newInstance(IosApplicationOutgoingDependencies.class, names, buildVariant, variantDependencies);

		return getObjects().newInstance(BinaryAwareNativeComponentDependencies.class, variantDependencies, incoming, outgoing);
	}

	@Inject
	protected abstract DependencyHandler getDependencyHandler();

	@Override
	protected void onEachVariant(BuildVariant buildVariant, VariantProvider<DefaultXCTestTestSuiteVariant> variant, NamingScheme names) {
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
