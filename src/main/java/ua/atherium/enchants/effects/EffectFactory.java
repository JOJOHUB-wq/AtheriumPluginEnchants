package ua.atherium.enchants.effects;

import ua.atherium.enchants.effects.handlers.*;

public class EffectFactory {

    public static EnchantmentEffect createEffect(String type) {
        if (type == null) {
            return null;
        }

        switch (type.toUpperCase()) {
            case "TRENCH":
                return new TrenchEffect();
            case "TIMBER":
                return new TimberEffect();
            case "SMELT":
                return new SmeltEffect();
            case "TELEKINESIS":
                return new TelekinesisEffect();
            case "POTION":
                return new PotionEffect();
            case "POTION_STATIC":
                return new StaticPotionEffect();
            case "EXPLOSION":
                return new ExplosiveEffect();
            case "SNIPER":
                return new SniperEffect();
            case "DODGE":
                return new DodgeEffect();
            case "LAVA_WALKER":
                return new LavaWalkerEffect();
            case "EXP_MULTIPLY":
                return new WisdomEffect();
            case "SPAWNER_BREAK":
                return new SpawnerBreakEffect();
            case "DURABILITY_PING":
                return new DurabilityPingEffect();
            case "DAMAGE_ARMOR":
                return new DamageArmorEffect();
            case "CANCEL_ATTACK":
                return new CancelAttackEffect();
            case "INCREASE_DAMAGE":
                return new IncreaseDamageEffect();
            case "FRY_UP":
                return new FryUpEffect();
            case "AUTO_FISH":
                return new AutoFishEffect();
            case "COBWEB_IMMUNITY":
                return new CobwebImmunityEffect();
            default:
                return null;
        }
    }
}
