package dev.nokee.ide.visualstudio.internal.vcxproj;

import lombok.Value;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.List;

@Value
@Root(name = "Target")
public class VCXTarget {
	@Attribute
	String name;

	@Attribute(required = false)
	String dependsOnTargets;

	@ElementListUnion({
		@ElementList(inline = true, required = false, type = VCXExec.class)
	})
	List<? extends VCXTask> tasks;
}
