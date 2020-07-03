package dev.nokee.ide.visualstudio.internal.vcxproj;

import lombok.Value;
import org.simpleframework.xml.*;

import java.util.List;

@Value
@Root(name = "Project")
@Namespace(reference = "http://schemas.microsoft.com/developer/msbuild/2003")
public class VCXProject {
	@Attribute(required = false)
	String defaultTargets;

	@Attribute(required = false)
	String toolVersions;

	@ElementListUnion({
		@ElementList(inline = true, type = VCXItemGroup.class),
		@ElementList(inline = true, type = VCXPropertyGroup.class),
		@ElementList(inline = true, type = VCXImport.class),
		@ElementList(inline = true, type = VCXImportGroup.class),
		@ElementList(inline = true, type = VCXItemDefinitionGroup.class),
		@ElementList(inline = true, type = VCXTarget.class)
	})
	List<Object> nodes;
}
