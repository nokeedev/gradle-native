package dev.nokee.platform.cpp.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.cpp.internal.plugins.CppLanguageBasePlugin;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.cpp.CppLibraryExtension;
import dev.nokee.platform.cpp.internal.DefaultCppLibraryExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetLinkageRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public class CppLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public CppLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CppLanguageBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);
		val componentProvider = components.register("main", DefaultNativeLibraryComponent.class, component -> {
			component.getBaseName().convention(project.getName());
		});
		val extension = new DefaultCppLibraryExtension(componentProvider.get(), project.getObjects(), project.getProviders(), project.getLayout(), project.getExtensions().getByType(LanguageSourceSetRegistry.class));

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(CppLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
