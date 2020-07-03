package dev.nokee.ide.visualstudio;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.RegularFileProperty;

public interface VisualStudioIdeTarget {
	VisualStudioIdeProjectConfiguration getProjectConfiguration();
	VisualStudioIdePropertyGroup getProperties();
	NamedDomainObjectContainer<VisualStudioIdePropertyGroup> getItemProperties();
	RegularFileProperty getProductLocation();
}
