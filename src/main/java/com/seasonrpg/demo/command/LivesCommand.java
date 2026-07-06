package com.seasonrpg.demo.command;

import com.seasonrpg.demo.lives.LivesManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /lives - mostra as vidas restantes do jogador. */
public final class LivesCommand implements CommandExecutor {

    private final LivesManager lives;

    public LivesCommand(LivesManager lives) {
        this.lives = lives;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores tem vidas.", NamedTextColor.RED));
            return true;
        }
        int n = lives.getLives(player.getUniqueId());
        sender.sendMessage(Component.text("Voce tem " + n + " vida(s) nesta temporada.",
                n <= 1 ? NamedTextColor.RED : NamedTextColor.YELLOW));
        return true;
    }
}
