package eu._4fh.abstract_bnet_api.restclient.data;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public class BattleNetWowCharacter {
	public final String name;
	public final String realmSlug;
	public final @CheckForNull Integer rank;

	public BattleNetWowCharacter(final String name, final String realmSlug, final @CheckForNull Integer rank) {
		this.name = name;
		this.realmSlug = realmSlug;
		this.rank = rank;
	}
}
