package dev.nokee.ide.base.internal;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdeRequestAction {
	public static final IdeRequestAction BUILD = new IdeRequestAction("build");
	public static final IdeRequestAction CLEAN = new IdeRequestAction("clean");

	String identifier;

	public static IdeRequestAction valueOf(String name) {
		return ImmutableSet.of(BUILD, CLEAN)
			.stream()
			.filter(it -> it.getIdentifier().equals(name.toLowerCase()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Unrecognized bridge action '%s'.", name)));
	}
}
