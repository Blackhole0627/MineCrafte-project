package com.seasonrpg.demo.lives;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Vidas limitadas por jogador, persistidas em lives.yml (chave = UUID).
 * O manager so guarda numeros; a narrativa (anuncio de ultima vida,
 * eliminacao) fica no {@link LivesListener}, que reage aos eventos de morte.
 */
public final class LivesManager {

    private final JavaPlugin plugin;
    private final File dataFile;
    private final YamlConfiguration data;

    private final int starting;
    private final int lastLifeThreshold;

    public LivesManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "lives.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        this.starting = Math.max(1, plugin.getConfig().getInt("lives.starting", 5));
        this.lastLifeThreshold = Math.max(1, plugin.getConfig().getInt("lives.last-life-threshold", 1));
    }

    private String key(UUID id) {
        return id.toString();
    }

    /** Vidas atuais; inicializa com o valor inicial se o jogador for novo. */
    public int getLives(UUID id) {
        String k = key(id);
        if (!data.contains(k)) {
            data.set(k, starting);
            save();
        }
        return data.getInt(k);
    }

    public void setLives(UUID id, int value) {
        data.set(key(id), Math.max(0, value));
        save();
    }

    /** Remove uma vida e devolve o total restante (nunca abaixo de 0). */
    public int loseLife(UUID id) {
        int now = Math.max(0, getLives(id) - 1);
        setLives(id, now);
        return now;
    }

    /** Adiciona vidas (recompensa de missao / evento) e devolve o novo total. */
    public int addLives(UUID id, int amount) {
        int now = getLives(id) + amount;
        setLives(id, now);
        return now;
    }

    public boolean isEliminated(UUID id) {
        return getLives(id) <= 0;
    }

    public boolean isOnLastLife(UUID id) {
        return getLives(id) == lastLifeThreshold;
    }

    public int getStarting() {
        return starting;
    }

    public int getLastLifeThreshold() {
        return lastLifeThreshold;
    }

    public void resetAll() {
        for (String k : data.getKeys(false)) {
            data.set(k, starting);
        }
        save();
    }

    public void saveAll() {
        save();
    }

    private void save() {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Nao consegui salvar lives.yml: " + e.getMessage());
        }
    }
}
