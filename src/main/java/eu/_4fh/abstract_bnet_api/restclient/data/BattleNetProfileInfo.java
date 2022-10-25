package eu._4fh.abstract_bnet_api.restclient.data;

import java.util.Objects;

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

	@Override
	public String toString() {
		return "BattleNetProfileInfo [" + battleTag + " (" + id + ")]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(battleTag, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BattleNetProfileInfo)) {
			return false;
		}
		BattleNetProfileInfo other = (BattleNetProfileInfo) obj;
		return Objects.equals(battleTag, other.battleTag) && id == other.id;
	}
}
