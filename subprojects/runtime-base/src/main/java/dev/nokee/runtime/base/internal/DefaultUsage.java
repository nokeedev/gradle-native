package dev.nokee.runtime.base.internal;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.gradle.api.attributes.Usage;

@Value
@EqualsAndHashCode(callSuper=false) // needed as Lombok fails in this case
public class DefaultUsage extends BaseNamed implements Usage {
	public DefaultUsage(String name) {
		super(name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Usage)) return false;
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
