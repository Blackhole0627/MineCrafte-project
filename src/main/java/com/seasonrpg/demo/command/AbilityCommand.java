package com.seasonrpg.demo.command;

import com.seasonrpg.demo.classes.ClassManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /ability - usa a habilidade da classe (mesmo efeito do shift + clique direito). */
public final class AbilityCommand implements CommandExecutor {

    private final ClassManager classes;

    public AbilityCommand(ClassManager classes) {
        this.classes = classes;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores tem habilidades.", NamedTextColor.RED));
            return true;
        }
        classes.useAbility(player);
        return true;
    }
}
