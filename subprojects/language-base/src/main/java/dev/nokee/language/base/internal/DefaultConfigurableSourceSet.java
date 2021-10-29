package dev.nokee.language.base.internal;

import dev.nokee.language.base.ConfigurableSourceSet;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;

final class DefaultConfigurableSourceSet implements ConfigurableSourceSet {
	private final LanguageSourceSetStrategy strategy;

	public DefaultConfigurableSourceSet(LanguageSourceSetStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public PatternFilterable getFilter() {
		return strategy.getFilter();
	}

	@Override
	public ConfigurableSourceSet filter(Action<? super PatternFilterable> action) {
		action.execute(strategy.getFilter());
		return this;
	}

	@Override
	public ConfigurableSourceSet from(Object... paths) {
		strategy.from(paths);
		return this;
	}

	@Override
	public ConfigurableSourceSet convention(Object... paths) {
		strategy.convention(paths);
		return this;
	}

	@Override
	public FileTree getAsFileTree() {
		return strategy.getAsFileTree();
	}

	@Override
	public FileCollection getSourceDirectories() {
		return strategy.getSourceDirectories();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return strategy.getBuildDependencies();
	}
}
