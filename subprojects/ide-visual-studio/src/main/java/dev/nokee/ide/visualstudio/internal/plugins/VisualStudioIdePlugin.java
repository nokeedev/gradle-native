package dev.nokee.ide.visualstudio.internal.plugins;

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectExtension;
import dev.nokee.ide.visualstudio.internal.rules.CreateNativeComponentVisualStudioIdeProject;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.components.ComponentConfigurer;
import dev.nokee.platform.base.internal.components.KnownComponent;
import dev.nokee.platform.base.internal.components.KnownComponentFactory;
import dev.nokee.platform.base.internal.plugins.ComponentBasePlugin;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.reflect.TypeOf;

public abstract class VisualStudioIdePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(VisualStudioIdeBasePlugin.class);

		project.getPlugins().withType(ComponentBasePlugin.class, mapComponentToVisualStudioIdeProjects(project, (VisualStudioIdeProjectExtension) project.getExtensions().getByName(VisualStudioIdeBasePlugin.VISUAL_STUDIO_EXTENSION_NAME)));
	}

	private Action<ComponentBasePlugin> mapComponentToVisualStudioIdeProjects(Project project, VisualStudioIdeProjectExtension extension) {
		return new Action<ComponentBasePlugin>() {
			private KnownComponentFactory knownComponentFactory;

			private KnownComponentFactory getKnownComponentFactory() {
				if (knownComponentFactory == null) {
					knownComponentFactory = project.getExtensions().getByType(KnownComponentFactory.class);
				}
				return knownComponentFactory;
			}

			@Override
			public void execute(ComponentBasePlugin appliedPlugin) {
				val componentConfigurer = project.getExtensions().getByType(ComponentConfigurer.class);
				componentConfigurer.whenElementKnown(ProjectIdentifier.of(project), getComponentImplementationType(), asKnownComponent(new CreateNativeComponentVisualStudioIdeProject(extension, project.getLayout(), project.getObjects(), project.getProviders())));
			}

			private <T extends Component> Action<? super TypeAwareDomainObjectIdentifier<T>> asKnownComponent(Action<? super KnownComponent<T>> action) {
				return identifier -> action.execute(getKnownComponentFactory().create(identifier));
			}

			private Class<BaseComponent<?>> getComponentImplementationType() {
				return new TypeOf<BaseComponent<?>>() {}.getConcreteClass();
			}
		};
	}
}
