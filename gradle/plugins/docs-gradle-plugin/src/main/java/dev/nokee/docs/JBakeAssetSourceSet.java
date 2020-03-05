package dev.nokee.docs;

import javax.inject.Inject;

public abstract class JBakeAssetSourceSet extends LanguageSourceSet {
	@Inject
	public JBakeAssetSourceSet(String name) {
		super(name);
	}
}
