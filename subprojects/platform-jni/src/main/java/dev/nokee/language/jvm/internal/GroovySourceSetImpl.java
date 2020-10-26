package dev.nokee.language.jvm.internal;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.jvm.GroovySourceSet;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

public final class GroovySourceSetImpl extends AbstractLanguageSourceSetAdapter<GroovySourceSet> implements GroovySourceSet {
	public GroovySourceSetImpl(LanguageSourceSetIdentifier<?> identifier, SourceDirectorySet sourceDirectorySet, ObjectFactory objectFactory, ProjectLayout projectLayout) {
		super(identifier, GroovySourceSet.class, sourceDirectorySet, objectFactory, projectLayout);
	}
}
