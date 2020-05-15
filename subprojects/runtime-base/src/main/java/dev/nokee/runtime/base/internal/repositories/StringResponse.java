package dev.nokee.runtime.base.internal.repositories;

public class StringResponse implements Response {
	private final String content;

	public StringResponse(String content) {
		this.content = content;
	}

	@Override
	public String getContentType() {
		return "text/plain; charset=utf-8";
	}

	@Override
	public String getContent() {
		return content;
	}
}
