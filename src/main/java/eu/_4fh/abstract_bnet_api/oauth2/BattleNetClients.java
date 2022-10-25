package eu._4fh.abstract_bnet_api.oauth2;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;
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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import eu._4fh.abstract_bnet_api.restclient.RequestExecutor;
import eu._4fh.abstract_bnet_api.util.Pair;

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
	@DefaultAnnotation(NonNull.class)
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

	@DefaultAnnotation(NonNull.class)
	public static class UserAuthorizationError extends Exception {
		private static final long serialVersionUID = 5963598473840020759L;

		public UserAuthorizationError(final String message, final @CheckForNull String description) {
			super(message + (description != null && !description.isBlank() ? " {" + description + "}" : ""));
		}
	}

	private final Map<BattleNetRegion, ApiClient> regionClients = new ConcurrentHashMap<>();
	private final Map<BattleNetRegion, List<BattleNetClient>> userClients = new ConcurrentHashMap<>();

	private final String oAuthApiKey;
	private final String oAuthApiSecret;
	private final int oAuthDefaultTokenDuration;
	private final String oAuthAuthRedirectTarget;
	private final String oAuthScope;

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
	public BattleNetClient finishUserAuthorizationProcess(final UserAuthorizationState state, final String answerUrl)
			throws UserAuthorizationError {
		final Pair<String, String> error = getErrorFromUrl(answerUrl);
		if (error != null) {
			throw new UserAuthorizationError(error.value1, error.value2);
		}

		try {
			final BattleNetRegion region = BattleNetRegion.getRegion(state.regionStr);
			final HttpRequestExecutor executor = new HttpUrlConnectionExecutor();
			final OAuth2InteractiveGrant grant = state.grantState.grant(getApiClient(region).getClient());
			final OAuth2AccessToken token = grant.withRedirect(new LazyUri(new Precoded(answerUrl)))
					.accessToken(executor);

			final UserClient client = new UserClient(region, token);
			userClients.computeIfAbsent(region, r -> Collections.synchronizedList(new LinkedList<>())).add(client);
			cleanupUserClients();
			return client;
		} catch (ProtocolError | ProtocolException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private @CheckForNull Pair<String, String> getErrorFromUrl(final String url) {
		try {
			final List<NameValuePair> values = new URIBuilder(url).getQueryParams();
			@CheckForNull
			String error = null;
			@CheckForNull
			String errorDescription = null;
			for (final NameValuePair pair : values) {
				if (pair.getName() == null || pair.getName().isBlank() || pair.getValue() == null
						|| pair.getValue().isBlank()) {
					continue;
				}
				if ("error".equalsIgnoreCase(pair.getName()) && error == null) {
					error = pair.getValue();
				} else if ("error_description".equalsIgnoreCase(pair.getName()) && errorDescription == null) {
					errorDescription = pair.getValue();
				}
			}

			if (error == null) {
				return null;
			} else {
				return new Pair<>(error, errorDescription);
			}
		} catch (URISyntaxException e) {
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
