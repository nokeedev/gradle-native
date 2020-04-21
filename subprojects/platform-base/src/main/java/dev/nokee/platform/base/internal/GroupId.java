package dev.nokee.platform.base.internal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Value(staticConstructor = "of")
public class GroupId {
	@Getter(AccessLevel.NONE) Supplier<Object> value;

	public Optional<String> get() {
		return Optional.ofNullable(value.get()).map(Object::toString);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GroupId)) return false;
		GroupId groupId = (GroupId) o;
		return Objects.equals(get(), groupId.get());
	}

	@Override
	public int hashCode() {
		return Objects.hash(get());
	}

	@Override
	public String toString() {
		return "GroupId{" +
			"value=" + value.get() +
			'}';
	}
}
