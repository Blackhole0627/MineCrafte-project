package com.seasonrpg.demo.classes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Guarda a classe escolhida por cada jogador (classes.yml), controla trocas e
 * cooldowns de habilidade, e monta o menu de selecao. O efeito das habilidades
 * fica no {@link ClassListener}; aqui so vive o estado.
 */
public final class ClassManager {

    /** Titulo do inventario usado para identificar cliques no menu. */
    public static final Component MENU_TITLE =
            Component.text("Escolha sua classe", NamedTextColor.DARK_AQUA);
    public static final String MENU_TITLE_PLAIN = "Escolha sua classe";

    private final JavaPlugin plugin;
    private final File dataFile;
    private final YamlConfiguration data;

    private final long cooldownMillis;
    private final boolean allowSwitch;

    /** Cooldown de habilidade em memoria: uuid -> instante do proximo uso liberado. */
    private final Map<UUID, Long> abilityReadyAt = new HashMap<>();

    public ClassManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "classes.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        this.cooldownMillis = Math.max(0, plugin.getConfig().getLong("classes.ability-cooldown-seconds", 12)) * 1000L;
        this.allowSwitch = plugin.getConfig().getBoolean("classes.allow-switch", true);
    }

    public PlayerClass getClass(UUID id) {
        return PlayerClass.fromString(data.getString(id + ".class"));
    }

    public boolean hasClass(UUID id) {
        return getClass(id) != null;
    }

    public boolean hasSwitched(UUID id) {
        return data.getBoolean(id + ".switched", false);
    }

    /**
     * Define a classe. Devolve false se o jogador ja tem classe e ou trocas
     * estao desativadas ou ele ja usou a troca da temporada.
     */
    public boolean chooseClass(UUID id, PlayerClass clazz) {
        boolean firstTime = !hasClass(id);
        if (!firstTime) {
            if (!allowSwitch || hasSwitched(id)) {
                return false;
            }
            data.set(id + ".switched", true);
        }
        data.set(id + ".class", clazz.name());
        save();
        return true;
    }

    /** Segundos restantes de cooldown, ou 0 se pronto. */
    public long cooldownRemaining(UUID id) {
        long ready = abilityReadyAt.getOrDefault(id, 0L);
        long diff = ready - System.currentTimeMillis();
        return diff <= 0 ? 0 : (diff + 999) / 1000;
    }

    public boolean isAbilityReady(UUID id) {
        return cooldownRemaining(id) == 0;
    }

    public void putOnCooldown(UUID id) {
        abilityReadyAt.put(id, System.currentTimeMillis() + cooldownMillis);
    }

    /**
     * Executa a habilidade da classe do jogador. Centraliza a logica para que
     * tanto o /ability quanto o shift + clique direito chamem o mesmo codigo.
     * Devolve false (com feedback ao jogador) se nao houver classe ou o
     * cooldown ainda nao acabou.
     */
    public boolean useAbility(Player player) {
        UUID id = player.getUniqueId();
        PlayerClass clazz = getClass(id);
        if (clazz == null) {
            player.sendMessage(Component.text("Escolha uma classe primeiro com /class.", NamedTextColor.RED));
            return false;
        }
        if (!isAbilityReady(id)) {
            player.sendMessage(Component.text("Habilidade em recarga: " + cooldownRemaining(id) + "s.",
                    NamedTextColor.GRAY));
            return false;
        }

        switch (clazz) {
            case WARRIOR -> castInvestida(player);
            case ARCHER -> castTiroCerteiro(player);
        }
        putOnCooldown(id);
        player.sendMessage(Component.text("Voce usou ", NamedTextColor.GRAY)
                .append(Component.text(clazz.abilityName() + "!", clazz.color())));
        return true;
    }

    private void castInvestida(Player player) {
        org.bukkit.util.Vector dir = player.getLocation().getDirection().normalize()
                .multiply(1.6).setY(0.45);
        player.setVelocity(dir);
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.RESISTANCE, 80, 1, false, true));
        player.getWorld().playSound(player.getLocation(),
                org.bukkit.Sound.ITEM_TRIDENT_RIPTIDE_2, 1f, 1f);
    }

    private void castTiroCerteiro(Player player) {
        org.bukkit.entity.Arrow arrow = player.launchProjectile(org.bukkit.entity.Arrow.class);
        arrow.setVelocity(player.getLocation().getDirection().normalize().multiply(3.2));
        arrow.setCritical(true);
        arrow.setDamage(arrow.getDamage() + 2.0);
        arrow.setShooter(player);
        player.getWorld().playSound(player.getLocation(),
                org.bukkit.Sound.ENTITY_ARROW_SHOOT, 1f, 1.4f);
    }

    public boolean isAllowSwitch() {
        return allowSwitch;
    }

    /** Entrega o kit inicial da classe (arma assinatura + basico de sobrevivencia). */
    public void giveStarterKit(Player player, PlayerClass clazz) {
        player.getInventory().addItem(new ItemStack(clazz.signatureItem()));
        if (clazz == PlayerClass.ARCHER) {
            ItemStack arrows = new ItemStack(Material.ARROW, 32);
            player.getInventory().addItem(arrows);
        }
        player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 8));
    }

    /** Abre o menu de escolha (chest 9 slots com um icone por classe). */
    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 9, MENU_TITLE);
        menu.setItem(2, classIcon(PlayerClass.WARRIOR));
        menu.setItem(6, classIcon(PlayerClass.ARCHER));
        player.openInventory(menu);
    }

    private ItemStack classIcon(PlayerClass clazz) {
        ItemStack item = new ItemStack(clazz.icon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(clazz.displayName(), clazz.color())
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Habilidade: " + clazz.abilityName(), NamedTextColor.GOLD)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        lore.add(Component.text(clazz.abilityDescription(), NamedTextColor.GRAY)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Clique para escolher", NamedTextColor.YELLOW)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void save() {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Nao consegui salvar classes.yml: " + e.getMessage());
        }
    }
}
