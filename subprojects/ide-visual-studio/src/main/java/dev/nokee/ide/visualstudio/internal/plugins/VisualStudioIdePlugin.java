package dev.nokee.ide.visualstudio.internal.plugins;

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectExtension;
import dev.nokee.ide.visualstudio.internal.rules.CreateNativeComponentVisualStudioIdeProject;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.core.ModelActions.*;
import static dev.nokee.model.internal.core.ModelNodes.withType;

public abstract class VisualStudioIdePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(VisualStudioIdeBasePlugin.class);

		project.getPlugins().withType(ComponentModelBasePlugin.class, mapComponentToVisualStudioIdeProjects(project, (VisualStudioIdeProjectExtension) project.getExtensions().getByName(VisualStudioIdeBasePlugin.VISUAL_STUDIO_EXTENSION_NAME)));
	}

	private Action<ComponentModelBasePlugin> mapComponentToVisualStudioIdeProjects(Project project, VisualStudioIdeProjectExtension extension) {
		return new Action<ComponentModelBasePlugin>() {
			@Override
			public void execute(ComponentModelBasePlugin appliedPlugin) {
				val modelConfigurer = project.getExtensions().getByType(ModelConfigurer.class);
				val action = new CreateNativeComponentVisualStudioIdeProject(extension, project.getLayout(), project.getObjects(), project.getProviders());
				modelConfigurer.configure(matching(ModelNodes.stateAtLeast(ModelNode.State.Registered).and(withType(getComponentImplementationType()))::test, once(executeAsKnownProjection(getComponentImplementationType(), action))));
			}

			private ModelType<BaseComponent<?>> getComponentImplementationType() {
				return ModelType.of(new TypeOf<BaseComponent<?>>() {});
			}
		};
	}
}
