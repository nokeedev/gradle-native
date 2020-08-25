package dev.nokee.platform.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.View;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class SourceViewFactoryImpl implements SourceViewFactory {
	private final ObjectFactory objectFactory;

	@Inject
	public SourceViewFactoryImpl(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends LanguageSourceSet> SourceView<T> create(View<T> delegate) {
		return objectFactory.newInstance(DefaultSourceView.class, delegate, this);
	}
}
