package dev.nokee.ide.base.internal;

public interface IdeRequest {
	IdeRequestAction getAction();
	String getTaskName();
}
