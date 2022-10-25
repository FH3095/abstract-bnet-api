package eu._4fh.abstract_bnet_api.oauth2;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.oauth2.client.OAuth2Client;
import org.dmfs.oauth2.client.grants.ClientCredentialsGrant;
import org.dmfs.oauth2.client.scope.BasicScope;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
/*package*/ class ApiClient implements BattleNetClient {
	private static final Duration ONE_MINUTE = new Duration(1, 0, 0, 1, 0);

	private final Logger log = Logger.getLogger(this.getClass().getName());

	private final BattleNetRegion region;
	private final OAuth2Client client;
	private @CheckForNull OAuth2AccessToken token;
	private final String oAuthScope;

	public ApiClient(final BattleNetRegion region, final OAuth2Client client, final String oAuthScope) {
		this.region = region;
		this.client = client;
		this.oAuthScope = oAuthScope;
		token = null;
	}

	@Override
	public BattleNetRegion getRegion() {
		return region;
	}

	public OAuth2Client getClient() {
		return client;
	}

	@Override
	public boolean isAccessTokenValid() {
		// Token can be refreshed and is therefor always valid
		return true;
	}

	@Override
	public synchronized OAuth2AccessToken getAccessToken() {
		try {
			if (token == null || token.expirationDate().before(DateTime.now().addDuration(ONE_MINUTE))) {
				token = createAccessToken();
			}
		} catch (ProtocolException | IOException | ProtocolError e) {
			throw new RuntimeException(e);
		}
		return token;
	}

	private OAuth2AccessToken createAccessToken() throws IOException, ProtocolError, ProtocolException {
		final OAuth2AccessToken oAuthToken = new ClientCredentialsGrant(getClient(), new BasicScope(oAuthScope))
				.accessToken(new HttpUrlConnectionExecutor());
		Objects.requireNonNull(oAuthToken, "Received no new token");
		log.log(Level.INFO, "Requested new api-client token, got: {0} as {1} for {2} valid until {3} in region {4}",
				new Object[] { oAuthToken.accessToken(), oAuthToken.tokenType(), oAuthToken.scope().toString(),
						oAuthToken.expirationDate().toString(), region.toString() });
		return oAuthToken;
	}
}
