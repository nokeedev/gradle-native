package dev.nokee.ide.visualstudio.internal.vcxproj;

import lombok.Value;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Value
@Root(name = "Exec")
public class VCXExec implements VCXTask {
	@Attribute
	String command;

	@Attribute(required = false)
	String outputs;

	@Attribute
	String workingDirectory;
}
