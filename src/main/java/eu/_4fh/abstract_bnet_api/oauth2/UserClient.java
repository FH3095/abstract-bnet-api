package eu._4fh.abstract_bnet_api.oauth2;

import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
/*package*/ class UserClient implements BattleNetClient {
	private static final Duration ONE_MINUTE = new Duration(1, 0, 0, 1, 0);

	private OAuth2AccessToken token;

	public UserClient(final OAuth2AccessToken token) {
		this.token = token;
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
