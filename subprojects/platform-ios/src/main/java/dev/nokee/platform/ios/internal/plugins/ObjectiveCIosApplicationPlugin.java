package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.plugins.ProjectStorePlugin;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.DaggerPlatformIosComponents;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosApplicationExtension;
import dev.nokee.runtime.darwin.internal.plugins.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
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

public class ObjectiveCIosApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;

	@Inject
	public ObjectiveCIosApplicationPlugin(ObjectFactory objects, ProjectLayout layout, TaskContainer tasks, ProviderFactory providers) {

		this.objects = objects;
		this.layout = layout;
		this.tasks = tasks;
		this.providers = providers;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);
		project.getPluginManager().apply(ToolChainMetadataRules.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class);
		project.getPluginManager().apply(ProjectStorePlugin.class);

		val store = project.getExtensions().getByType(DomainObjectStore.class);
		val identifier = ComponentIdentifier.ofMain(ProjectIdentifier.of(project));
		val component = DaggerPlatformIosComponents.factory().create(project).applicationFactory().create(identifier);
		store.add(new DomainObjectElement<DefaultIosApplicationComponent>() {
			  @Override
			  public DefaultIosApplicationComponent get() {
				  return component;
			  }

			  @Override
			  public Class<DefaultIosApplicationComponent> getType() {
				  return DefaultIosApplicationComponent.class;
			  }

			  @Override
			  public DomainObjectIdentity getIdentity() {
				  return DomainObjectIdentity.named("main");
			  }
		});
		component.getGroupId().set(GroupId.of(project::getGroup));
		val extension = getObjects().newInstance(DefaultObjectiveCIosApplicationExtension.class, component);

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
