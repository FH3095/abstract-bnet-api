package eu._4fh.abstract_bnet_api.restclient.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu._4fh.abstract_bnet_api.restclient.AbstractBattleNetRequest;
import eu._4fh.abstract_bnet_api.restclient.BattleNetRequestType;
import eu._4fh.abstract_bnet_api.restclient.data.BattleNetWowCharacter;

public class BNetGuildMembersRequest extends AbstractBattleNetRequest<List<BattleNetWowCharacter>> {

	@Override
	protected List<BattleNetWowCharacter> convertJsonToObject(JSONObject guildObj) {
		final List<BattleNetWowCharacter> result = new ArrayList<>();
		final JSONArray array = guildObj.getJSONArray("members");
		for (int i = 0; i < array.length(); ++i) {
			final JSONObject obj = array.getJSONObject(i).getJSONObject("character");
			final int rank = array.getJSONObject(i).optInt("rank", Integer.MAX_VALUE);
			final BattleNetWowCharacter character = new BattleNetWowCharacter(obj.getString("name"),
					obj.getJSONObject("realm").getString("slug"), rank == Integer.MAX_VALUE ? null : rank);
			result.add(character);
		}

		return result;
	}

	@Override
	protected BattleNetRequestType getRequestType() {
		return BattleNetRequestType.PROFILE_WITH_LOCALE;
	}
}
