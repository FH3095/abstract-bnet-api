package eu._4fh.abstract_bnet_api.oauth2;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.oauth2.client.BasicOAuth2AuthorizationProvider;
import org.dmfs.oauth2.client.BasicOAuth2Client;
import org.dmfs.oauth2.client.BasicOAuth2ClientCredentials;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.oauth2.client.OAuth2Client;
import org.dmfs.oauth2.client.grants.ClientCredentialsGrant;
import org.dmfs.oauth2.client.scope.BasicScope;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.uris.LazyUri;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
public class ClientCache {
	private final Logger log = Logger.getLogger(this.getClass().getName());
	private final Map<Region, OAuth2Client> regionClients = new ConcurrentHashMap<>();
	private final Map<Region, OAuth2AccessToken> regionTokens = new ConcurrentHashMap<>();

	public final String oAuthApiKey;
	public final String oAuthApiSecret;
	public final int oAuthDefaultTokenDuration;
	public final String oAuthAuthRedirectTarget;
	public final String oAuthScope;

	private ClientCache(final String oAuthApiKey, final String oAuthApiSecret, final int oAuthDefaultTokenDuration,
			final String oAuthRedirectTarget, final String oAuthScope) {
		this.oAuthApiKey = oAuthApiKey;
		this.oAuthApiSecret = oAuthApiSecret;
		this.oAuthDefaultTokenDuration = oAuthDefaultTokenDuration;
		this.oAuthAuthRedirectTarget = oAuthRedirectTarget;
		this.oAuthScope = oAuthScope;
	}

	public OAuth2Client getClient(final String regionStr) {
		return getClient(Region.getRegion(regionStr));
	}

	private OAuth2Client getClient(final Region region) {
		return regionClients.computeIfAbsent(region, this::createClient);
	}

	private OAuth2Client createClient(final Region region) {
		final LazyUri redirectTarget = new LazyUri(new Precoded(oAuthAuthRedirectTarget));
		// Call fragement to force validation of the url
		redirectTarget.fragment();

		return new BasicOAuth2Client(
				new BasicOAuth2AuthorizationProvider(URI.create(region.oauthUrl + "/authorize"),
						URI.create(region.oauthUrl + "/token"),
						new Duration(1, oAuthDefaultTokenDuration,
								0) /* default expiration time in case the server doesn't return any */),
				new BasicOAuth2ClientCredentials(oAuthApiKey, oAuthApiSecret), redirectTarget);
	}

	public OAuth2AccessToken getAccessToken(final Region region) {
		return regionTokens.compute(region, (r, token) -> {
			try {
				if (token == null
						|| token.expirationDate().before(DateTime.now().addDuration(new Duration(1, 0, 0, 1, 0)))) {
					return createAccessToken(r);
				} else {
					return token;
				}
			} catch (ProtocolException | IOException | ProtocolError e) {
				throw new RuntimeException(e);
			}
		});
	}

	private OAuth2AccessToken createAccessToken(final Region region)
			throws IOException, ProtocolError, ProtocolException {
		final OAuth2AccessToken oAuthToken = new ClientCredentialsGrant(getClient(region), new BasicScope(oAuthScope))
				.accessToken(new HttpUrlConnectionExecutor());
		Objects.requireNonNull(oAuthToken, "Received no new token");
		log.log(Level.INFO, "Requested new token, got: {0} as {1} for {2} valid until {3}",
				new Object[] { oAuthToken.accessToken(), oAuthToken.tokenType(), oAuthToken.scope().toString(),
						oAuthToken.expirationDate().toString() });
		return oAuthToken;
	}
}
