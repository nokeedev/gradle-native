package dev.nokee.utils.internal;

import dev.nokee.ChainingAction;
import org.gradle.api.Action;

import java.io.Serializable;

public class NullAction<T> implements ChainingAction<T>, Action<T>, Serializable {
	public static final NullAction<?> DO_NOTHING = new NullAction<>();
	@Override
	public void execute(T t) {}
}
