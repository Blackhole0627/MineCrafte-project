package com.seasonrpg.demo.hud;

import com.seasonrpg.demo.classes.ClassManager;
import com.seasonrpg.demo.classes.PlayerClass;
import com.seasonrpg.demo.lives.LivesManager;
import com.seasonrpg.demo.season.SeasonManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Camada visual sempre presente: uma boss bar no topo com o estado da
 * temporada e uma sidebar (scoreboard) com vidas e classe. Atualiza a cada
 * segundo, o suficiente para o relogio andar sem custo relevante.
 */
public final class HudManager {

    private final JavaPlugin plugin;
    private final SeasonManager season;
    private final LivesManager lives;
    private final ClassManager classes;

    private final BossBar bossBar;
    private BukkitTask task;

    public HudManager(JavaPlugin plugin, SeasonManager season, LivesManager lives, ClassManager classes) {
        this.plugin = plugin;
        this.season = season;
        this.lives = lives;
        this.classes = classes;
        this.bossBar = BossBar.bossBar(Component.text("Temporada"),
                1.0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
    }

    public void start() {
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null) task.cancel();
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.hideBossBar(bossBar);
        }
    }

    private void tick() {
        updateBossBar();
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.showBossBar(bossBar);
            updateSidebar(p);
        }
    }

    private void updateBossBar() {
        Component title = Component.text("Temporada " + season.getSeasonNumber()
                + " · Semana " + season.currentWeek() + "/" + season.getTotalWeeks()
                + " · termina em " + season.formatRemaining(), NamedTextColor.WHITE);
        bossBar.name(title);
        bossBar.progress((float) Math.max(0.0, Math.min(1.0, 1.0 - season.seasonProgress())));
    }

    private void updateSidebar(Player p) {
        // Sidebar simples via actionbar para nao brigar com outros scoreboards;
        // leve e suficiente para a demo.
        PlayerClass clazz = classes.getClass(p.getUniqueId());
        String classText = clazz == null ? "sem classe" : clazz.displayName();
        int hearts = lives.getLives(p.getUniqueId());
        Component bar = Component.text("❤ " + hearts + " vidas", NamedTextColor.RED)
                .append(Component.text("   |   ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Classe: " + classText, NamedTextColor.AQUA));
        p.sendActionBar(bar);
    }
}
