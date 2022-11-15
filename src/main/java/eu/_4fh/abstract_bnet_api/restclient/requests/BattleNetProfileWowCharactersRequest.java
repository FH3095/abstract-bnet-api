package eu._4fh.abstract_bnet_api.restclient.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu._4fh.abstract_bnet_api.restclient.AbstractBattleNetRequest;
import eu._4fh.abstract_bnet_api.restclient.BattleNetRequestType;
import eu._4fh.abstract_bnet_api.restclient.data.BattleNetWowCharacter;

public class BattleNetProfileWowCharactersRequest extends AbstractBattleNetRequest<List<BattleNetWowCharacter>> {
	public static final String API_PATH = "/profile/user/wow";

	@Override
	protected List<BattleNetWowCharacter> convertJsonToObject(JSONObject arrayObj) {
		final List<BattleNetWowCharacter> result = new ArrayList<>();
		final JSONArray wowAccounts = arrayObj.getJSONArray("wow_accounts");
		for (int i = 0; i < wowAccounts.length(); ++i) {
			final JSONArray characters = wowAccounts.getJSONObject(i).getJSONArray("characters");
			for (int j = 0; j < characters.length(); ++j) {
				final JSONObject obj = characters.getJSONObject(j);
				final BattleNetWowCharacter character = new BattleNetWowCharacter(obj.getLong("id"),
						obj.getString("name"), obj.getJSONObject("realm").getString("slug"), null);
				result.add(character);
			}
		}

		return result;
	}

	@Override
	protected BattleNetRequestType getRequestType() {
		return BattleNetRequestType.PROFILE_WITH_LOCALE;
	}
}
