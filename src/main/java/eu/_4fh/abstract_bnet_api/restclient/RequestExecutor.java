package eu._4fh.abstract_bnet_api.restclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dmfs.httpessentials.client.HttpRequest;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.oauth2.client.http.decorators.BearerAuthenticatedRequest;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import eu._4fh.abstract_bnet_api.oauth2.BattleNetClient;
import eu._4fh.abstract_bnet_api.oauth2.BattleNetRegion;

/**
 * Used to execute requests.
 */
@DefaultAnnotation(NonNull.class)
public class RequestExecutor {
	private final BattleNetClient client;
	private final String locale;
	private final HttpUrlConnectionExecutor executor;

	public RequestExecutor(final BattleNetClient client, String locale) {
		locale = locale.replace('-', '_');
		if (!client.getRegion().isAllowedLocale(locale.replace('_', '-'))) {
			throw new IllegalArgumentException("Locale " + locale + " is not allowed for region " + client.getRegion());
		}
		executor = new HttpUrlConnectionExecutor();
		this.client = client;
		this.locale = locale;
	}

	public <T> T executeRequest(final String path, final AbstractBattleNetRequest<T> request) {
		try {
			final BattleNetRegion region = client.getRegion();

			final List<String> queryParts = new ArrayList<>(2);
			final BattleNetRequestType requestType = request.getRequestType();
			if (!requestType.battleNetNamespacePrefix.isBlank()) {
				queryParts.add("namespace=" + requestType.battleNetNamespacePrefix + "-" + region.namespaceName);
			}
			if (requestType.withLocale) {
				queryParts.add("locale=" + locale);
			}

			final URI apiUri = URI.create(requestType.usesOAuthUrl ? region.oauthUrl : region.apiUrl);
			final URI uri = new URI(apiUri.getScheme(), apiUri.getAuthority(), path,
					queryParts.stream().collect(Collectors.joining("&")), null);
			final HttpRequest<T> authenticatedRequest = new BearerAuthenticatedRequest<>(request,
					client.getAccessToken());
			return executor.execute(uri, authenticatedRequest);
		} catch (IOException | ProtocolError | ProtocolException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
