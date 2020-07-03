package dev.nokee.ide.visualstudio.internal.vcxproj;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Root(name = "Import")
public class VCXImport {
	@Attribute(required = false)
	@With String label;

	@Attribute
	String project;

	@Attribute(required = false)
	@With String condition;

	public static VCXImport of(String project) {
		return new VCXImport(null, project, null);
	}
}
