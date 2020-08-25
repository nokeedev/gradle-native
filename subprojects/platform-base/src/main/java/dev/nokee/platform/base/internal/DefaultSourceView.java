package dev.nokee.platform.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.View;

import javax.inject.Inject;

public class DefaultSourceView<T extends LanguageSourceSet> extends BaseView<T> implements SourceView<T> {
	private final SourceViewFactory viewFactory;

	@Inject
	public DefaultSourceView(View<T> delegate, SourceViewFactory viewFactory) {
		super(delegate);
		this.viewFactory = viewFactory;
	}

	@Override
	public <S extends T> SourceView<S> withType(Class<S> type) {
		return viewFactory.create(super.withType(type));
	}
}
