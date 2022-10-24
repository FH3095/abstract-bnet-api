package eu._4fh.abstract_bnet_api.rest_client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.dmfs.httpessentials.client.HttpRequest;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.oauth2.client.http.decorators.BearerAuthenticatedRequest;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import eu._4fh.abstract_bnet_api.oauth2.ClientCache;
import eu._4fh.abstract_bnet_api.oauth2.Region;

@DefaultAnnotation(NonNull.class)
public class RequestExecutor {
	private final ClientCache cache;
	private final Region region;
	private final String locale;
	private final HttpUrlConnectionExecutor executor;

	public RequestExecutor(final ClientCache cache, final Region region, String locale) {
		locale = locale.replace('-', '_');
		if (!region.isAllowedLocale(locale.replace('_', '-'))) {
			throw new IllegalArgumentException("Locale " + locale + " is not allowed for region " + region);
		}
		executor = new HttpUrlConnectionExecutor();
		this.cache = cache;
		this.region = region;
		this.locale = locale;
	}

	public <T> T executeRequest(final String path, final AbstractBNetRequest<T> request, final boolean withLocale) {
		try {
			final URI apiUri = URI.create(region.apiUrl);
			final URI uri = new URI(apiUri.getScheme(), apiUri.getAuthority(), path, "namespace="
					+ request.getNamespacePrefix() + "-" + region.toString() + (withLocale ? "&locale=" + locale : ""),
					null);
			final HttpRequest<T> authenticatedRequest = new BearerAuthenticatedRequest<>(request,
					cache.getAccessToken(region));
			return executor.execute(uri, authenticatedRequest);
		} catch (IOException | ProtocolError | ProtocolException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
