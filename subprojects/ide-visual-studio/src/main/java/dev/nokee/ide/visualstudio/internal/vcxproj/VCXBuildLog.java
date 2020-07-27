package dev.nokee.ide.visualstudio.internal.vcxproj;

import lombok.Value;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

public abstract class VCXBuildLog {
	private VCXBuildLog() {}

	@Value
	@Root(name = "BuildLog")
	public static class Definition implements VCXItemDefinition {
		@Element(name = "Path")
		String path;
	}
}
