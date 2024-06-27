/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.sparrow.heart.feature.team;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum TeamVisibility {

    ALWAYS("always"),
    HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
    HIDE_FOR_OWN_TEAM("hideForOwnTeam"),
    NEVER("never");

    private final String id;

    TeamVisibility(@NotNull String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @NotNull
    public static TeamVisibility byId(String id) {
        return Arrays.stream(values())
                .filter(mode -> mode.id.equals(id))
                .findFirst()
                .orElse(ALWAYS);
    }
}
