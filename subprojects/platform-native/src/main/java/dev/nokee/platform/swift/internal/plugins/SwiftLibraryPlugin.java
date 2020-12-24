package dev.nokee.platform.swift.internal.plugins;

import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetLinkageRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.swift.SwiftLibraryExtension;
import dev.nokee.platform.swift.SwiftLibrarySources;
import dev.nokee.platform.swift.internal.DefaultSwiftLibraryExtension;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.gradle.util.GUtil;

import javax.inject.Inject;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.register;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.componentSourcesOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.nativeLibrary;

public class SwiftLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public SwiftLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(SwiftLanguageBasePlugin.class);

		// TODO: Use the ComponentContainer instead of ModelRegistry
		val components = project.getExtensions().getByType(ComponentContainer.class);
//		val componentProvider = components.register("main", DefaultNativeLibraryComponent.class, component -> {
//			component.getBaseName().convention(GUtil.toCamelCase(project.getName()));
//		});
		val componentProvider = ModelNodes.of(components).register(swiftLibrary("main", project));
		componentProvider.configure(component -> {
			component.getBaseName().convention(GUtil.toCamelCase(project.getName()));
		});
		val extension = new DefaultSwiftLibraryExtension(componentProvider.get(), project.getObjects(), project.getProviders(), project.getLayout());

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(extension::finalizeExtension);

		project.getExtensions().add(SwiftLibraryExtension.class, EXTENSION_NAME, extension);
	}

	public static NodeRegistration<DefaultNativeLibraryComponent> swiftLibrary(String name, Project project) {
		return nativeLibrary(name, project)
			.action(self(discover()).apply(register(sources())));
	}

	private static NodeRegistration<SwiftLibrarySources> sources() {
		return componentSourcesOf(SwiftLibrarySources.class)
			.action(self(discover()).apply(register(sourceSet("swift", SwiftSourceSet.class))));
	}
}
