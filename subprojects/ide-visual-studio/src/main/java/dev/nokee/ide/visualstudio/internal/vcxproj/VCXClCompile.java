package dev.nokee.ide.visualstudio.internal.vcxproj;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

public abstract class VCXClCompile {
	private VCXClCompile() {}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Root(name = "ClCompile")
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

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Root(name = "ClCompile")
	public static class Item implements VCXItem {
		@Attribute
		String include;

		@Element(required = false)
		@With String filter;

		public static Item of(String include) {
			return new Item(include, null);
		}
	}
}
