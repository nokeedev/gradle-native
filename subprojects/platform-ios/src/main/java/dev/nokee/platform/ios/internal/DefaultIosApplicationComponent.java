package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.internal.PathAwareCommandLineTool;
import dev.nokee.core.exec.internal.VersionedCommandLineTool;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactories;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.ios.tasks.internal.*;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.DevelopmentVariantConvention;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.Project;
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
import org.gradle.util.GUtil;
import org.gradle.util.VersionNumber;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;

public class DefaultIosApplicationComponent extends BaseNativeComponent<DefaultIosApplicationVariant> implements DependencyAwareComponent<NativeComponentDependencies>, BinaryAwareComponent, Component {
	private final DefaultNativeComponentDependencies dependencies;
	@Getter private final Property<GroupId> groupId;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;
	private final TaskRegistry taskRegistry;

	@Inject
	public DefaultIosApplicationComponent(ComponentIdentifier<DefaultIosApplicationComponent> identifier, NamingScheme names, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler) {
		super(identifier, names, DefaultIosApplicationVariant.class, objects, providers, tasks, layout, configurations);
		this.dependencyHandler = dependencyHandler;
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(getConfigurations()), names::getConfigurationName), new DefaultDependencyFactory(getDependencyHandler()))));
		this.dependencies = objects.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
		this.groupId = objects.property(GroupId.class);
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
		getDevelopmentVariant().convention(providers.provider(new DevelopmentVariantConvention<>(getVariantCollection()::get)));
		this.taskRegistry = new TaskRegistryImpl(tasks);
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	protected DefaultIosApplicationVariant createVariant(VariantIdentifier<?> identifier, VariantComponentDependencies<?> variantDependencies) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		DefaultIosApplicationVariant result = getObjects().newInstance(DefaultIosApplicationVariant.class, identifier, names, variantDependencies);
		return result;
	}

	@Override
	protected VariantComponentDependencies<NativeComponentDependencies> newDependencies(NamingScheme names, BuildVariantInternal buildVariant) {
		var variantDependencies = getDependencies();
		if (getBuildVariants().get().size() > 1) {
			val dependencyContainer = getObjects().newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(getConfigurations()), names::getConfigurationName), new DefaultDependencyFactory(getDependencyHandler())));
			variantDependencies = getObjects().newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant);
		boolean hasSwift = !getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(getObjects());
		NativeOutgoingDependencies outgoing = getObjects().newInstance(IosApplicationOutgoingDependencies.class, names, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	protected void onEachVariant(KnownVariant<DefaultIosApplicationVariant> variant) {
		variant.configure(application -> {
			application.getBinaries().configureEach(ExecutableBinary.class, binary -> {
				binary.getCompileTasks().configureEach(SourceCompile.class, task -> {
					task.getCompilerArgs().addAll(getProviders().provider(() -> ImmutableList.of("-target", "x86_64-apple-ios13.2-simulator", "-F", getSdkPath() + "/System/Library/Frameworks")));
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
					task.getLinkerArgs().addAll("-Xlinker", "-rpath", "-Xlinker", "@executable_path/Frameworks",
						"-Xlinker", "-export_dynamic",
						"-Xlinker", "-no_deduplicate",
						"-Xlinker", "-objc_abi_version", "-Xlinker", "2",
//					"-Xlinker", "-sectcreate", "-Xlinker", "__TEXT", "-Xlinker", "__entitlements", "-Xlinker", createEntitlementTask.get().outputFile.get().asFile.absolutePath
						"-lobjc", "-framework", "UIKit", "-framework", "Foundation");
					// TODO: -lobjc should probably only be present for binary compiling/linking objc binaries
				});
			});

			// Create iOS application specific tasks
			Configuration interfaceBuilderToolConfiguration = getConfigurations().create("interfaceBuilderTool");
			interfaceBuilderToolConfiguration.getDependencies().add(getDependencyHandler().create("dev.nokee.tool:ibtool:latest.release"));
			Provider<CommandLineTool> interfaceBuilderTool = getProviders().provider(() -> new DescriptorCommandLineTool(interfaceBuilderToolConfiguration.getSingleFile()));

			Provider<CommandLineTool> assetCompilerTool = getProviders().provider(() -> new VersionedCommandLineTool(new File("/usr/bin/actool"), VersionNumber.parse("11.3.1")));
			Provider<CommandLineTool> codeSignatureTool = getProviders().provider(() -> new PathAwareCommandLineTool(new File("/usr/bin/codesign")));

			String moduleName = getNames().getBaseName().getAsCamelCase();
			Provider<String> identifier = getProviders().provider(() -> getGroupId().get().get().map(it -> it + "." + moduleName).orElse(moduleName));

			val compileStoryboardTask = taskRegistry.register("compileStoryboard", StoryboardCompileTask.class, task -> {
				task.getDestinationDirectory().set(getLayout().getBuildDirectory().dir("ios/storyboards/compiled/main"));
				task.getModule().set(moduleName);
				task.getSources().from(getObjects().fileTree().setDir("src/main/resources").matching(it -> it.include("*.lproj/*.storyboard")));
				task.getInterfaceBuilderTool().set(interfaceBuilderTool);
				task.getInterfaceBuilderTool().finalizeValueOnRead();
			});

			val linkStoryboardTask = taskRegistry.register("linkStoryboard", StoryboardLinkTask.class, task -> {
				task.getDestinationDirectory().set(getLayout().getBuildDirectory().dir("ios/storyboards/linked/main"));
				task.getModule().set(moduleName);
				task.getSources().from(compileStoryboardTask.flatMap(StoryboardCompileTask::getDestinationDirectory));
				task.getInterfaceBuilderTool().set(interfaceBuilderTool);
				task.getInterfaceBuilderTool().finalizeValueOnRead();
			});

			val assetCatalogCompileTaskTask = taskRegistry.register("compileAssetCatalog", AssetCatalogCompileTask.class, task -> {
				task.getSource().set(getLayout().getProjectDirectory().file("src/main/resources/Assets.xcassets"));
				task.getIdentifier().set(identifier);
				task.getDestinationDirectory().set(getLayout().getBuildDirectory().dir("ios/assets/main"));
				task.getAssetCompilerTool().set(assetCompilerTool);
			});

			val processPropertyListTask = taskRegistry.register("processPropertyList", ProcessPropertyListTask.class, task -> {
				task.getIdentifier().set(identifier);
				task.getModule().set(moduleName);
				task.getSources().from(getProviders().provider(() -> {
					// TODO: I'm not sure we should jump through some hoops for a missing Info.plist.
					//  I'm under the impression that a missing Info.plist file is an error and should be failing in some way.
					// TODO: Regardless of what we do above, the "skip when empty" should be handled by the task itself
					File plistFile = getLayout().getProjectDirectory().file("src/main/resources/Info.plist").getAsFile();
					if (plistFile.exists()) {
						return ImmutableList.of(plistFile);
					}
					return ImmutableList.of();
				}));
				task.getOutputFile().set(getLayout().getBuildDirectory().file("ios/Info.plist"));
			});

			val createApplicationBundleTask = taskRegistry.register("createApplicationBundle", CreateIosApplicationBundleTask.class, task -> {
				List<? extends Provider<RegularFile>> binaries = application.getBinaries().withType(ExecutableBinaryInternal.class).map(it -> it.getLinkTask().flatMap(LinkExecutable::getLinkedFile)).get();

				task.getExecutable().set(binaries.iterator().next()); // TODO: Fix this approximation
				task.getSwiftSupportRequired().convention(false);
				task.getApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/main/" + moduleName + "-unsigned.app"));
				task.getSources().from(linkStoryboardTask.flatMap(StoryboardLinkTask::getDestinationDirectory));
				// Linked file is configured in IosApplicationRules
				task.getSources().from(binaries);
				task.getSources().from(assetCatalogCompileTaskTask.flatMap(AssetCatalogCompileTask::getDestinationDirectory));
				task.getSources().from(processPropertyListTask.flatMap(ProcessPropertyListTask::getOutputFile));
			});
			application.getBinaryCollection().add(getObjects().newInstance(IosApplicationBundleInternal.class));

			val signApplicationBundleTask = taskRegistry.register("signApplicationBundle", SignIosApplicationBundleTask.class, task -> {
				task.getUnsignedApplicationBundle().set(createApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
				task.getSignedApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/main/" + moduleName + ".app"));
				task.getCodeSignatureTool().set(codeSignatureTool);
			});

			application.getBinaryCollection().add(getObjects().newInstance(SignedIosApplicationBundleInternal.class, signApplicationBundleTask));
		});

		val bundle = taskRegistry.register("bundle", task -> {
			task.dependsOn(variant.map(it -> it.getBinaries().withType(SignedIosApplicationBundleInternal.class).get()));
		});
	}

	@Override
	public void finalizeExtension(Project project) {
		getVariants().configureEach(variant -> {
			variant.getBinaries().configureEach(BaseNativeBinary.class, binary -> {
				binary.getBaseName().convention(GUtil.toCamelCase(project.getName()));
			});
		});
		getVariantCollection().whenElementKnown(this::onEachVariant);
		super.finalizeExtension(project);
	}

	public static DomainObjectFactory<DefaultIosApplicationComponent> newFactory(ObjectFactory objects, NamingSchemeFactory namingSchemeFactory) {
		return identifier -> {
			NamingScheme names = namingSchemeFactory.forMainComponent().withComponentDisplayName(((ComponentIdentifier<?>)identifier).getDisplayName());
			return objects.newInstance(DefaultIosApplicationComponent.class, identifier, names);
		};
	}
}
