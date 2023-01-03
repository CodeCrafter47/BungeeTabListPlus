/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.api.velocity;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.UUID;

/**
 * An icon shown in the tab list.
 */
@Data
public class Icon implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID player;
    @NonNull
    private final String[][] properties;

    /**
     * The default icon. The client will show a random Alex/ Steve face when using this.
     */
    public static final Icon DEFAULT = new Icon(null, new String[0][]);
}
