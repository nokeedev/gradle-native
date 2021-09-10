package dev.nokee.model.internal;

import dev.nokee.model.NokeeExtension;
import dev.nokee.model.core.ModelProjection;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.function.Consumer;

import static dev.nokee.utils.NamedDomainObjectCollectionUtils.whenElementKnown;

public abstract /*final*/ class ModelBasePlugin<T extends PluginAware & ExtensionAware> implements Plugin<T> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public void apply(T target) {
		val type = publicTypeOf(target);

		val registry = new DefaultNamedDomainObjectRegistry();
		val extension = getObjects().newInstance(DefaultNokeeExtension.class, registry);
		target.getExtensions().add(NokeeExtension.class, "nokee", extension);

		extension.getModelRegistry().getRoot().newProjection(builder -> builder.type(type).forInstance(target));

		executeWhen(target,
			it -> {

			},
			project -> {
				extension.bridgeContainer(project.getConfigurations());

				// Special handling for Task container, because it supports any subtype of Task
				val bridgedContainer = new BridgedDomainObjectContainerRegistry<>(new NamedDomainObjectContainerRegistry.TaskContainerRegistry(project.getTasks()), extension.getModelRegistry());
				registry.registerContainer(bridgedContainer);
				whenElementKnown(project.getTasks(), bridgedContainer);

				// Special handling for SoftwareComponent container, because it's a two-part container (the named set and factory)
				//   Here, we shim the "special" container into a normal container.
				registry.registerContainer(getObjects().newInstance(NamedDomainObjectContainerRegistry.SoftwareComponentContainerRegistry.class, project.getComponents()));
				project.getComponents().whenObjectAdded(it -> {
					val node = extension.getModelRegistry().getRoot().find(it.getName()).orElseGet(() -> extension.getModelRegistry().getRoot().newChildNode(it.getName()));
					node.newProjection(builder -> builder.type((Class<SoftwareComponent>) it.getClass()).forInstance(it));
				});

				project.afterEvaluate(proj -> {
					extension.getModelRegistry().allProjections().forEach(ModelProjection::finalizeProjection);
				});
			}
		);
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> publicTypeOf(T target) {
		if (target instanceof Settings) {
			return (Class<T>) Settings.class;
		} else if (target instanceof Project) {
			return (Class<T>) Project.class;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private static void executeWhen(Object target, Consumer<? super Settings> settingsAction, Consumer<? super Project> projectAction) {
		if (target instanceof Settings) {
			settingsAction.accept((Settings) target);
		} else if (target instanceof Project) {
			projectAction.accept((Project) target);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
