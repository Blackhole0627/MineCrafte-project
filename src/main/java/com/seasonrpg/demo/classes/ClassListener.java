package com.seasonrpg.demo.classes;

import com.seasonrpg.demo.lives.LivesManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Liga o menu de classes e o gatilho de habilidade ao {@link ClassManager}.
 *  - Clique no menu -> escolhe a classe e entrega o kit.
 *  - Shift + clique direito segurando o item da classe -> usa a habilidade.
 */
public final class ClassListener implements Listener {

    private final JavaPlugin plugin;
    private final ClassManager classes;
    private final LivesManager lives;

    public ClassListener(JavaPlugin plugin, ClassManager classes, LivesManager lives) {
        this.plugin = plugin;
        this.classes = classes;
        this.lives = lives;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().title() == null) return;
        // Identifica o menu pelo titulo em texto puro (robusto entre versoes).
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(event.getView().title());
        if (!ClassManager.MENU_TITLE_PLAIN.equals(plainTitle)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        PlayerClass picked = matchIcon(event.getCurrentItem());
        if (picked == null) return;

        boolean ok = classes.chooseClass(player.getUniqueId(), picked);
        player.closeInventory();

        if (!ok) {
            player.sendMessage(Component.text("Voce ja usou sua troca de classe desta temporada.",
                    NamedTextColor.RED));
            return;
        }

        classes.giveStarterKit(player, picked);
        player.sendMessage(Component.text("Voce agora e ", NamedTextColor.GRAY)
                .append(Component.text(picked.displayName(), picked.color()))
                .append(Component.text(". Habilidade: ", NamedTextColor.GRAY))
                .append(Component.text(picked.abilityName(), NamedTextColor.GOLD))
                .append(Component.text(" (shift + clique direito ou /ability).", NamedTextColor.GRAY)));
    }

    private PlayerClass matchIcon(ItemStack item) {
        for (PlayerClass clazz : PlayerClass.values()) {
            if (item.getType() == clazz.icon()) {
                return clazz;
            }
        }
        return null;
    }

    @EventHandler
    public void onAbilityUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        if (lives.isEliminated(player.getUniqueId())) return;

        PlayerClass clazz = classes.getClass(player.getUniqueId());
        if (clazz == null) return;

        ItemStack inHand = event.getItem();
        if (inHand == null || inHand.getType() != clazz.signatureItem()) return;

        // Arqueiro usa arco: cancelamos o disparo normal para nao gastar flecha
        // junto com a habilidade.
        event.setCancelled(true);
        classes.useAbility(player);
    }
}
