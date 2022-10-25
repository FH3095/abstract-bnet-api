package eu._4fh.abstract_bnet_api.restclient.data;

import java.util.Objects;

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

	@Override
	public String toString() {
		return "BattleNetWowCharacter [" + name + "-" + realmSlug + " rank="
				+ (rank == null ? "unknown" : rank.toString()) + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, rank, realmSlug);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BattleNetWowCharacter)) {
			return false;
		}
		BattleNetWowCharacter other = (BattleNetWowCharacter) obj;
		return Objects.equals(name, other.name) && Objects.equals(rank, other.rank)
				&& Objects.equals(realmSlug, other.realmSlug);
	}
}
