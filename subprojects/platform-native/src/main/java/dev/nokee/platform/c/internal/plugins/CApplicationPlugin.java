package dev.nokee.platform.c.internal.plugins;

import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactoryRegistry;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.c.CApplication;
import dev.nokee.platform.c.CApplicationSources;
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
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.component;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.componentSourcesOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;

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
		val components = project.getExtensions().getByType(ComponentContainer.class);
		ModelNodes.of(components).get(NodeRegistrationFactoryRegistry.class).registerFactory(of(CApplication.class), name -> cApplication(name, project));
		val componentProvider = components.register("main", CApplication.class, configureUsingProjection(DefaultNativeApplicationComponent.class, baseNameConvention(project.getName()).andThen(configureBuildVariants())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(CApplication.class, EXTENSION_NAME, extension);
	}

	public static NodeRegistration<CApplication> cApplication(String name, Project project) {
		return component(name, CApplication.class)
			.withProjection(createdUsing(of(DefaultNativeApplicationComponent.class), nativeApplicationProjection(name, project)))
			.action(self(discover()).apply(register(sources())));
	}

	private static NodeRegistration<CApplicationSources> sources() {
		return componentSourcesOf(CApplicationSources.class)
			.action(self(discover()).apply(register(sourceSet("c", CSourceSet.class))))
			.action(self(discover()).apply(register(sourceSet("headers", CHeaderSet.class))));
	}
}
