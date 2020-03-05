package dev.nokee.docs;

import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.file.ConfigurableFileCollection;

public abstract class LanguageSourceSet implements SoftwareComponent {
	protected final String name;

	public LanguageSourceSet(String name) {
		this.name = name;
	}

	public abstract ConfigurableFileCollection getSource();

	@Override
	public String getName() {
		return name;
	}
}
