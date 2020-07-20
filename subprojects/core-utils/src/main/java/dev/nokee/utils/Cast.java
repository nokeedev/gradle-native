package dev.nokee.utils;

public abstract class Cast {
	private Cast() {}

	@SuppressWarnings("unchecked")
	public static <T> T uncheckedCast(String because, Object object) {
		return (T) object;
	}

	@SuppressWarnings("unchecked")
	public static <T> T uncheckedCastBecauseOfTypeErasure(Object object) {
		return (T) object;
	}
}
