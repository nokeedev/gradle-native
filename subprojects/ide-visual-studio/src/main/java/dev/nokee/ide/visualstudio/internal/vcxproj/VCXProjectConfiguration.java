package dev.nokee.ide.visualstudio.internal.vcxproj;

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import lombok.Value;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Value(staticConstructor = "of")
@Root(name = "ProjectConfiguration")
public class VCXProjectConfiguration implements VCXItem {
	@Attribute
	public String getInclude() {
		return configuration + "|" + platform;
	}

	@Element
	String configuration;

	@Element
	String platform;

	public static VCXProjectConfiguration of(VisualStudioIdeProjectConfiguration projectConfiguration) {
		return new VCXProjectConfiguration(projectConfiguration.getConfiguration().getIdentifier(), projectConfiguration.getPlatform().getIdentifier());
	}
}
