package dev.nokee.platform.c.internal.plugins;

import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.c.CApplicationExtension;
import dev.nokee.platform.c.CApplicationSources;
import dev.nokee.platform.c.internal.DefaultCApplicationExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
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

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.register;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.componentSourcesOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.nativeApplication;

public class CApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public CApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CLanguageBasePlugin.class);

		// TODO: Use the ComponentContainer instead of ModelRegistry
		val components = project.getExtensions().getByType(ComponentContainer.class);
//		val componentProvider = components.register("main", DefaultNativeApplicationComponent.class, component -> {
//			component.getBaseName().convention(project.getName());
//		});
		val componentProvider = ModelNodes.of(components).register(cApplication("main", project));
		componentProvider.configure(component -> {
			component.getBaseName().convention(project.getName());
		});
		val extension = new DefaultCApplicationExtension(componentProvider.get(), project.getObjects(), project.getProviders(), project.getLayout());

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(CApplicationExtension.class, EXTENSION_NAME, extension);
	}

	public static NodeRegistration<DefaultNativeApplicationComponent> cApplication(String name, Project project) {
		return nativeApplication(name, project)
			.action(self(discover()).apply(register(sources())));
	}

	private static NodeRegistration<CApplicationSources> sources() {
		return componentSourcesOf(CApplicationSources.class)
			.action(self(discover()).apply(register(sourceSet("c", CSourceSet.class))))
			.action(self(discover()).apply(register(sourceSet("headers", CHeaderSet.class))));
	}
}
