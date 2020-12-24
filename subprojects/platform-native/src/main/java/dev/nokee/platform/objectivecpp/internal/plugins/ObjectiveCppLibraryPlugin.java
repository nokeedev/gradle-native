package dev.nokee.platform.objectivecpp.internal.plugins;

import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetLinkageRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibraryExtension;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibrarySources;
import dev.nokee.platform.objectivecpp.internal.DefaultObjectiveCppLibraryExtension;
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
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.nativeLibrary;
import static dev.nokee.platform.objectivecpp.internal.ObjectiveCppSourceSetModelHelpers.configureObjectiveCppSourceSetConventionUsingMavenAndGradleCoreNativeLayout;

public class ObjectiveCppLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCppLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCppLanguageBasePlugin.class);

		// TODO: Use the ComponentContainer instead of ModelRegistry
		val components = project.getExtensions().getByType(ComponentContainer.class);
//		val componentProvider = components.register("main", DefaultNativeLibraryComponent.class, component -> {
//			component.getBaseName().convention(project.getName());
//		});
		val componentProvider = ModelNodes.of(components).register(objectiveCppLibrary("main", project));
		componentProvider.configure(component -> {
			component.getBaseName().convention(project.getName());
		});
		val extension = new DefaultObjectiveCppLibraryExtension(componentProvider.get(), project.getObjects(), project.getProviders(), project.getLayout());

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(ObjectiveCppLibraryExtension.class, EXTENSION_NAME, extension);
	}

	public static NodeRegistration<DefaultNativeLibraryComponent> objectiveCppLibrary(String name, Project project) {
		return nativeLibrary(name, project)
			.action(self(discover()).apply(register(sources())))
			.action(configureObjectiveCppSourceSetConventionUsingMavenAndGradleCoreNativeLayout(ComponentName.of(name)));
	}

	private static NodeRegistration<ObjectiveCppLibrarySources> sources() {
		return componentSourcesOf(ObjectiveCppLibrarySources.class)
			.action(self(discover()).apply(register(sourceSet("objectiveCpp", ObjectiveCppSourceSet.class))))
			.action(self(discover()).apply(register(sourceSet("public", CppHeaderSet.class))))
			.action(self(discover()).apply(register(sourceSet("headers", CppHeaderSet.class))));
	}
}
