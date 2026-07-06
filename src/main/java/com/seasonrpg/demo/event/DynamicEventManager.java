package com.seasonrpg.demo.event;

import com.seasonrpg.demo.lives.LivesManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Motor de eventos dinamicos da demo: a "Invasao". Anuncia com contagem
 * regressiva, faz surgir uma horda em um ponto do mundo, conta quem abate os
 * invasores e recompensa os defensores. E o pilar que gera momentos
 * imprevisiveis para streamers.
 *
 * Na demo os invasores sao zumbis vanilla marcados com uma scoreboard tag; no
 * build pago viram chefes do MythicMobs sem mudar essa estrutura.
 */
public final class DynamicEventManager implements Listener {

    private static final String INVADER_TAG = "seasonrpg_invader";

    private final JavaPlugin plugin;
    private final LivesManager lives;

    private final int waveSize;
    private final int warningSeconds;

    private boolean active = false;
    private final Set<UUID> aliveInvaders = new HashSet<>();
    private final Map<UUID, Integer> contribution = new HashMap<>();
    private BukkitTask timeoutTask;
    private BukkitTask autoTask;

    public DynamicEventManager(JavaPlugin plugin, LivesManager lives) {
        this.plugin = plugin;
        this.lives = lives;
        this.waveSize = Math.max(1, plugin.getConfig().getInt("event.wave-size", 10));
        this.warningSeconds = Math.max(0, plugin.getConfig().getInt("event.warning-seconds", 30));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean isActive() {
        return active;
    }

    /** Agenda eventos automaticos, se ligado na config. */
    public void startAutoScheduler() {
        long interval = Math.max(1, plugin.getConfig().getLong("event.auto-interval-minutes", 20)) * 60L * 20L;
        this.autoTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!active && !plugin.getServer().getOnlinePlayers().isEmpty()) {
                startEvent();
            }
        }, interval, interval);
    }

    /** Dispara a sequencia de invasao: aviso -> horda -> defesa. */
    public boolean startEvent() {
        if (active) {
            return false;
        }
        Location target = pickLocation();
        if (target == null) {
            return false; // ninguem online / sem mundo
        }
        active = true;
        aliveInvaders.clear();
        contribution.clear();

        announceWarning(target);
        // Conta regressiva e depois spawn.
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> spawnWave(target), warningSeconds * 20L);
        return true;
    }

    private Location pickLocation() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            return p.getLocation().clone().add(6, 0, 6);
        }
        World world = plugin.getServer().getWorlds().isEmpty()
                ? null : plugin.getServer().getWorlds().get(0);
        return world == null ? null : world.getSpawnLocation();
    }

    private void announceWarning(Location target) {
        String coords = target.getBlockX() + ", " + target.getBlockZ();
        Component msg = Component.text("⚔ INVASAO", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text(" a caminho de ", NamedTextColor.GRAY))
                .append(Component.text("(" + coords + ")", NamedTextColor.YELLOW))
                .append(Component.text(" em " + warningSeconds + "s. Defendam a regiao!", NamedTextColor.GRAY));
        plugin.getServer().broadcast(msg);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.EVENT_RAID_HORN, 1f, 1f);
        }
    }

    private void spawnWave(Location center) {
        World world = center.getWorld();
        if (world == null) {
            finish(false);
            return;
        }
        for (int i = 0; i < waveSize; i++) {
            double ox = (i % 5) - 2;
            double oz = (i / 5) - 1;
            Location spawn = center.clone().add(ox, 0, oz);
            Zombie z = (Zombie) world.spawnEntity(spawn, EntityType.ZOMBIE);
            z.addScoreboardTag(INVADER_TAG);
            z.customName(Component.text("Invasor", NamedTextColor.DARK_RED));
            z.setCustomNameVisible(true);
            z.setShouldBurnInDay(false);
            aliveInvaders.add(z.getUniqueId());
        }
        plugin.getServer().broadcast(Component.text("A horda chegou! " + waveSize
                + " invasores atacam.", NamedTextColor.RED));

        // Seguranca: se a horda nao for derrotada em 5 min, encerra mesmo assim.
        timeoutTask = plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> { if (active) finish(true); }, 5 * 60L * 20L);
    }

    @EventHandler
    public void onInvaderDeath(EntityDeathEvent event) {
        if (!active) return;
        Entity dead = event.getEntity();
        if (!dead.getScoreboardTags().contains(INVADER_TAG)) return;

        aliveInvaders.remove(dead.getUniqueId());

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            contribution.merge(killer.getUniqueId(), 1, Integer::sum);
        }

        int remaining = aliveInvaders.size();
        if (remaining > 0 && remaining <= 3) {
            plugin.getServer().broadcast(Component.text("Faltam " + remaining
                    + " invasores!", NamedTextColor.GOLD));
        }
        if (remaining == 0) {
            finish(true);
        }
    }

    private void finish(boolean success) {
        if (!active) return;
        active = false;
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
        // Remove invasores que sobraram (timeout).
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e.getScoreboardTags().contains(INVADER_TAG)) {
                    e.remove();
                }
            }
        }
        aliveInvaders.clear();

        if (!success || contribution.isEmpty()) {
            plugin.getServer().broadcast(Component.text("A invasao terminou.", NamedTextColor.GRAY));
            contribution.clear();
            return;
        }

        plugin.getServer().broadcast(Component.text("✔ Invasao repelida! Recompensas entregues aos defensores.",
                NamedTextColor.GREEN, TextDecoration.BOLD));
        for (Map.Entry<UUID, Integer> entry : contribution.entrySet()) {
            Player p = plugin.getServer().getPlayer(entry.getKey());
            if (p == null) continue;
            rewardDefender(p, entry.getValue());
        }
        contribution.clear();
    }

    private void rewardDefender(Player p, int kills) {
        p.getInventory().addItem(trophy(kills));
        // Defender bem pode devolver uma vida perdida (ate o teto inicial).
        if (kills >= 3 && lives.getLives(p.getUniqueId()) < lives.getStarting()) {
            lives.addLives(p.getUniqueId(), 1);
            p.sendMessage(Component.text("Voce recuperou 1 vida por defender a regiao!",
                    NamedTextColor.GREEN));
        }
        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        p.sendMessage(Component.text("Voce abateu " + kills + " invasor(es).", NamedTextColor.YELLOW));
    }

    private ItemStack trophy(int kills) {
        ItemStack item = new ItemStack(Material.DIAMOND, Math.max(1, kills));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Espolio da Invasao", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    public void stopCurrentEvent() {
        if (autoTask != null) {
            autoTask.cancel();
            autoTask = null;
        }
        if (active) {
            finish(false);
        }
    }
}
