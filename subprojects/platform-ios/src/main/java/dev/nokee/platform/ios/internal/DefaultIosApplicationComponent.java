package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.internal.PathAwareCommandLineTool;
import dev.nokee.core.exec.internal.VersionedCommandLineTool;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.base.internal.variants.KnownVariant;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.ios.tasks.internal.*;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.utils.Cast;
import lombok.Getter;
import lombok.val;
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
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.util.VersionNumber;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;

public class DefaultIosApplicationComponent extends BaseNativeComponent<DefaultIosApplicationVariant> implements DependencyAwareComponent<NativeComponentDependencies>, BinaryAwareComponent, Component {
	private final DefaultNativeComponentDependencies dependencies;
	@Getter private final Property<GroupId> groupId;
	private final DependencyHandler dependencyHandler;
	private final TaskRegistry taskRegistry;
	private final IosComponentVariants componentVariants;
	private final BinaryView<Binary> binaries;
	private final ObjectFactory objects;
	private final ProviderFactory providers;
	private final ProjectLayout layout;
	private final ConfigurationContainer configurations;

	@Inject
	public DefaultIosApplicationComponent(ComponentIdentifier<DefaultIosApplicationComponent> identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository) {
		super(identifier, DefaultIosApplicationVariant.class, objects, tasks);
		this.objects = objects;
		this.providers = providers;
		this.layout = layout;
		this.configurations = configurations;
		this.dependencyHandler = dependencyHandler;
		val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurations), dependencyHandler)));
		this.dependencies = objects.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
		this.groupId = objects.property(GroupId.class);
		getDimensions().convention(ImmutableSet.of(DefaultBinaryLinkage.DIMENSION_TYPE, DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
		this.taskRegistry = new TaskRegistryImpl(tasks);
		this.componentVariants = new IosComponentVariants(objects, this, dependencyHandler, configurations, providers, taskRegistry, eventPublisher, viewFactory, variantRepository);
		this.binaries = Cast.uncheckedCastBecauseOfTypeErasure(objects.newInstance(VariantAwareBinaryView.class, new DefaultMappingView<>(getVariantCollection().getAsView(DefaultIosApplicationVariant.class), Variant::getBinaries)));
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
	public Provider<DefaultIosApplicationVariant> getDevelopmentVariant() {
		return componentVariants.getDevelopmentVariant();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaries;
	}

	@Override
	public VariantCollection<DefaultIosApplicationVariant> getVariantCollection() {
		return componentVariants.getVariantCollection();
	}

	protected void onEachVariant(KnownVariant<DefaultIosApplicationVariant> variant) {
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

			// Create iOS application specific tasks
			Configuration interfaceBuilderToolConfiguration = configurations.create("interfaceBuilderTool");
			interfaceBuilderToolConfiguration.getDependencies().add(dependencyHandler.create("dev.nokee.tool:ibtool:latest.release"));
			Provider<CommandLineTool> interfaceBuilderTool = providers.provider(() -> new DescriptorCommandLineTool(interfaceBuilderToolConfiguration.getSingleFile()));

			Provider<CommandLineTool> assetCompilerTool = providers.provider(() -> new VersionedCommandLineTool(new File("/usr/bin/actool"), VersionNumber.parse("11.3.1")));
			Provider<CommandLineTool> codeSignatureTool = providers.provider(() -> new PathAwareCommandLineTool(new File("/usr/bin/codesign")));

			String moduleName = BaseNameUtils.from(variant.getIdentifier()).getAsCamelCase();
			Provider<String> identifier = providers.provider(() -> getGroupId().get().get().map(it -> it + "." + moduleName).orElse(moduleName));

			val compileStoryboardTask = taskRegistry.register("compileStoryboard", StoryboardCompileTask.class, task -> {
				task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/storyboards/compiled/main"));
				task.getModule().set(moduleName);
				task.getSources().from(objects.fileTree().setDir("src/main/resources").matching(it -> it.include("*.lproj/*.storyboard")));
				task.getInterfaceBuilderTool().set(interfaceBuilderTool);
				task.getInterfaceBuilderTool().finalizeValueOnRead();
			});

			val linkStoryboardTask = taskRegistry.register("linkStoryboard", StoryboardLinkTask.class, task -> {
				task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/storyboards/linked/main"));
				task.getModule().set(moduleName);
				task.getSources().from(compileStoryboardTask.flatMap(StoryboardCompileTask::getDestinationDirectory));
				task.getInterfaceBuilderTool().set(interfaceBuilderTool);
				task.getInterfaceBuilderTool().finalizeValueOnRead();
			});

			val assetCatalogCompileTaskTask = taskRegistry.register("compileAssetCatalog", AssetCatalogCompileTask.class, task -> {
				task.getSource().set(layout.getProjectDirectory().file("src/main/resources/Assets.xcassets"));
				task.getIdentifier().set(identifier);
				task.getDestinationDirectory().set(layout.getBuildDirectory().dir("ios/assets/main"));
				task.getAssetCompilerTool().set(assetCompilerTool);
			});

			val processPropertyListTask = taskRegistry.register("processPropertyList", ProcessPropertyListTask.class, task -> {
				task.getIdentifier().set(identifier);
				task.getModule().set(moduleName);
				task.getSources().from(providers.provider(() -> {
					// TODO: I'm not sure we should jump through some hoops for a missing Info.plist.
					//  I'm under the impression that a missing Info.plist file is an error and should be failing in some way.
					// TODO: Regardless of what we do above, the "skip when empty" should be handled by the task itself
					File plistFile = layout.getProjectDirectory().file("src/main/resources/Info.plist").getAsFile();
					if (plistFile.exists()) {
						return ImmutableList.of(plistFile);
					}
					return ImmutableList.of();
				}));
				task.getOutputFile().set(layout.getBuildDirectory().file("ios/Info.plist"));
			});

			val createApplicationBundleTask = taskRegistry.register("createApplicationBundle", CreateIosApplicationBundleTask.class, task -> {
				Provider<List<? extends Provider<RegularFile>>> binaries = application.getBinaries().withType(ExecutableBinaryInternal.class).map(it -> it.getLinkTask().flatMap(LinkExecutable::getLinkedFile));

				task.getExecutable().set(binaries.flatMap(it -> it.iterator().next())); // TODO: Fix this approximation
				task.getSwiftSupportRequired().convention(false);
				task.getApplicationBundle().set(layout.getBuildDirectory().file("ios/products/main/" + moduleName + "-unsigned.app"));
				task.getSources().from(linkStoryboardTask.flatMap(StoryboardLinkTask::getDestinationDirectory));
				// Linked file is configured in IosApplicationRules
				task.getSources().from(binaries);
				task.getSources().from(assetCatalogCompileTaskTask.flatMap(AssetCatalogCompileTask::getDestinationDirectory));
				task.getSources().from(processPropertyListTask.flatMap(ProcessPropertyListTask::getOutputFile));
			});
			application.getBinaryCollection().add(objects.newInstance(IosApplicationBundleInternal.class));

			val signApplicationBundleTask = taskRegistry.register("signApplicationBundle", SignIosApplicationBundleTask.class, task -> {
				task.getUnsignedApplicationBundle().set(createApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
				task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/main/" + moduleName + ".app"));
				task.getCodeSignatureTool().set(codeSignatureTool);
			});

			application.getBinaryCollection().add(objects.newInstance(SignedIosApplicationBundleInternal.class, signApplicationBundleTask));
		});

		val bundle = taskRegistry.register("bundle", task -> {
			task.dependsOn(variant.map(it -> it.getBinaries().withType(SignedIosApplicationBundleInternal.class).get()));
		});
	}

	public void finalizeExtension(Project project) {
		getVariantCollection().whenElementKnown(this::onEachVariant);
		getVariantCollection().whenElementKnown(this::createBinaries);
		getVariantCollection().whenElementKnown(new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		getVariantCollection().whenElementKnown(new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);

		componentVariants.calculateVariants();
	}
}
