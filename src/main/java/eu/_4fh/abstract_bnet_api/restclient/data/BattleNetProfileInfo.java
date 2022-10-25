package eu._4fh.abstract_bnet_api.restclient.data;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

@DefaultAnnotation(NonNull.class)
public class BattleNetProfileInfo {
	public final long id;
	public final String battleTag;

	public BattleNetProfileInfo(final long id, final String battleTag) {
		this.id = id;
		this.battleTag = battleTag;
	}
}
