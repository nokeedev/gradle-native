package dev.nokee.platform.base.internal.plugins;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.NodeAction;
import dev.nokee.model.internal.core.NodePredicate;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.components.DefaultComponentContainer;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.function.Consumer;

import static dev.nokee.model.internal.BaseNamedDomainObjectContainer.namedContainer;
import static dev.nokee.model.internal.BaseNamedDomainObjectView.namedView;
import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.ModelNodes.mutate;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.maven;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.withConventionOf;

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
	}

	private static NodeRegistration<DefaultComponentContainer> components() {
		return namedContainer("components", of(DefaultComponentContainer.class));
	}

	public static <T extends Component> NodeRegistration<T> component(String name, Class<T> type, Factory<T> factory) {
		return NodeRegistration.unmanaged(name, of(type), factory)
			.action(configureSourceSetConventionUsingMavenLayout(ComponentName.of(name)));
	}

	private static NodeAction configureSourceSetConventionUsingMavenLayout(ComponentName componentName) {
		return whenComponentSourcesDiscovered().apply(configureEachSourceSet(of(LanguageSourceSet.class), withConventionOf(maven(componentName))));
	}

	public static <T extends LanguageSourceSet> ModelAction configureEachSourceSet(ModelType<T> type, Consumer<? super T> action) {
		return node -> {
			assert node.canBeViewedAs(of(ComponentSources.class)) : "should only apply to ComponentSources";
			node.applyTo(allDirectDescendants(mutate(type)).apply(executeUsingProjection(type, action::accept)));
		};
	}

	public static NodePredicate whenComponentSourcesDiscovered() {
		return allDirectDescendants(discover(of(ComponentSources.class)));
	}

	public static <T extends ComponentSources> NodeRegistration<T> componentSourcesOf(Class<T> sourcesType) {
		return namedView("sources", of(sourcesType));
	}
}
