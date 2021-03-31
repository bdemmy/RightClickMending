package bdemmy.rightclickmending.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class MixinItem {
    private int getMendingRepairCost(int repairAmount) {
        return repairAmount / 2;
    }

    private int getMendingRepairAmount(int experienceAmount) {
        return experienceAmount * 2;
    }

    @Inject(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void OnItemUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        if (!world.isClient) {
            if (user.isSneaking()) {
                ItemStack handStack = user.getStackInHand(hand);
                if (!handStack.isEmpty() && handStack.isDamaged()) {
                    boolean mending = EnchantmentHelper.getLevel(Enchantments.MENDING, handStack) > 0;

                    if (mending) {
                        int i = Math.min(getMendingRepairAmount(user.totalExperience), handStack.getDamage());
                        user.addExperience(-getMendingRepairCost(i));
                        handStack.setDamage(handStack.getDamage() - i);

                        info.setReturnValue(TypedActionResult.pass(user.getStackInHand(hand)));
                    }
                }
            }
        }
    }
}
