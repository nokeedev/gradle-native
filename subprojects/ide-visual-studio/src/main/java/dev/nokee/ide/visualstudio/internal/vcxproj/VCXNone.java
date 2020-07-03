package dev.nokee.ide.visualstudio.internal.vcxproj;

import lombok.Value;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Value
@Root(name = "None")
public class VCXNone implements VCXItem {
	@Attribute
	String include;
}
