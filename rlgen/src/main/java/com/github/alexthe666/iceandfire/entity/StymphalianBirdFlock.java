package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.StymphalianBirdAIAirTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAINearestAttackableTarget;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/** Ice and Fire 1.7.1 StymphalianBirdFlock (not saved: flocks re-form after a reload, as in 1.12). */
public class StymphalianBirdFlock {
   private EntityStymphalianBird leader;
   private ArrayList<EntityStymphalianBird> members = new ArrayList<>();
   private BlockPos leaderTarget;
   private BlockPos prevLeaderTarget;
   private RandomSource random;
   private int distance = 15;

   private StymphalianBirdFlock() {
   }

   public static StymphalianBirdFlock createFlock(EntityStymphalianBird bird) {
      StymphalianBirdFlock flock = new StymphalianBirdFlock();
      flock.leader = bird;
      flock.members = new ArrayList<>();
      flock.members.add(bird);
      flock.leaderTarget = bird.airTarget;
      flock.random = bird.getRandom();
      return flock;
   }

   public boolean isLeader(EntityStymphalianBird bird) {
      return this.leader != null && this.leader == bird;
   }

   @Nullable
   public static StymphalianBirdFlock getNearbyFlock(EntityStymphalianBird bird) {
      float d0 = (float)com.github.alexthe666.iceandfire.IceAndFire.CONFIG.stymphalianBirdFlockLength;
      List<Entity> list = bird.level().getEntities(bird,
         new AABB(bird.getX(), bird.getY(), bird.getZ(), bird.getX() + 1.0, bird.getY() + 1.0, bird.getZ() + 1.0).inflate(d0, 10.0, d0),
         e -> e instanceof EntityStymphalianBird);
      list.sort(new EntityAINearestAttackableTarget.Sorter(bird));
      for (Entity entity : list) {
         if (entity instanceof EntityStymphalianBird other && other.flock != null) {
            return other.flock;
         }
      }
      return null;
   }

   public void addToFlock(EntityStymphalianBird bird) {
      this.members.add(bird);
   }

   public void update() {
      if (!this.members.isEmpty() && (this.leader == null || !this.leader.isAlive())) {
         this.leader = this.members.get(this.random.nextInt(this.members.size()));
      }
      if (this.leader != null && this.leader.isAlive()) {
         this.prevLeaderTarget = this.leaderTarget;
         this.leaderTarget = this.leader.airTarget;
      }
   }

   public void onLeaderAttack(LivingEntity attackTarget) {
      for (EntityStymphalianBird bird : this.members) {
         if (bird.getTarget() == null && !this.isLeader(bird)) {
            bird.setTarget(attackTarget);
         }
      }
   }

   public EntityStymphalianBird getLeader() {
      return this.leader;
   }

   public void setTarget(BlockPos target) {
      this.leaderTarget = target;
      for (EntityStymphalianBird bird : this.members) {
         if (!this.isLeader(bird)) {
            bird.airTarget = StymphalianBirdAIAirTarget.getNearbyAirTarget(bird);
         }
      }
   }

   public void setFlying(boolean flying) {
      for (EntityStymphalianBird bird : this.members) {
         if (!this.isLeader(bird)) {
            bird.setFlying(flying);
         }
      }
   }

   public void setFearTarget(LivingEntity living) {
      for (EntityStymphalianBird bird : this.members) {
         bird.setVictor(living);
      }
   }
}
