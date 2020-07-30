package dev.nokee.platform.nativebase.internal;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.gradle.api.Named;

@Value
@EqualsAndHashCode(callSuper = false)
public class NamedTargetBuildType extends BaseTargetBuildType implements Named {
	String name;
}
