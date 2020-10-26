package dev.nokee.platform.ios.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.platform.ios.IosResourceSet;
import org.gradle.api.model.ObjectFactory;

public final class IosResourceSetImpl extends AbstractLanguageSourceSet<IosResourceSet> implements IosResourceSet {
	public IosResourceSetImpl(LanguageSourceSetIdentifier<?> identifier, ObjectFactory objectFactory) {
		super(identifier, IosResourceSet.class, objectFactory);
	}
}
