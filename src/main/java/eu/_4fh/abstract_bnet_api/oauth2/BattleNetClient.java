package eu._4fh.abstract_bnet_api.oauth2;

import org.dmfs.oauth2.client.OAuth2AccessToken;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A Client for the battle-net api. Either authorized for an user or just the
 * api client.
 */
@DefaultAnnotation(NonNull.class)
public interface BattleNetClient {
	/**
	 * Return the region of the client. Usually only used internally.
	 */
	BattleNetRegion getRegion();

	/**
	 * Return the access token. Usually only used internally.
	 */
	OAuth2AccessToken getAccessToken();

	/**
	 * Returns if the access token is valid. (For the api client, this always
	 * returns true because we can refresh the token.)
	 */
	boolean isAccessTokenValid();
}
