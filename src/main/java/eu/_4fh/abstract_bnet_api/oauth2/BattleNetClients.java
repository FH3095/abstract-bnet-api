package eu._4fh.abstract_bnet_api.oauth2;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.oauth2.client.BasicOAuth2AuthorizationProvider;
import org.dmfs.oauth2.client.BasicOAuth2Client;
import org.dmfs.oauth2.client.BasicOAuth2ClientCredentials;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.oauth2.client.OAuth2Client;
import org.dmfs.oauth2.client.OAuth2InteractiveGrant;
import org.dmfs.oauth2.client.OAuth2InteractiveGrant.OAuth2GrantState;
import org.dmfs.oauth2.client.grants.AuthorizationCodeGrant;
import org.dmfs.oauth2.client.scope.BasicScope;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.uris.LazyUri;
import org.dmfs.rfc5545.Duration;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
public class BattleNetClients {
	public static class UserAuthorizationState implements Serializable {
		private static final long serialVersionUID = -3419942940565666963L;

		private final String regionStr;
		private final OAuth2GrantState grantState;
		private final URI authoriazionUrl;

		private UserAuthorizationState(final String regionStr, final OAuth2GrantState grantState,
				final URI authorizationUrl) {
			this.regionStr = regionStr;
			this.grantState = grantState;
			this.authoriazionUrl = authorizationUrl;
		}

		public URI getAuthorizationUrl() {
			return authoriazionUrl;
		}
	}

	private final Map<BattleNetRegion, ApiClient> regionClients = new ConcurrentHashMap<>();

	public final String oAuthApiKey;
	public final String oAuthApiSecret;
	public final int oAuthDefaultTokenDuration;
	public final String oAuthAuthRedirectTarget;
	public final String oAuthScope;

	private BattleNetClients(final String oAuthApiKey, final String oAuthApiSecret, final int oAuthDefaultTokenDuration,
			final String oAuthRedirectTarget, final String oAuthScope) {
		this.oAuthApiKey = oAuthApiKey;
		this.oAuthApiSecret = oAuthApiSecret;
		this.oAuthDefaultTokenDuration = oAuthDefaultTokenDuration;
		this.oAuthAuthRedirectTarget = oAuthRedirectTarget;
		this.oAuthScope = oAuthScope;
	}

	public BattleNetClient getClient(final String regionStr) {
		return getApiClient(BattleNetRegion.getRegion(regionStr));
	}

	private ApiClient getApiClient(final BattleNetRegion region) {
		return regionClients.computeIfAbsent(region, r -> new ApiClient(createClient(r), oAuthScope));
	}

	private OAuth2Client createClient(final BattleNetRegion region) {
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

	public UserAuthorizationState startUserAuthorizationProcess(final String regionStr) {
		final OAuth2InteractiveGrant grant = new AuthorizationCodeGrant(
				getApiClient(BattleNetRegion.getRegion(regionStr)).getClient(), new BasicScope(oAuthScope));
		return new UserAuthorizationState(regionStr, grant.state(), grant.authorizationUrl());
	}

	public BattleNetClient finishUserAuthorizationProcess(final UserAuthorizationState state, final String answerUrl) {
		try {
			final HttpRequestExecutor executor = new HttpUrlConnectionExecutor();
			final OAuth2InteractiveGrant grant = state.grantState
					.grant(getApiClient(BattleNetRegion.getRegion(state.regionStr)).getClient());
			final OAuth2AccessToken token = grant.withRedirect(new LazyUri(new Precoded(answerUrl)))
					.accessToken(executor);

			return new UserClient(token);
		} catch (ProtocolError | ProtocolException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
