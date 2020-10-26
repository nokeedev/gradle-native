package dev.nokee.language.jvm.internal;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.jvm.JavaSourceSet;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

public final class JavaSourceSetImpl extends AbstractLanguageSourceSetAdapter<JavaSourceSet> implements JavaSourceSet {
	public JavaSourceSetImpl(LanguageSourceSetIdentifier<?> identifier, SourceDirectorySet sourceDirectorySet, ObjectFactory objectFactory, ProjectLayout projectLayout) {
		super(identifier, JavaSourceSet.class, sourceDirectorySet, objectFactory, projectLayout);
	}
}
