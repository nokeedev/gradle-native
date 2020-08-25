package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public abstract class AbstractLanguageSourceSet<SELF extends LanguageSourceSet> implements LanguageSourceSet {
	private final DomainObjectIdentifier identifier;
	private final Class<SELF> publicType;
	private final ConfigurableFileCollection sources;
	private final ObjectFactory objects;
	private final PatternSet patternSet = new PatternSet();

	protected AbstractLanguageSourceSet(DomainObjectIdentifier identifier, Class<SELF> publicType, ObjectFactory objects) {
		this.identifier = identifier;
		this.publicType = publicType;
		this.sources = objects.fileCollection();
		this.objects = objects;
	}

	public SELF from(Object... paths) {
		sources.from(paths);
		return publicType.cast(this);
	}

	public FileCollection getSourceDirectories() {
		return objects.fileCollection().from(new Callable<List<File>>() {
			@Override
			public List<File> call() throws Exception {
				return sources.getFiles().stream().map(it -> {
					if (it.isDirectory()) {
						return it;
					}
					return it.getParentFile();
				}).collect(Collectors.toList());
			}
		});
	}

	// TODO: Maybe merge this into identifier?
	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(publicType);
	}

	@Override
	public PatternFilterable getFilter() {
		return patternSet;
	}

	@Override
	public SELF filter(Action<? super PatternFilterable> action) {
		action.execute(patternSet);
		return publicType.cast(this);
	}

	@Override
	public FileTree getAsFileTree() {
		return sources.getAsFileTree().matching(patternSet);
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
		return identifier;
	}

	protected String getLanguageName() {
		return LanguageSourceSetNamer.INSTANCE.determineName(this);
	}

	@Override
	public String getDisplayName() {
		String languageName = getLanguageName();
		if (languageName.toLowerCase().endsWith("resources")) {
			return languageName + " '" + identifier + "'";
		}
		return languageName + " source '" + identifier + "'";
	}
}
