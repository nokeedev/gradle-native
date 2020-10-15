package dev.nokee.language.objectivec.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.language.base.internal.UTTypeUtils.asFilenamePattern;

public final class ObjectiveCSourceSetImpl extends AbstractLanguageSourceSet<ObjectiveCSourceSet> implements ObjectiveCSourceSet {
	public ObjectiveCSourceSetImpl(ObjectFactory objects) {
		super(ObjectiveCSourceSet.class, objects);
		getFilter().include(asFilenamePattern(UTTypeObjectiveCSource.INSTANCE));
	}
}
