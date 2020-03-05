package dev.nokee.docs;

import javax.inject.Inject;

public abstract class JBakeContentSourceSet extends LanguageSourceSet {
	@Inject
	public JBakeContentSourceSet(String name) {
		super(name);
	}
}
