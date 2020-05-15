package dev.nokee.runtime.base.internal.repositories;

import com.google.common.hash.Hashing;
import org.apache.commons.io.FilenameUtils;

import java.nio.charset.Charset;
import java.util.Optional;

public class ContentHashingHandler implements RouteHandler {
	private final RouteHandler delegate;

	ContentHashingHandler(RouteHandler delegate) {
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
		return new StringResponse(Hashing.sha1().hashString(content.getContent(), Charset.defaultCharset()).toString());
	}
}
