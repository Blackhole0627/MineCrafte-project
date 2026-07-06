package com.seasonrpg.demo.command;

import com.seasonrpg.demo.lives.LivesManager;
import com.seasonrpg.demo.season.SeasonManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /seasonadmin - utilitarios para demonstrar rapido:
 *   reset                  reinicia o relogio da temporada
 *   setweek <n>            pula para a semana n
 *   givelife <jogador> <n> devolve vidas (ou resetlives)
 *   resetlives            devolve todos ao valor inicial
 */
public final class SeasonAdminCommand implements CommandExecutor {

    private final SeasonManager season;
    private final LivesManager lives;

    public SeasonAdminCommand(SeasonManager season, LivesManager lives) {
        this.season = season;
        this.lives = lives;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Uso: /seasonadmin <reset|setweek|givelife|resetlives>",
                    NamedTextColor.YELLOW));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reset" -> {
                season.resetSeason();
                sender.sendMessage(Component.text("Temporada reiniciada.", NamedTextColor.GREEN));
            }
            case "setweek" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Uso: /seasonadmin setweek <n>", NamedTextColor.YELLOW));
                    return true;
                }
                try {
                    int week = Integer.parseInt(args[1]);
                    season.setWeek(week);
                    sender.sendMessage(Component.text("Semana ajustada para " + season.currentWeek() + ".",
                            NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Numero invalido.", NamedTextColor.RED));
                }
            }
            case "givelife" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Uso: /seasonadmin givelife <jogador> <n>",
                            NamedTextColor.YELLOW));
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Jogador nao encontrado (precisa ter entrado ao menos uma vez).",
                            NamedTextColor.RED));
                    return true;
                }
                try {
                    int n = Integer.parseInt(args[2]);
                    int total = lives.addLives(target.getUniqueId(), n);
                    sender.sendMessage(Component.text(target.getName() + " agora tem " + total + " vida(s).",
                            NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Numero invalido.", NamedTextColor.RED));
                }
            }
            case "resetlives" -> {
                lives.resetAll();
                sender.sendMessage(Component.text("Vidas de todos reiniciadas.", NamedTextColor.GREEN));
            }
            default -> sender.sendMessage(Component.text("Subcomando desconhecido.", NamedTextColor.RED));
        }
        return true;
    }
}
