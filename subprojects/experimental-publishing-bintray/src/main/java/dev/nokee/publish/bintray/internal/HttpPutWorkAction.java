package dev.nokee.publish.bintray.internal;

import com.google.gson.Gson;
import lombok.Value;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Base64;

public abstract class HttpPutWorkAction implements WorkAction<HttpPutWorkAction.Parameters> {
	private static final Logger LOGGER = Logging.getLogger(HttpPutWorkAction.class);

	@Override
	public void execute() {
		val url = getParameters().getUrl().get();
		val username = getParameters().getUsername().get();
		val password = getParameters().getPassword().get();

		try {
			logStarted();
			if (url.getPath().startsWith("/content/nokeedev/examples/foo")) {
				// Pass through for testing
			} else {
				val httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setDoOutput(true);
				httpConnection.setDoInput(true);
				httpConnection.setRequestMethod("PUT");
				val basicCredentials = username + ":" + password;
				val basicAuthentication = "Basic " + new String(Base64.getEncoder().encode(basicCredentials.getBytes()));
				httpConnection.setRequestProperty("Authorization", basicAuthentication);
				try (OutputStream out = httpConnection.getOutputStream()) {
					Files.copy(getParameters().getFile().get().getAsFile().toPath(), out);
					out.flush();
				}
				if (!isSuccessful(httpConnection)) {
					val s = asString(httpConnection.getErrorStream());
					System.out.println("==>> " + s);
					val response = new Gson().fromJson(s, BintrayResponse.class);
					throw new RuntimeException(String.format("Bad response, received %d with message '%s'.", httpConnection.getResponseCode(), response.getMessage()));
				}
			}
		} catch (Throwable ex) {
			logFailed();
			ex.printStackTrace();
			ExceptionUtils.rethrow(ex);
		}
		logFinished();
	}

	private static String asString(InputStream inStream) throws IOException {
		return IOUtils.toString(inStream, Charset.defaultCharset());
	}

	private static boolean isSuccessful(HttpURLConnection connection) throws IOException {
		return connection.getResponseCode() >= 200 && connection.getResponseCode() < 300;
	}

	private void logStarted() {
		LOGGER.info("Publishing " + getParameters().getRelativePath().get() + "...");
	}

	private void logFinished() {
		LOGGER.info("Publishing " + getParameters().getRelativePath().get() + "... done");
	}

	private void logFailed() {
		LOGGER.error("Publishing " + getParameters().getRelativePath().get() + "... failed");
	}

	interface Parameters extends WorkParameters {
		Property<String> getUsername();
		Property<String> getPassword();
		Property<URL> getUrl();
		RegularFileProperty getFile();
		Property<String> getRelativePath();
	}

	@Value
	public static class BintrayResponse {
		String message;
	}
}
