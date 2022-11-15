package eu._4fh.abstract_bnet_api.oauth2;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * List of supported BNet Regions. Contains also supported Locales per
 * BattleNetRegion.
 */
@DefaultAnnotation(NonNull.class)
public enum BattleNetRegion {
	US("us", "https://oauth.battle.net", "https://us.api.blizzard.com", Locale.US, new Locale("es", "MX"),
			new Locale("pt", "BR")),
	EU("eu", "https://oauth.battle.net", "https://eu.api.blizzard.com", Locale.UK, new Locale("es", "ES"),
			Locale.FRANCE, new Locale("ru", "RU"), Locale.GERMANY, new Locale("pt", "PT"), Locale.ITALY),
	KR("kr", "https://oauth.battle.net", "https://kr.api.blizzard.com", Locale.KOREA),
	TW("tw", "https://oauth.battle.net", "https://tw.api.blizzard.com", Locale.TAIWAN);

	public final String namespaceName;
	public final String oauthUrl;
	public final String apiUrl;
	public final Set<Locale> locales;

	public static BattleNetRegion getRegion(final String regionStr) {
		switch (regionStr.toUpperCase(Locale.ROOT)) {
		case "US":
			return US;
		case "EU":
			return EU;
		case "KR":
			return KR;
		case "TW":
			return TW;
		default:
			throw new IllegalArgumentException("Unknown region " + regionStr.toUpperCase(Locale.ROOT));
		}
	}

	BattleNetRegion(final String namespaceName, final String oauthUrl, final String apiUrl, final Locale... locales) {
		this.namespaceName = namespaceName;
		this.oauthUrl = oauthUrl;
		this.apiUrl = apiUrl;
		this.locales = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(locales)));
	}

	public boolean isAllowedLocale(final String locale) {
		return locales.contains(Locale.forLanguageTag(locale));
	}

	public String getRegionName() {
		return name();
	}
}
