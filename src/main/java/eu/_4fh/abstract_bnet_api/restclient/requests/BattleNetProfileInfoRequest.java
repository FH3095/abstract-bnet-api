package eu._4fh.abstract_bnet_api.restclient.requests;

import org.json.JSONObject;

import eu._4fh.abstract_bnet_api.restclient.AbstractBattleNetRequest;
import eu._4fh.abstract_bnet_api.restclient.BattleNetRequestType;
import eu._4fh.abstract_bnet_api.restclient.data.BattleNetProfileInfo;

public class BattleNetProfileInfoRequest extends AbstractBattleNetRequest<BattleNetProfileInfo> {
	@Override
	protected BattleNetProfileInfo convertJsonToObject(JSONObject obj) {
		return new BattleNetProfileInfo(obj.getLong("id"), obj.getString("battletag"));
	}

	@Override
	protected BattleNetRequestType getRequestType() {
		return BattleNetRequestType.OAUTH;
	}
}
