package eu._4fh.abstract_bnet_api.oauth2;

import org.dmfs.oauth2.client.OAuth2AccessToken;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
public interface BattleNetClient {
	OAuth2AccessToken getAccessToken();

	boolean isAccessTokenValid();
}
