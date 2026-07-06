package com.seasonrpg.demo.command;

import com.seasonrpg.demo.lives.LivesManager;
import com.seasonrpg.demo.season.SeasonManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /season - resumo da temporada para o jogador. */
public final class SeasonCommand implements CommandExecutor {

    private final SeasonManager season;
    private final LivesManager lives;

    public SeasonCommand(SeasonManager season, LivesManager lives) {
        this.season = season;
        this.lives = lives;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        sender.sendMessage(Component.text("=== Temporada " + season.getSeasonNumber() + " ===",
                NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("Semana " + season.currentWeek() + " de "
                + season.getTotalWeeks(), NamedTextColor.WHITE));
        sender.sendMessage(Component.text("Termina em: " + season.formatRemaining(), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Objetivo: " + season.getObjective(), NamedTextColor.GRAY));
        if (sender instanceof Player p) {
            sender.sendMessage(Component.text("Suas vidas: " + lives.getLives(p.getUniqueId()),
                    NamedTextColor.RED));
        }
        return true;
    }
}
