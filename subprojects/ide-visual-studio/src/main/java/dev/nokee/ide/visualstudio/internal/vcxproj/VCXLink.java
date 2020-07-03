package dev.nokee.ide.visualstudio.internal.vcxproj;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

public abstract class VCXLink {
	private VCXLink() {}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Root(name = "Link")
	public static class Definition implements VCXItemDefinition {
		@ElementList(inline = true)
		List<VCXProperty> properties;

		public static Definition of(Iterable<VCXProperty> properties) {
			return new Definition(ImmutableList.copyOf(properties));
		}

		public static Definition of(VCXProperty... properties) {
			return new Definition(ImmutableList.copyOf(properties));
		}
	}
}
