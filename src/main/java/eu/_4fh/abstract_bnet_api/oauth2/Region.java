package eu._4fh.abstract_bnet_api.oauth2;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * List of supported BNet Regions. Contains also supported Locales per Region.
 */
@DefaultAnnotation(NonNull.class)
public enum Region {
	US("https://oauth.battle.net", "https://us.api.blizzard.com", Locale.US, new Locale("es", "MX"),
			new Locale("pt", "BR")),
	EU("https://oauth.battle.net", "https://eu.api.blizzard.com", Locale.UK, new Locale("es", "ES"), Locale.FRANCE,
			new Locale("ru", "RU"), Locale.GERMANY, new Locale("pt", "PT"), Locale.ITALY),
	KR("https://oauth.battle.net", "https://kr.api.blizzard.com", Locale.KOREA),
	TW("https://oauth.battle.net", "https://tw.api.blizzard.com", Locale.TAIWAN);

	public final String oauthUrl;
	public final String apiUrl;
	public final Set<Locale> locales;

	public static Region getRegion(final String regionStr) {
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

	private Region(final String oauthUrl, final String apiUrl, final Locale... locales) {
		this.oauthUrl = oauthUrl;
		this.apiUrl = apiUrl;
		final Set<Locale> localesSet = ConcurrentHashMap.newKeySet(locales.length);
		localesSet.addAll(Arrays.asList(locales));
		this.locales = Collections.unmodifiableSet(localesSet);
	}

	public boolean isAllowedLocale(final String locale) {
		return locales.contains(Locale.forLanguageTag(locale));
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
}
