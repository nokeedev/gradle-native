package dev.nokee.language.base.internal;

import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.language.base.internal.UTTypeUtils.onlyIf;

// TODO: Introduce a directory source set vs file source set
public class BaseSourceSet implements ConfigurableSourceSet {
	private final String name;
	private final UTType type;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter private final SourceDirectorySet sourceDirectorySet;
	protected final ConfigurableFileCollection sources;

	protected BaseSourceSet(String name, UTType type, ObjectFactory objects) {
		this.name = name;
		this.type = type;
		this.objects = objects;
		this.sourceDirectorySet = objects.sourceDirectorySet("foo", "bar");
		this.sources = objects.fileCollection();

		sourceDirectorySet.getFilter().include(onlyIf(type).getIncludes());
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
