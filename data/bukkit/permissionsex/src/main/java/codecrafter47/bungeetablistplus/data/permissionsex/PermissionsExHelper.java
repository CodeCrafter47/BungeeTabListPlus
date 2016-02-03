package codecrafter47.bungeetablistplus.data.permissionsex;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import java.util.List;

public class PermissionsExHelper {

	public static PermissionGroup getMainPermissionGroupFromRank(PermissionUser pPermissionUser) {
		if (pPermissionUser == null)
			return null;

		List<PermissionGroup> groups = pPermissionUser.getParents();
		if (groups.size() <= 0)
			return null;

		if (groups.size() == 1) {
			return groups.get(0);
		}

		PermissionGroup maingroup = null;
		for (PermissionGroup pg : groups) {
			if (pg.isRanked() && pg.getRankLadder().equals("default") && (maingroup == null || pg.getRank() < maingroup.getRank()))
				maingroup = pg;
		}
		return maingroup;
	}

}
