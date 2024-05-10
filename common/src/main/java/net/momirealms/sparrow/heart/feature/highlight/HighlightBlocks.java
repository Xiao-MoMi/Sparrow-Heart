package net.momirealms.sparrow.heart.feature.highlight;

import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.entity.Player;

public class HighlightBlocks {

    private final int[] entityIDs;
    private final String teamName;

    public HighlightBlocks(int[] entityIDs, String teamName) {
        this.entityIDs = entityIDs;
        this.teamName = teamName;
    }

    public void destroy(Player player) {
        SparrowHeart.getInstance().removeClientSideTeam(player, teamName);
        SparrowHeart.getInstance().removeClientSideEntity(player, entityIDs);
    }

    public int[] entityIDs() {
        return entityIDs;
    }

    public String team() {
        return teamName;
    }
}
