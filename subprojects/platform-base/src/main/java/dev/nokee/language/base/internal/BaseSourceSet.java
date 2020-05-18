package dev.nokee.language.base.internal;

import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.language.base.internal.UTTypeUtils.onlyIf;

public abstract class BaseSourceSet<T extends UTType> implements ConfigurableSourceSet<T> {
	private final UTType type;
	private final SourceDirectorySet sourceDirectorySet = getObjects().sourceDirectorySet("foo", "bar");

	@Inject
	protected abstract ObjectFactory getObjects();

	protected BaseSourceSet(UTType type) {
		this.type = type;
		sourceDirectorySet.getFilter().include(onlyIf(type));
	}

	public SourceSet<T> srcDir(Object srcPath) {
		sourceDirectorySet.srcDir(srcPath);
		return this;
	}

	@Override
	public FileTree getAsFileTree() {
		return sourceDirectorySet.getAsFileTree().matching(it -> it.include(onlyIf(type)));
	}
}
