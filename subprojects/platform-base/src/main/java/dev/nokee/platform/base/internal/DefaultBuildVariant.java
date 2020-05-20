package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import lombok.Value;

import java.util.List;

@Value
public class DefaultBuildVariant implements BuildVariant {
	List<Dimension> dimensions;

	public static DefaultBuildVariant of(Dimension... dimensions) {
		return new DefaultBuildVariant(ImmutableList.copyOf(dimensions));
	}
}
