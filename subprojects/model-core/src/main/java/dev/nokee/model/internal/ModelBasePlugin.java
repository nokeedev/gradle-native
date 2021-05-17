package dev.nokee.model.internal;

import dev.nokee.model.NokeeExtension;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.function.Function;

public /*final*/ abstract class ModelBasePlugin<T extends PluginAware & ExtensionAware> implements Plugin<T> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public void apply(T target) {
		val type = computeWhen(target, it -> Settings.class, it -> Project.class);

		val extension = getObjects().newInstance(DefaultNokeeExtension.class);
		target.getExtensions().add(NokeeExtension.class, "nokee", extension);

		extension.getModelRegistry().getRoot().newProjection(builder -> builder.type(type).forInstance(target));
	}

	private static <T> T computeWhen(Object target, Function<? super Settings, T> settingsAction, Function<? super Project, T> projectAction) {
		if (target instanceof Settings) {
			return settingsAction.apply((Settings) target);
		} else if (target instanceof Project) {
			return projectAction.apply((Project) target);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
