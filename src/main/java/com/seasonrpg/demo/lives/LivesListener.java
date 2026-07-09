package com.seasonrpg.demo.lives;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Traduz mortes em narrativa. Cada morte custa uma vida; chegar a ultima vida
 * dispara um anuncio para todo o servidor (o momento streamavel); perder a
 * ultima elimina o jogador (spectator ou kick, conforme config).
 */
public final class LivesListener implements Listener {

    private final JavaPlugin plugin;
    private final LivesManager lives;

    public LivesListener(JavaPlugin plugin, LivesManager lives) {
        this.plugin = plugin;
        this.lives = lives;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        // Jogador eliminado que reentra continua como espectador.
        if (lives.isEliminated(p.getUniqueId())
                && "SPECTATOR".equalsIgnoreCase(plugin.getConfig().getString("lives.on-eliminated", "SPECTATOR"))) {
            p.setGameMode(GameMode.SPECTATOR);
        }
        int remaining = lives.getLives(p.getUniqueId());
        p.sendMessage(Component.text("Voce tem ", NamedTextColor.GRAY)
                .append(Component.text(remaining + " vida(s)", NamedTextColor.RED))
                .append(Component.text(" nesta temporada.", NamedTextColor.GRAY)));
    }

    /**
     * Aplica o modo espectador no respawn. Fazer isso na morte e' pouco
     * confiavel (o jogador ainda esta na tela de morte); no respawn e' garantido.
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player p = event.getPlayer();
        if (lives.isEliminated(p.getUniqueId())
                && "SPECTATOR".equalsIgnoreCase(plugin.getConfig().getString("lives.on-eliminated", "SPECTATOR"))) {
            p.setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        if (lives.isEliminated(p.getUniqueId())) {
            return; // ja eliminado, nada a descontar
        }

        int remaining = lives.loseLife(p.getUniqueId());

        if (remaining <= 0) {
            eliminate(p);
            return;
        }

        // Feedback pessoal para o jogador.
        p.sendMessage(Component.text("Voce perdeu uma vida. Restam ", NamedTextColor.RED)
                .append(Component.text(String.valueOf(remaining), NamedTextColor.YELLOW))
                .append(Component.text(".", NamedTextColor.RED)));

        if (lives.isOnLastLife(p.getUniqueId())) {
            announceLastLife(p);
        }
    }

    private void announceLastLife(Player p) {
        Component msg = Component.text("⚠ ", NamedTextColor.GOLD)
                .append(Component.text(p.getName(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text(" esta em sua ", NamedTextColor.GRAY))
                .append(Component.text("ULTIMA VIDA", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text("!", NamedTextColor.GRAY));
        plugin.getServer().broadcast(msg);
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.playSound(online.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.4f);
        }
    }

    private void eliminate(Player p) {
        String mode = plugin.getConfig().getString("lives.on-eliminated", "SPECTATOR");

        Component tribute = Component.text("☠ ", NamedTextColor.DARK_RED)
                .append(Component.text(p.getName(), NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text(" caiu na Temporada e nao voltara.", NamedTextColor.GRAY));
        plugin.getServer().broadcast(tribute);
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.playSound(online.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.6f, 1.0f);
        }

        if ("KICK".equalsIgnoreCase(mode)) {
            // Agendado para o proximo tick: nao da para kickar dentro do death event.
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    p.kick(Component.text("Voce perdeu sua ultima vida nesta temporada.",
                            NamedTextColor.RED)));
        }
        // Modo SPECTATOR e' aplicado em onRespawn (mais confiavel que na morte).
    }
}
