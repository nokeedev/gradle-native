package dev.nokee.model.plugins;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.internal.ModelBasePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.ModelBasePlugin.nokee;

public abstract class ModelPlugin<T extends PluginAware & ExtensionAware> implements Plugin<T> {
	T target; // leave it as package private, we don't want subclass to access it

	@Inject
	protected /*final*/ ProviderFactory getProviders() {
		throw new UnsupportedOperationException();
	}

	@Inject
	protected /*final*/ ObjectFactory getObjects() {
		throw new UnsupportedOperationException();
	}

	protected final PluginManager getPluginManager() {
		return target.getPluginManager();
	}

	@Override
	public final void apply(T target) {
		this.target = target;
		target.getPluginManager().apply(ModelBasePlugin.class);
		apply(nokee(target).getModel(), nokee(target).getModel().projection(typeOf(target)));
	}

	private Class<T> typeOf(T target) {
		if (target instanceof Project) {
			return (Class<T>) Project.class;
		}
		return (Class<T>) Settings.class;
	}

	protected abstract void apply(ModelNode node, KnownDomainObject<T> knownTarget);
}
