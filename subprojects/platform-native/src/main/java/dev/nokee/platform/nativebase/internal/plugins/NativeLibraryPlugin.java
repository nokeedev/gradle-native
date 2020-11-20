package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.c.internal.CHeaderSetImpl;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.nativebase.NativeLibraryExtension;
import dev.nokee.platform.nativebase.internal.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

public class NativeLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public NativeLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);
		val componentProvider = components.register("main", DefaultMultiLanguageNativeLibraryComponent.class, component -> {
			component.getBaseName().convention(project.getName());
		});
		val extension = new NativeLibraryExtensionImpl(componentProvider.get(), project.getObjects(), project.getProviders(), project.getLayout());

		// This is a work around for exported headers, some clean up needs to happen:
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		val sourceSetRegistry = project.getExtensions().getByType(LanguageSourceSetRegistry.class);
		sourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("public"), CHeaderSetImpl.class, componentProvider.getIdentifier()));

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(NativeLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
