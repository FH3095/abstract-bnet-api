package eu._4fh.abstract_bnet_api.oauth2;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
/*package*/ class UserClient implements BattleNetClient {
	private static final Duration ONE_MINUTE = new Duration(1, 0, 0, 1, 0);

	private final Logger log = Logger.getLogger(this.getClass().getName());

	private final BattleNetRegion region;
	private OAuth2AccessToken token;

	public UserClient(final BattleNetRegion region, final OAuth2AccessToken token) {
		try {
			log.log(Level.INFO,
					"Requested new user-client token, got: {0} as {1} for {2} valid until {3} in region {4}",
					new Object[] { token.accessToken(), token.tokenType(), token.scope().toString(),
							token.expirationDate().toString(), region.toString() });
		} catch (ProtocolException e) {
			// Ignore only logging
		}
		this.region = region;
		this.token = token;
	}

	@Override
	public BattleNetRegion getRegion() {
		return region;
	}

	@Override
	public OAuth2AccessToken getAccessToken() {
		return token;
	}

	@Override
	public boolean isAccessTokenValid() {
		try {
			if (token.expirationDate().before(DateTime.now().addDuration(ONE_MINUTE))) {
				return false;
			}
		} catch (ProtocolException e) {
			throw new RuntimeException(e);
		}
		return true;
	}
}
