package dev.nokee.language.jvm.internal;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.jvm.KotlinSourceSet;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

public final class KotlinSourceSetImpl extends AbstractLanguageSourceSetAdapter<KotlinSourceSet> implements KotlinSourceSet {
	public KotlinSourceSetImpl(LanguageSourceSetIdentifier<?> identifier, SourceDirectorySet sourceDirectorySet, ObjectFactory objectFactory, ProjectLayout projectLayout) {
		super(identifier, KotlinSourceSet.class, sourceDirectorySet, objectFactory, projectLayout);
	}
}
