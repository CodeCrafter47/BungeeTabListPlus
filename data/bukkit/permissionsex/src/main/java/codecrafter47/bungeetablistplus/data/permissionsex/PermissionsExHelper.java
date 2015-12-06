package codecrafter47.bungeetablistplus.data.permissionsex;

import java.util.List;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

public class PermissionsExHelper {

	public static PermissionGroup getMainPermissionGroupFromRank(PermissionUser pPermissionUser) {
		if (pPermissionUser == null)
			return null;

		List<PermissionGroup> groups = pPermissionUser.getParents();
		if (groups.size() <= 0)
			return null;

		PermissionGroup maingroup = null;
		for (PermissionGroup pg : groups) {
			if (pg.getRankLadder().equals("default") && (maingroup == null || pg.getRank() < maingroup.getRank()))
				maingroup = pg;
		}
		return maingroup;
	}

}
