package dev.nokee.docs;

import javax.inject.Inject;

public abstract class JBakeTemplateSourceSet extends LanguageSourceSet {
	@Inject
	public JBakeTemplateSourceSet(String name) {
		super(name);
	}
}
