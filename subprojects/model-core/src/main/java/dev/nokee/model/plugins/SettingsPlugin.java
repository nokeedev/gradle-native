package dev.nokee.model.plugins;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.dsl.ModelNode;
import org.gradle.api.initialization.Settings;

public abstract class SettingsPlugin extends ModelPlugin<Settings> {
	@Override
	protected abstract void apply(ModelNode node, KnownDomainObject<Settings> knownSettings);
}
