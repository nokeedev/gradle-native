package dev.nokee.internal;

public abstract class Cast {
	private Cast() {}

	@SuppressWarnings("unchecked")
	public static <T> T uncheckedCast(String because, Object object) {
		return (T) object;
	}
}
