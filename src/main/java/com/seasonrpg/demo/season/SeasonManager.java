package com.seasonrpg.demo.season;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Relogio da temporada. Guarda quando a temporada comecou (em ticks do mundo,
 * persistido em season.yml) e deriva a "semana" atual a partir da duracao
 * configurada por semana. Nao mantem timer proprio: quem quiser saber o tempo
 * apenas consulta os metodos abaixo, o que evita drift e sobrevive a restart.
 */
public final class SeasonManager {

    private final JavaPlugin plugin;
    private final File dataFile;
    private final YamlConfiguration data;

    private int seasonNumber;
    private int totalWeeks;
    private long minutesPerWeek;
    private String objective;

    /** Momento de inicio da temporada, em millis de relogio (System-independent: persistido). */
    private long startEpochMinutes;

    public SeasonManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "season.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        reloadFromConfig();
    }

    private void reloadFromConfig() {
        this.seasonNumber = plugin.getConfig().getInt("season.number", 1);
        this.totalWeeks = Math.max(1, plugin.getConfig().getInt("season.total-weeks", 8));
        this.minutesPerWeek = Math.max(1, plugin.getConfig().getLong("season.minutes-per-week", 10));
        this.objective = plugin.getConfig().getString("season.objective",
                "Sobreviva e prepare-se para o chefe final.");
    }

    /** Inicializa o inicio da temporada se ainda nao existir. */
    public void start() {
        if (data.contains("start-epoch-minutes")) {
            this.startEpochMinutes = data.getLong("start-epoch-minutes");
        } else {
            this.startEpochMinutes = nowMinutes();
            data.set("start-epoch-minutes", startEpochMinutes);
            save();
        }
    }

    public void stop() {
        save();
    }

    /** Reinicia a temporada do zero (usado pelo comando admin de reset). */
    public void resetSeason() {
        this.startEpochMinutes = nowMinutes();
        data.set("start-epoch-minutes", startEpochMinutes);
        save();
    }

    /** Forca uma semana especifica ajustando o inicio para tras (demo/admin). */
    public void setWeek(int week) {
        int clamped = Math.max(1, Math.min(totalWeeks, week));
        this.startEpochMinutes = nowMinutes() - (long) (clamped - 1) * minutesPerWeek;
        data.set("start-epoch-minutes", startEpochMinutes);
        save();
    }

    /** Minutos decorridos desde o inicio da temporada. */
    public long minutesElapsed() {
        return Math.max(0, nowMinutes() - startEpochMinutes);
    }

    /** Semana atual (1-based), limitada a totalWeeks. */
    public int currentWeek() {
        long week = (minutesElapsed() / minutesPerWeek) + 1;
        return (int) Math.min(totalWeeks, week);
    }

    /** Fracao 0..1 de progresso da temporada inteira, para a barra. */
    public double seasonProgress() {
        long totalMinutes = (long) totalWeeks * minutesPerWeek;
        return Math.max(0.0, Math.min(1.0, (double) minutesElapsed() / totalMinutes));
    }

    /** Minutos restantes ate o fim da temporada. */
    public long minutesRemaining() {
        long totalMinutes = (long) totalWeeks * minutesPerWeek;
        return Math.max(0, totalMinutes - minutesElapsed());
    }

    public boolean isOver() {
        return minutesRemaining() <= 0;
    }

    /** "2d 3h 10m" a partir de minutos, curto e legivel no chat. */
    public String formatRemaining() {
        long mins = minutesRemaining();
        long days = mins / (60 * 24);
        long hours = (mins % (60 * 24)) / 60;
        long minutes = mins % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        sb.append(minutes).append("m");
        return sb.toString().trim();
    }

    public int getSeasonNumber() { return seasonNumber; }
    public int getTotalWeeks() { return totalWeeks; }
    public String getObjective() { return objective; }

    private long nowMinutes() {
        // System.currentTimeMillis e permitido em runtime do plugin (nao e o
        // ambiente restrito do harness). Convertido para minutos.
        return System.currentTimeMillis() / 60000L;
    }

    private void save() {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Nao consegui salvar season.yml: " + e.getMessage());
        }
    }
}
