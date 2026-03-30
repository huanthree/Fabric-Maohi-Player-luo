package com.example.maohi.bot;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public class BotAI {

    private final BotPlayer bot;

    private Vec3 wanderTarget = null;
    private int wanderTicksLeft = 0;
    private int idleTicksLeft = 0;
    private int tickCounter = 0;
    private int nextDecisionTick = 0;

    private enum Action { WANDER, IDLE, JUMP, LOOK_AROUND, INTERACT, SNEAK }
    private Action current = Action.IDLE;

    public BotAI(BotPlayer bot) {
        this.bot = bot;
        this.idleTicksLeft = ThreadLocalRandom.current().nextInt(20, 60);
    }

    public void tick() {
        tickCounter++;
        if (current == Action.WANDER) applyMovement();
        if (tickCounter < nextDecisionTick) return;
        scheduleNextDecision();

        switch (current) {
            case WANDER      -> tickWander();
            case IDLE        -> tickIdle();
            case JUMP        -> { bot.jumpFromGround(); current = Action.WANDER; }
            case LOOK_AROUND -> tickLookAround();
            case INTERACT    -> tickInteract();
            case SNEAK       -> tickSneak();
        }
        if (shouldSwitch()) pickNext();
    }

    private void applyMovement() {
        if (wanderTarget == null || wanderTicksLeft <= 0) return;
        Vec3 pos = bot.position();
        Vec3 dir = wanderTarget.subtract(pos).normalize();

        ThreadLocalRandom r = ThreadLocalRandom.current();
        double speed = 0.12 + r.nextDouble() * 0.06;
        double jitterX = (r.nextDouble() - 0.5) * 0.02;
        double jitterZ = (r.nextDouble() - 0.5) * 0.02;

        bot.setDeltaMovement(dir.x * speed + jitterX, bot.getDeltaMovement().y, dir.z * speed + jitterZ);

        // 替代 hasImpulse：直接标记移动
        bot.move(net.minecraft.world.entity.MoverType.SELF, bot.getDeltaMovement());

        if (r.nextInt(80) == 0) bot.setDeltaMovement(0, bot.getDeltaMovement().y, 0);

        float targetYaw = (float)(Math.atan2(-dir.x, dir.z) * (180.0 / Math.PI));
        float currentYaw = bot.getYRot();
        bot.setYRot(currentYaw + (targetYaw - currentYaw) * 0.15f + (r.nextFloat() - 0.5f) * 3f);

        if (isBlocked()) bot.jumpFromGround();
        wanderTicksLeft--;
        if (pos.distanceTo(wanderTarget) < 1.5) wanderTicksLeft = 0;
    }

    private void tickWander() {
        if (wanderTarget == null || wanderTicksLeft <= 0) pickTarget();
    }

    private void tickIdle() {
        bot.setDeltaMovement(0, bot.getDeltaMovement().y, 0);
        if (ThreadLocalRandom.current().nextInt(5) == 0) {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            bot.setYRot(bot.getYRot() + (r.nextFloat() - 0.5f) * 15f);
            bot.setXRot(bot.getXRot() + (r.nextFloat() - 0.5f) * 8f);
        }
        if (--idleTicksLeft <= 0) pickNext();
    }

    private void tickLookAround() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        bot.setYRot(bot.getYRot() + (r.nextFloat() - 0.5f) * 25f);
        bot.setXRot(bot.getXRot() * 0.8f + (r.nextFloat() - 0.4f) * 20f);
        if (--idleTicksLeft <= 0) { current = Action.IDLE; idleTicksLeft = humanDelay(10, 30); }
    }

    private void tickInteract() {
        bot.swing(InteractionHand.MAIN_HAND);
        current = Action.IDLE;
        idleTicksLeft = humanDelay(5, 15);
    }

    private void tickSneak() {
        bot.setShiftKeyDown(true);
        if (--idleTicksLeft <= 0) {
            bot.setShiftKeyDown(false);
            current = Action.IDLE;
            idleTicksLeft = humanDelay(10, 25);
        }
    }

    private void pickTarget() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        Vec3 pos = bot.position();
        double range = 8 + r.nextDouble() * 24;
        double angle = r.nextDouble() * Math.PI * 2;
        wanderTarget = new Vec3(pos.x + Math.cos(angle) * range, pos.y, pos.z + Math.sin(angle) * range);
        wanderTicksLeft = humanDelay(40, 100);
    }

    private void pickNext() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int roll = r.nextInt(100);
        if      (roll < 35) { current = Action.WANDER;      pickTarget(); }
        else if (roll < 55) { current = Action.IDLE;        idleTicksLeft = humanDelay(20, 80); }
        else if (roll < 68) { current = Action.LOOK_AROUND; idleTicksLeft = humanDelay(3, 8); }
        else if (roll < 78) { current = Action.INTERACT; }
        else if (roll < 86) { current = Action.JUMP; }
        else                { current = Action.SNEAK;       idleTicksLeft = humanDelay(10, 40); }
    }

    private void scheduleNextDecision() {
        nextDecisionTick = tickCounter + humanDelay(10, 40);
    }

    private static int humanDelay(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private boolean shouldSwitch() {
        return (current == Action.WANDER && wanderTicksLeft <= 0)
            || (current == Action.IDLE   && idleTicksLeft   <= 0);
    }

    private boolean isBlocked() {
        Vec3 vel = bot.getDeltaMovement();
        if (Math.abs(vel.x) < 0.01 && Math.abs(vel.z) < 0.01) return false;
        BlockPos ahead = BlockPos.containing(bot.getX() + vel.x * 2, bot.getY(), bot.getZ() + vel.z * 2);
        // 1.21.x isSolidRender() 无参数
        return bot.level().getBlockState(ahead).isSolidRender();
    }
}
