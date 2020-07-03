package dev.nokee.ide.base.internal;

import org.gradle.api.Describable;
import org.gradle.api.Rule;

public abstract class IdeBridgeRule implements Rule {
	private final Describable ide;

	public IdeBridgeRule(Describable ide) {
		this.ide = ide;
	}

	@Override
	public String getDescription() {
		return String.format("%s IDE bridge tasks begin with %s. Do not call these directly.", ide.getDisplayName(), "_xcode");
	}

	@Override
	public void apply(String taskName) {

	}

	protected abstract void doApply(String taskName);
}
