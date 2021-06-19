package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProjections;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactoryRegistry;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.ObjectiveCIosApplication;
import dev.nokee.platform.ios.ObjectiveCIosApplicationSources;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;
import org.gradle.util.GUtil;

import java.util.Arrays;
import java.util.function.BiConsumer;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.register;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.component;
import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.platform.nativebase.internal.NativePlatformFactory.platformNameFor;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static dev.nokee.platform.objectivec.internal.ObjectiveCSourceSetModelHelpers.configureObjectiveCSourceSetConventionUsingMavenAndGradleCoreNativeLayout;

public class ObjectiveCIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);
		project.getPluginManager().apply(ToolChainMetadataRules.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class);

		// Create the component
		project.getPluginManager().apply(ComponentModelBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);

		val components = project.getExtensions().getByType(ComponentContainer.class);
		val registry = ModelNodes.of(components).get(NodeRegistrationFactoryRegistry.class);
		registry.registerFactory(of(ObjectiveCIosApplication.class), name -> objectiveCIosApplication(name, project));
		val componentProvider = components.register("main", ObjectiveCIosApplication.class, configureUsingProjection(DefaultIosApplicationComponent.class, baseNameConvention(GUtil.toCamelCase(project.getName())).andThen((t, projection) -> ((DefaultIosApplicationComponent) projection).getGroupId().set(GroupId.of(project::getGroup))).andThen(configureBuildVariants())));
		project.getExtensions().add(ObjectiveCIosApplication.class, EXTENSION_NAME, componentProvider.get());

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));
	}

	public static class ToolChainMetadataRules extends RuleSource {
		@Mutate
		public void configureToolchain(NativeToolChainRegistry toolchains) {
			toolchains.withType(Clang.class, toolchain -> {
				toolchain.target(platformNameFor(OperatingSystemFamily.forName(OperatingSystemFamily.IOS), MachineArchitecture.forName(MachineArchitecture.X86_64)), platform -> {
					// Although this should be correct, clearing the args to remove the -m64 (which is not technically, exactly, required in this instance) and adding the target with the correct sysroot...
					// Gradle forcefully append the macOS SDK sysroot to the configured args.
					// The sysroot used is the macOS not the iPhoneSimulator.
					// To solve this, we can reprobe the compiler right before the task executes.
					((DefaultGccPlatformToolChain) platform).getCompilerProbeArgs().clear();
					((DefaultGccPlatformToolChain) platform).getCompilerProbeArgs().addAll(Arrays.asList("-target", "x86_64-apple-ios13.2-simulator", "-isysroot", getSdkPath()));
				});
			});
		}
	}

	public static <T, PROJECTION extends BaseComponent<?>> BiConsumer<T, PROJECTION> configureBuildVariants() {
		return (t, projection) -> {
			projection.getBuildVariants().set(projection.getFinalSpace().map(DefaultBuildVariant::fromSpace));
			projection.getBuildVariants().finalizeValueOnRead();
			projection.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.
		};
	}

	public static NodeRegistration<ObjectiveCIosApplication> objectiveCIosApplication(String name, Project project) {
		return component(name, ObjectiveCIosApplication.class)
			.withProjection(ModelProjections.createdUsing(of(DefaultIosApplicationComponent.class), () -> create(name, project)))
			.action(self(discover()).apply(register(sources())))
			.action(configureObjectiveCSourceSetConventionUsingMavenAndGradleCoreNativeLayout(ComponentName.of(name)));
	}

	public static DefaultIosApplicationComponent create(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultIosApplicationComponent.class, ProjectIdentifier.of(project));
		return new DefaultIosApplicationComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class));
	}

	private static NodeRegistration<ObjectiveCIosApplicationSources> sources() {
		return ComponentModelBasePlugin.componentSourcesOf(ObjectiveCIosApplicationSources.class)
			.action(self(discover()).apply(register(sourceSet("objectiveC", ObjectiveCSourceSet.class))))
			.action(self(discover()).apply(register(sourceSet("headers", CHeaderSet.class))))
			.action(self(discover()).apply(register(sourceSet("resources", IosResourceSet.class))));
	}
}
