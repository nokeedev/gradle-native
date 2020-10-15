package dev.nokee.language.objectivecpp.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.language.base.internal.UTTypeUtils.asFilenamePattern;

public final class ObjectiveCppSourceSetImpl extends AbstractLanguageSourceSet<ObjectiveCppSourceSet> implements ObjectiveCppSourceSet {
	public ObjectiveCppSourceSetImpl(LanguageSourceSetIdentifier<?> identifier, ObjectFactory objects) {
		super(identifier, ObjectiveCppSourceSet.class, objects);
		getFilter().include(asFilenamePattern(UTTypeObjectiveCppSource.INSTANCE));
	}
}
