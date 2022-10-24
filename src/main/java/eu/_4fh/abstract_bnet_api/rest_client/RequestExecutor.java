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
import eu._4fh.abstract_bnet_api.oauth2.BattleNetClient;
import eu._4fh.abstract_bnet_api.oauth2.BattleNetRegion;

@DefaultAnnotation(NonNull.class)
public class RequestExecutor {
	private final BattleNetClient client;
	private final BattleNetRegion region;
	private final String locale;
	private final HttpUrlConnectionExecutor executor;

	public RequestExecutor(final BattleNetClient client, final BattleNetRegion battleNetRegion, String locale) {
		locale = locale.replace('-', '_');
		if (!battleNetRegion.isAllowedLocale(locale.replace('_', '-'))) {
			throw new IllegalArgumentException("Locale " + locale + " is not allowed for region " + battleNetRegion);
		}
		executor = new HttpUrlConnectionExecutor();
		this.client = client;
		this.region = battleNetRegion;
		this.locale = locale;
	}

	public <T> T executeRequest(final String path, final AbstractBattleNetRequest<T> request) {
		try {
			final URI apiUri = URI.create(region.apiUrl);
			final URI uri = new URI(apiUri.getScheme(), apiUri.getAuthority(), path,
					"namespace=" + request.getNamespacePrefix() + "-" + region.toString()
							+ (request.needsLocale() ? "&locale=" + locale : ""),
					null);
			final HttpRequest<T> authenticatedRequest = new BearerAuthenticatedRequest<>(request,
					client.getAccessToken());
			return executor.execute(uri, authenticatedRequest);
		} catch (IOException | ProtocolError | ProtocolException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
