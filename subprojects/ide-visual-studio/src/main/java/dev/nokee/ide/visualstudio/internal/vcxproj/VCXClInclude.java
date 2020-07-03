package dev.nokee.ide.visualstudio.internal.vcxproj;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

public abstract class VCXClInclude {
	private VCXClInclude() {}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Root(name = "ClInclude")
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
