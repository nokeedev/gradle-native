package dev.nokee.platform.nativebase.internal.repositories;

import org.apache.commons.io.FilenameUtils;
import org.gradle.internal.hash.Hashing;

import java.util.Optional;

public class ContentHashingHandler implements Handler {
	private final Handler delegate;

	ContentHashingHandler(Handler delegate) {
		this.delegate = delegate;
	}

	@Override
	public Optional<Response> handle(String target) {
		if (FilenameUtils.isExtension(target, "sha1")) {
			return delegate.handle(FilenameUtils.removeExtension(target)).map(ContentHashingHandler::sha1);
		}
		return delegate.handle(target);
	}

	private static Response sha1(Response content) {
		return new StringResponse(Hashing.sha1().hashString(content.getContent()).toString());
	}
}
