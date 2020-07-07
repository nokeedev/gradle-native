package dev.nokee.language.base.internal;

import lombok.Getter;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.language.base.internal.UTTypeUtils.onlyIf;

// TODO: Introduce a directory source set vs file source set
public abstract class BaseSourceSet implements ConfigurableSourceSet {
	private final String name;
	private final UTType type;
	@Getter private final SourceDirectorySet sourceDirectorySet = getObjects().sourceDirectorySet("foo", "bar");
	private final ConfigurableFileCollection sources = getObjects().fileCollection();

	@Inject
	protected abstract ObjectFactory getObjects();

	protected BaseSourceSet(String name, UTType type) {
		this.name = name;
		this.type = type;
		sourceDirectorySet.getFilter().include(onlyIf(type));
		sources.from(sourceDirectorySet.getAsFileTree());
	}

	public BaseSourceSet srcDir(Object srcPath) {
		sourceDirectorySet.srcDir(srcPath);
		return this;
	}

	@Override
	public BaseSourceSet from(Object files) {
		sources.from(files);
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public UTType getType() {
		return type;
	}

	@Override
	public FileTree getAsFileTree() {
		return sources.getAsFileTree();
	}
}
