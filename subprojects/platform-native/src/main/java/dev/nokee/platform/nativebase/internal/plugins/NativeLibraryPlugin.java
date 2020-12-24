package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.nativebase.NativeLibraryExtension;
import dev.nokee.platform.nativebase.internal.NativeLibraryExtensionImpl;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetLinkageRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.register;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.multiLanguageNativeLibrary;

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
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);
//		val componentProvider = components.register("main", DefaultMultiLanguageNativeLibraryComponent.class, component -> {
//			component.getBaseName().convention(project.getName());
//		});
		val componentProvider =  ModelNodes.of(components).register(multiLanguageNativeLibrary("main", project)
			.action(self(discover()).apply(register(sourceSet("public", CHeaderSet.class))))
			.action(self(discover()).apply(register(sourceSet("headers", CHeaderSet.class)))));
		componentProvider.configure(component -> {
			component.getBaseName().convention(project.getName());
		});
		val extension = new NativeLibraryExtensionImpl(componentProvider.get(), project.getObjects(), project.getProviders(), project.getLayout());

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(NativeLibraryExtension.class, EXTENSION_NAME, extension);
	}
}
