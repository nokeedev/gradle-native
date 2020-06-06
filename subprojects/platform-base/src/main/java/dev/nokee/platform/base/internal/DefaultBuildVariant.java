package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import lombok.Value;
import org.gradle.api.GradleException;
import org.gradle.internal.Cast;

import java.util.List;

@Value
public class DefaultBuildVariant implements BuildVariant {
	List<Dimension> dimensions;

	public static DefaultBuildVariant of(Dimension... dimensions) {
		return new DefaultBuildVariant(ImmutableList.copyOf(dimensions));
	}

	public static DefaultBuildVariant of(Iterable<Dimension> dimensions) {
		return new DefaultBuildVariant(ImmutableList.copyOf(dimensions));
	}

	@Override
	public <T extends Dimension> T getAxisValue(DimensionType<T> type) {
		// TODO: We can validate the type of the value match the type of the dimension.
		return Cast.uncheckedCast(dimensions.stream().filter(it -> it.getType().equals(type)).findAny().orElseThrow(() -> new GradleException(String.format("Dimension '%s' is not part of this build variant.", type.toString()))));
	}

	@Override
	public boolean hasAxisValue(DimensionType<? extends Dimension> type) {
		return dimensions.stream().anyMatch(it -> it.getType().equals(type));
	}
}
