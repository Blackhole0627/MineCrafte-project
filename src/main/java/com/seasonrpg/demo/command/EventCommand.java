package com.seasonrpg.demo.command;

import com.seasonrpg.demo.event.DynamicEventManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/** /event start|stop - dispara ou encerra uma invasao (admin). */
public final class EventCommand implements CommandExecutor {

    private final DynamicEventManager events;

    public EventCommand(DynamicEventManager events) {
        this.events = events;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Uso: /event <start|stop>", NamedTextColor.YELLOW));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (events.startEvent()) {
                    sender.sendMessage(Component.text("Invasao iniciada.", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Ja ha um evento ativo ou nao ha jogadores online.",
                            NamedTextColor.RED));
                }
            }
            case "stop" -> {
                events.stopCurrentEvent();
                sender.sendMessage(Component.text("Evento encerrado.", NamedTextColor.GREEN));
            }
            default -> sender.sendMessage(Component.text("Uso: /event <start|stop>", NamedTextColor.YELLOW));
        }
        return true;
    }
}
