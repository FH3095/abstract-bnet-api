package eu._4fh.abstract_bnet_api.restclient;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
public enum BattleNetRequestType {
	OAUTH("", false),
	STATIC_WITH_LOCALE("static", true),
	STATIC_WITHOUT_LOCALE("static", false),
	DYNAMIC_WITH_LOCALE("dynamic", true),
	DYNAMIC_WITHOUT_LOCALE("dynamic", false),
	PROFILE_WITH_LOCALE("profile", true),
	PROFILE_WITHOUT_LOCALE("profile", false);

	public final String battleNetNamespacePrefix;
	public final boolean withLocale;

	BattleNetRequestType(final String battleNetNamespacePrefix, final boolean withLocale) {
		this.battleNetNamespacePrefix = battleNetNamespacePrefix;
		this.withLocale = withLocale;
	}
}
