package eu._4fh.abstract_bnet_api.oauth2;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
import eu._4fh.abstract_bnet_api.restclient.RequestExecutor;

/**
 * This class can be used to get relevant BattleNetClients to create
 * {@link RequestExecutor}s
 * ({@link RequestExecutor#RequestExecutor(BattleNetClient, String)}).
 */
@DefaultAnnotation(NonNull.class)
public class BattleNetClients {
	/**
	 * Class to remember relevant values from
	 * {@link BattleNetClients#startUserAuthorizationProcess(String)}. This
	 * class is intentionally Serializable to make it possible to remember this
	 * values somewhere.
	 */
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
	private final Map<BattleNetRegion, List<BattleNetClient>> userClients = new ConcurrentHashMap<>();

	public final String oAuthApiKey;
	public final String oAuthApiSecret;
	public final int oAuthDefaultTokenDuration;
	public final String oAuthAuthRedirectTarget;
	public final String oAuthScope;

	public BattleNetClients(final String oAuthApiKey, final String oAuthApiSecret, final int oAuthDefaultTokenDuration,
			final String oAuthRedirectTarget, final String oAuthScope) {
		this.oAuthApiKey = oAuthApiKey;
		this.oAuthApiSecret = oAuthApiSecret;
		this.oAuthDefaultTokenDuration = oAuthDefaultTokenDuration;
		this.oAuthAuthRedirectTarget = oAuthRedirectTarget;
		this.oAuthScope = oAuthScope;
	}

	/**
	 * Return a client for access to the battle net api. These client can only
	 * be used to access data, that does not require an authorization code flow.
	 */
	public BattleNetClient getClient(final String regionStr) {
		return getApiClient(BattleNetRegion.getRegion(regionStr));
	}

	private ApiClient getApiClient(final BattleNetRegion region) {
		return regionClients.computeIfAbsent(region, r -> new ApiClient(region, createClient(r), oAuthScope));
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

	/**
	 * Starts an authorization process, to access users data on their behalf.
	 * The client should be redirected to the url given in
	 * {@link UserAuthorizationState#getAuthorizationUrl()}.
	 */
	public UserAuthorizationState startUserAuthorizationProcess(final String regionStr) {
		final OAuth2InteractiveGrant grant = new AuthorizationCodeGrant(
				getApiClient(BattleNetRegion.getRegion(regionStr)).getClient(), new BasicScope(oAuthScope));
		return new UserAuthorizationState(regionStr, grant.state(), grant.authorizationUrl());
	}

	/**
	 * Finishes the authorization process.
	 */
	public BattleNetClient finishUserAuthorizationProcess(final UserAuthorizationState state, final String answerUrl) {
		try {
			final BattleNetRegion region = BattleNetRegion.getRegion(state.regionStr);
			final HttpRequestExecutor executor = new HttpUrlConnectionExecutor();
			final OAuth2InteractiveGrant grant = state.grantState.grant(getApiClient(region).getClient());
			final OAuth2AccessToken token = grant.withRedirect(new LazyUri(new Precoded(answerUrl)))
					.accessToken(executor);

			final UserClient client = new UserClient(region, token);
			userClients.computeIfAbsent(region, r -> Collections.synchronizedList(new LinkedList<>())).add(client);
			cleanupUserClients();
			return new UserClient(region, token);
		} catch (ProtocolError | ProtocolException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<BattleNetClient> getUserClients(final String regionStr) {
		cleanupUserClients();
		return userClients.getOrDefault(BattleNetRegion.getRegion(regionStr), Collections.emptyList());
	}

	private void cleanupUserClients() {
		userClients.forEach((region, list) -> list.removeIf(client -> !client.isAccessTokenValid()));
	}
}
