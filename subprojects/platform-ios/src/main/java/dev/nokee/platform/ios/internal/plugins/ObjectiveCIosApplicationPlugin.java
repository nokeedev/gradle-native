package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.internal.Cast;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosApplicationExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;
import java.util.Arrays;

import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.platform.nativebase.internal.NativePlatformFactory.platformNameFor;

public abstract class ObjectiveCIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);
		project.getPluginManager().apply(ToolChainMetadataRules.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class);

		NamingScheme names = NamingScheme.asMainComponent(project.getName()).withComponentDisplayName("main iOS application");
		ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", project.getExtensions().create("components", ComponentCollection.class));
		DefaultIosApplicationComponent component = components.register(DefaultIosApplicationComponent.class, names).get();
		component.getGroupId().set(GroupId.of(project::getGroup));
		DefaultObjectiveCIosApplicationExtension extension = getObjects().newInstance(DefaultObjectiveCIosApplicationExtension.class, component);

		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(ObjectiveCIosApplicationExtension.class, EXTENSION_NAME, extension);
	}

	public static class ToolChainMetadataRules extends RuleSource {
		@Mutate
		public void configureToolchain(NativeToolChainRegistry toolchains) {
			toolchains.withType(Clang.class, toolchain -> {
				toolchain.target(platformNameFor(DefaultOperatingSystemFamily.IOS, DefaultMachineArchitecture.X86_64), platform -> {
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
}
