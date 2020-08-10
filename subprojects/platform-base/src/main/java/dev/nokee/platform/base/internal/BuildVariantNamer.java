package dev.nokee.platform.base.internal;

import dev.nokee.runtime.base.internal.Dimension;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;
import org.gradle.api.Namer;

import java.util.stream.Collectors;

public class BuildVariantNamer implements Namer<BuildVariant> {
	public static final BuildVariantNamer INSTANCE = new BuildVariantNamer();

	@Override
	public String determineName(BuildVariant buildVariant) {
		return StringUtils.uncapitalize(buildVariant.getDimensions().stream().map(this::determineName).map(StringUtils::capitalize).collect(Collectors.joining()));
	}

	private String determineName(Dimension dimension) {
		if (dimension instanceof Named) {
			return ((Named) dimension).getName();
		}
		throw new IllegalArgumentException("Can't determine name");
	}
}
