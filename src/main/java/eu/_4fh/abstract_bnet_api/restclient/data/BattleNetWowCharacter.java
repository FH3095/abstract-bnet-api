package eu._4fh.abstract_bnet_api.restclient.data;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public class BattleNetWowCharacter {
	public final long id;
	public final String name;
	public final String realmSlug;
	public final @CheckForNull Byte guildRank;

	public BattleNetWowCharacter(final long id, final String name, final String realmSlug,
			final @CheckForNull Byte guildRank) {
		this.id = id;
		this.name = name;
		this.realmSlug = realmSlug;
		this.guildRank = guildRank;
	}

	@Override
	public String toString() {
		return "BattleNetWowCharacter [" + name + "-" + realmSlug + " (" + id + ") rank="
				+ (guildRank == null ? "unknown" : guildRank.toString()) + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(guildRank, id, name, realmSlug);
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
		return Objects.equals(guildRank, other.guildRank) && id == other.id && Objects.equals(name, other.name)
				&& Objects.equals(realmSlug, other.realmSlug);
	}
}
