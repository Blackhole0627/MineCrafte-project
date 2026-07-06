package com.seasonrpg.demo.classes;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/**
 * As duas classes da demo. Cada uma tem UMA habilidade que muda o jeito de
 * jogar (nao apenas numeros), exatamente o escopo enxuto combinado para a
 * primeira temporada. Mais classes entram no build pago.
 */
public enum PlayerClass {

    WARRIOR(
            "Guerreiro",
            NamedTextColor.RED,
            Material.IRON_SWORD,
            "Investida",
            "Avanca para frente com um salto e ganha resistencia breve.",
            Material.IRON_SWORD),

    ARCHER(
            "Arqueiro",
            NamedTextColor.GREEN,
            Material.BOW,
            "Tiro Certeiro",
            "Dispara uma flecha veloz e certeira, mesmo sem mirar perfeito.",
            Material.BOW);

    private final String displayName;
    private final NamedTextColor color;
    private final Material icon;
    private final String abilityName;
    private final String abilityDescription;
    /** Item que o jogador segura para usar a habilidade com shift + clique direito. */
    private final Material signatureItem;

    PlayerClass(String displayName, NamedTextColor color, Material icon,
                String abilityName, String abilityDescription, Material signatureItem) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.abilityName = abilityName;
        this.abilityDescription = abilityDescription;
        this.signatureItem = signatureItem;
    }

    public String displayName() { return displayName; }
    public NamedTextColor color() { return color; }
    public Material icon() { return icon; }
    public String abilityName() { return abilityName; }
    public String abilityDescription() { return abilityDescription; }
    public Material signatureItem() { return signatureItem; }

    public static PlayerClass fromString(String s) {
        if (s == null) return null;
        try {
            return PlayerClass.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
