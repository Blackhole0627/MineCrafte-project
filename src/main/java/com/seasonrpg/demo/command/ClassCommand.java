package com.seasonrpg.demo.command;

import com.seasonrpg.demo.classes.ClassManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /class - abre o menu de escolha de classe. */
public final class ClassCommand implements CommandExecutor {

    private final ClassManager classes;

    public ClassCommand(ClassManager classes) {
        this.classes = classes;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores podem escolher classe.", NamedTextColor.RED));
            return true;
        }
        classes.openMenu(player);
        return true;
    }
}
