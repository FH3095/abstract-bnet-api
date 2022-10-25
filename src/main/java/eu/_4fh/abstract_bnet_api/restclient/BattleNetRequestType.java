package eu._4fh.abstract_bnet_api.restclient;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
public enum BattleNetRequestType {
	OAUTH(),
	STATIC_WITH_LOCALE("static", true),
	STATIC_WITHOUT_LOCALE("static", false),
	DYNAMIC_WITH_LOCALE("dynamic", true),
	DYNAMIC_WITHOUT_LOCALE("dynamic", false),
	PROFILE_WITH_LOCALE("profile", true),
	PROFILE_WITHOUT_LOCALE("profile", false);

	public final boolean usesOAuthUrl;
	public final String battleNetNamespacePrefix;
	public final boolean withLocale;

	private BattleNetRequestType(final String battleNetNamespacePrefix, final boolean withLocale) {
		usesOAuthUrl = false;
		this.battleNetNamespacePrefix = battleNetNamespacePrefix;
		this.withLocale = withLocale;
	}

	private BattleNetRequestType() {
		usesOAuthUrl = true;
		this.battleNetNamespacePrefix = "";
		this.withLocale = false;
	}
}
