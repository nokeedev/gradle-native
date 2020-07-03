package dev.nokee.ide.visualstudio.internal.vcxproj;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Root(name = "PropertyGroup")
public class VCXPropertyGroup {
	@Attribute(required = false)
	@With String condition;

	@Attribute(required = false)
	@With String label;

	@ElementList(inline = true)
	List<VCXProperty> properties;

	public static VCXPropertyGroup empty(String label) {
		return new VCXPropertyGroup(null, label, ImmutableList.of());
	}

	public static VCXPropertyGroup of(Iterable<VCXProperty> properties) {
		return new VCXPropertyGroup(null, null, ImmutableList.copyOf(properties));
	}

	public static VCXPropertyGroup of(VCXProperty... properties) {
		return new VCXPropertyGroup(null, null, ImmutableList.copyOf(properties));
	}
}
