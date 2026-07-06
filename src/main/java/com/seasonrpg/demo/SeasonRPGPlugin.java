package com.seasonrpg.demo;

import com.seasonrpg.demo.classes.ClassListener;
import com.seasonrpg.demo.classes.ClassManager;
import com.seasonrpg.demo.command.AbilityCommand;
import com.seasonrpg.demo.command.ClassCommand;
import com.seasonrpg.demo.command.EventCommand;
import com.seasonrpg.demo.command.LivesCommand;
import com.seasonrpg.demo.command.SeasonAdminCommand;
import com.seasonrpg.demo.command.SeasonCommand;
import com.seasonrpg.demo.event.DynamicEventManager;
import com.seasonrpg.demo.hud.HudManager;
import com.seasonrpg.demo.lives.LivesListener;
import com.seasonrpg.demo.lives.LivesManager;
import com.seasonrpg.demo.season.SeasonManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Ponto de entrada da demo. Amarra os quatro pilares do nucleo:
 *  1. {@link SeasonManager}       - relogio da temporada.
 *  2. {@link LivesManager}        - vidas limitadas + narrativa de ultima vida.
 *  3. {@link ClassManager}        - escolha de classe que muda o jeito de jogar.
 *  4. {@link DynamicEventManager} - eventos que surpreendem o servidor.
 *
 * Nada aqui depende de plugins externos: roda em um Paper limpo. No projeto
 * pago os mobs vanilla dos eventos dariam lugar a chefes do MythicMobs.
 */
public final class SeasonRPGPlugin extends JavaPlugin {

    private SeasonManager seasonManager;
    private LivesManager livesManager;
    private ClassManager classManager;
    private DynamicEventManager eventManager;
    private HudManager hudManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.seasonManager = new SeasonManager(this);
        this.livesManager = new LivesManager(this);
        this.classManager = new ClassManager(this);
        this.eventManager = new DynamicEventManager(this, livesManager);
        this.hudManager = new HudManager(this, seasonManager, livesManager, classManager);

        seasonManager.start();
        hudManager.start();
        if (getConfig().getBoolean("event.auto", false)) {
            eventManager.startAutoScheduler();
        }

        registerListeners();
        registerCommands();

        getLogger().info("SeasonRPG Demo habilitado. Temporada "
                + seasonManager.getSeasonNumber() + " em andamento.");
    }

    @Override
    public void onDisable() {
        if (hudManager != null) hudManager.stop();
        if (seasonManager != null) seasonManager.stop();
        if (eventManager != null) eventManager.stopCurrentEvent();
        if (livesManager != null) livesManager.saveAll();
        getLogger().info("SeasonRPG Demo desabilitado. Dados de vidas salvos.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
                new LivesListener(this, livesManager), this);
        getServer().getPluginManager().registerEvents(
                new ClassListener(this, classManager, livesManager), this);
    }

    private void registerCommands() {
        getCommand("season").setExecutor(new SeasonCommand(seasonManager, livesManager));
        getCommand("class").setExecutor(new ClassCommand(classManager));
        getCommand("ability").setExecutor(new AbilityCommand(classManager));
        getCommand("lives").setExecutor(new LivesCommand(livesManager));
        getCommand("event").setExecutor(new EventCommand(eventManager));
        getCommand("seasonadmin").setExecutor(
                new SeasonAdminCommand(seasonManager, livesManager));
    }

    public SeasonManager seasons() { return seasonManager; }
    public LivesManager lives() { return livesManager; }
    public ClassManager classes() { return classManager; }
    public DynamicEventManager events() { return eventManager; }
}
