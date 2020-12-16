package dev.nokee.platform.base.internal.plugins;

import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.model.internal.DomainObjectDiscovered;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.components.DefaultComponentContainer;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.BaseNamedDomainObjectContainer.newRegistration;

public class ComponentModelBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(LanguageBasePlugin.class);
		project.getPluginManager().apply(BinaryBasePlugin.class);
		project.getPluginManager().apply(TaskBasePlugin.class);
		project.getPluginManager().apply(VariantBasePlugin.class);

		val modeRegistry = project.getExtensions().getByType(ModelRegistry.class);
		val components = modeRegistry.register(components()).get();
		project.getExtensions().add(ComponentContainer.class, "components", components);

		// Shimming model to discovered events
		components.whenElementKnownEx(knownComponent -> {
			val modelLookup = project.getExtensions().getByType(ModelLookup.class);
			val path = ((ModelIdentifier<?>)knownComponent.getIdentifier()).getPath();
			val type = modelLookup.get(path).get(Component.class).getClass();
			project.getExtensions().getByType(DomainObjectEventPublisher.class).publish(new DomainObjectDiscovered<>(ComponentIdentifier.of(ComponentName.of(path.getName()), type, ProjectIdentifier.of(project))));
		});
	}

	private static NodeRegistration<DefaultComponentContainer> components() {
		return newRegistration("components", DefaultComponentContainer.class);
	}
}
