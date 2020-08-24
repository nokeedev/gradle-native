package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.util.PatternFilterable;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class LanguageSourceSetImpl implements LanguageSourceSet {
	//	private final DomainObjectIdentifier identifier;
//	private final Class<SELF> publicType;
	private final ConfigurableFileCollection sources;
	private final ObjectFactory objects;
	//	private final PatternSet patternSet = new PatternSet();

	protected LanguageSourceSetImpl(ObjectFactory objects) {
//		this.identifier = identifier;
//		this.publicType = publicType;
		this.sources = objects.fileCollection();
		this.objects = objects;
	}

	@Override
	public LanguageSourceSet from(Object... paths) {
		sources.from(paths);
		return this;
	}

	@Override
	public FileCollection getSourceDirectories() {
		return objects.fileCollection().from(sources.getElements().map(this::toSourceDirectories));
	}

	private Iterable<File> toSourceDirectories(Set<FileSystemLocation> files) {
		return files.stream().map(FileSystemLocation::getAsFile).map(this::toSourceDirectory).collect(Collectors.toList());
	}

	private File toSourceDirectory(File file) {
		if (file.isDirectory()) {
			return file;
		}
		return file.getParentFile();
	}

	// TODO: Maybe merge this into identifier?
	@Override
	public TypeOf<?> getPublicType() {
//		return TypeOf.typeOf(publicType);
		return null;
	}

	@Override
	public PatternFilterable getFilter() {
//		return patternSet;
		return null;
	}

	@Override
	public LanguageSourceSet filter(Action<? super PatternFilterable> action) {
//		action.execute(patternSet);
//		return publicType.cast(this);
		return null;
	}

	@Override
	public FileTree getAsFileTree() {
//		return sources.getAsFileTree().matching(patternSet);
		return sources.getAsFileTree();
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
//		return identifier;
		return null;
	}

//	protected String getLanguageName() {
//		return LanguageSourceSetNamer.INSTANCE.determineName(this);
//	}

	@Override
	public String getDisplayName() {
//		String languageName = getLanguageName();
//		if (languageName.toLowerCase().endsWith("resources")) {
//			return languageName + " '" + identifier + "'";
//		}
//		return languageName + " source '" + identifier + "'";
		return null;
	}
}
