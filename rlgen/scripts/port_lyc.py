"""Mechanical first pass of the Lycanites Mobs 2.0.8.9 (1.12.2, MCP names) server code to the 1.21.1 NeoForge API.
usage: port_lyc.py <decompiled lyc root (has com/lycanitesmobs)> <rlgen/src/main/java> [--force] [paths...]
Only writes files that do not exist yet (hand-fixed files are never overwritten) unless --force is given.
The output still needs hand fixes; it only takes care of the renames that are the same everywhere."""
import os, re, sys

args = [a for a in sys.argv[1:] if not a.startswith('--')]
FORCE = '--force' in sys.argv
SRC, DST = args[0], args[1]
ONLY = args[2:]

SKIP_DIRS = ('com/lycanitesmobs/client/', 'com/lycanitesmobs/core/dungeon/')

WILDCARDS = """net.minecraft.core net.minecraft.core.particles net.minecraft.core.registries net.minecraft.nbt net.minecraft.network
net.minecraft.network.chat net.minecraft.network.syncher net.minecraft.resources net.minecraft.server.level net.minecraft.sounds net.minecraft.tags
net.minecraft.util net.minecraft.world net.minecraft.world.damagesource net.minecraft.world.effect net.minecraft.world.entity
net.minecraft.world.entity.ai.attributes net.minecraft.world.entity.ai.navigation net.minecraft.world.entity.ai.control net.minecraft.world.entity.animal
net.minecraft.world.entity.monster net.minecraft.world.entity.player net.minecraft.world.entity.projectile net.minecraft.world.entity.item
net.minecraft.world.entity.npc net.minecraft.world.entity.vehicle net.minecraft.world.entity.ambient net.minecraft.world.item net.minecraft.world.level net.minecraft.world.level.block
net.minecraft.world.level.block.state net.minecraft.world.level.block.state.properties net.minecraft.world.level.material
net.minecraft.world.level.pathfinder net.minecraft.world.phys net.minecraft.world.level.block.entity net.minecraft.world.level.biome
net.neoforged.bus.api net.neoforged.neoforge.common net.neoforged.neoforge.event.tick net.neoforged.neoforge.event.entity
net.neoforged.neoforge.event.entity.living net.neoforged.neoforge.event.entity.player net.neoforged.neoforge.event.level
com.lycanitesmobs.compat""".split()

EXPLICIT = """net.minecraft.world.entity.player.Player net.minecraft.world.entity.player.Inventory net.minecraft.world.item.ItemStack
net.minecraft.world.entity.Entity net.minecraft.world.level.Level net.minecraft.world.item.Item net.minecraft.world.level.block.Block
net.minecraft.world.level.block.state.BlockState net.minecraft.world.entity.monster.Enemy net.minecraft.world.phys.Vec3
net.minecraft.world.phys.AABB net.minecraft.core.BlockPos net.minecraft.core.Direction net.minecraft.core.Holder
net.minecraft.world.entity.ai.attributes.Attributes
net.minecraft.world.entity.EntityType net.minecraft.world.item.Items net.minecraft.world.level.block.Blocks
net.minecraft.world.damagesource.DamageSource net.minecraft.world.effect.MobEffect net.minecraft.world.effect.MobEffectInstance
net.minecraft.world.effect.MobEffects net.minecraft.sounds.SoundEvent net.minecraft.sounds.SoundEvents net.minecraft.sounds.SoundSource
net.minecraft.world.entity.LivingEntity net.minecraft.world.entity.Mob net.minecraft.nbt.CompoundTag net.minecraft.nbt.ListTag
net.minecraft.world.InteractionHand net.minecraft.world.InteractionResult net.minecraft.world.entity.item.ItemEntity""".split()

# 1.12 simple class names -> 1.21 (word renames on code)
TYPES = [
 ('EntityPlayerMP', 'ServerPlayer'), ('EntityPlayer', 'Player'), ('EntityLivingBase', 'LivingEntity'), ('EntityLiving', 'Mob'),
 ('EntityCreature', 'PathfinderMob'), ('EntityAnimal', 'Animal'), ('EntityTameable', 'TamableAnimal'), ('EntityAgeable', 'AgeableMob'),
 ('IMob', 'Enemy'), ('EntityMob', 'Monster'), ('EntityFlying', 'FlyingMob'), ('EntityItem', 'ItemEntity'), ('EntityXPOrb', 'ExperienceOrb'),
 ('EntityThrowable', 'ThrowableProjectile'), ('EntityArrow', 'AbstractArrow'), ('IProjectile', 'Projectile'), ('EntityVillager', 'Villager'),
 ('EntityIronGolem', 'IronGolem'), ('EntityZombieVillager', 'ZombieVillager'), ('EntityZombie', 'Zombie'), ('EntityPigZombie', 'ZombifiedPiglin'),
 ('EntityWitherSkeleton', 'WitherSkeleton'), ('EntitySkeletonHorse', 'SkeletonHorse'), ('EntitySkeleton', 'Skeleton'), ('EntityStray', 'Stray'),
 ('EntityHusk', 'Husk'), ('EntityCreeper', 'Creeper'), ('EntityGhast', 'Ghast'), ('EntityBlaze', 'Blaze'), ('EntityMagmaCube', 'MagmaCube'),
 ('EntityEnderman', 'EnderMan'), ('EntityWitch', 'Witch'), ('EntitySilverfish', 'Silverfish'), ('EntitySnowman', 'SnowGolem'),
 ('EntityWither', 'WitherBoss'), ('EntityDragon', 'EnderDragon'), ('EntityMinecart', 'AbstractMinecart'), ('EntityBoat', 'Boat'),
 ('EntityFireball', 'AbstractHurtingProjectile'), ('Random', 'RandomSource'),
 ('WorldServer', 'ServerLevel'), ('World', 'Level'), ('IBlockAccess', 'BlockGetter'), ('IBlockState', 'BlockState'), ('Vec3d', 'Vec3'),
 ('AxisAlignedBB', 'AABB'), ('NBTTagCompound', 'CompoundTag'), ('NBTTagList', 'ListTag'), ('NBTTagString', 'StringTag'), ('NBTBase', 'Tag'),
 ('EnumFacing', 'Direction'), ('EnumHand', 'InteractionHand'), ('EnumActionResult', 'InteractionResult'), ('ActionResult', 'InteractionResultHolder'),
 ('PotionEffect', 'MobEffectInstance'), ('SoundCategory', 'SoundSource'), ('MathHelper', 'Mth'), ('DataParameter', 'EntityDataAccessor'),
 ('EntityDataManager', 'SynchedEntityData'), ('DataSerializers', 'EntityDataSerializers'), ('PacketBuffer', 'RegistryFriendlyByteBuf'),
 ('TextComponentString', 'LycText'), ('TextComponentTranslation', 'LycText'), ('ITextComponent', 'Component'), ('EnumDifficulty', 'Difficulty'),
 ('BossInfoServer', 'ServerBossEvent'), ('BossInfo', 'BossEvent'), ('EnumCreatureType', 'MobCategory'), ('IEntityLivingData', 'SpawnGroupData'),
 ('PathNavigateGround', 'GroundPathNavigation'), ('PathNavigate', 'PathNavigation'), ('PathPoint', 'Node'), ('PathNodeType', 'PathType'),
 ('EntityMoveHelper', 'MoveControl'), ('EntityLookHelper', 'LookControl'), ('EntityJumpHelper', 'JumpControl'), ('NodeProcessor', 'NodeEvaluator'),
 ('PathFinder', 'PathFinder'), ('EntityEquipmentSlot', 'EquipmentSlot'), ('ItemBlock', 'BlockItem'), ('ItemFood', 'Item'), ('EnumAction', 'UseAnim'),
 ('ITooltipFlag', 'TooltipFlag'), ('TileEntity', 'BlockEntity'), ('IInventory', 'Container'), ('InventoryPlayer', 'Inventory'),
 ('ItemStackHelper', 'ContainerHelper'), ('MoverType', 'MoverType'), ('IEntityOwnable', 'OwnableEntity'),
 ('EntityDamageSourceIndirect', 'EntityDamageSource'), ('IAttributeInstance', 'AttributeInstance'),
 ('SharedMonsterAttributes', 'Attributes'), ('BlockRenderLayer', 'Object'), ('IStringSerializable', 'StringRepresentable'),
 ('RayTraceResult', 'HitResult'), ('ChunkPos', 'ChunkPos'), ('MapColor', 'MapColor'), ('SoundType', 'SoundType'),
 ('EnumBlockRenderType', 'RenderShape'), ('ItemArmor', 'ArmorItem'), ('Village', 'Object'), ('EnumCreatureAttribute', 'EnumCreatureAttribute'),
]

# member renames: regex (applied in order)
R = [
 # forge side annotations
 (r'@SideOnly\(Side\.\w+\)\s*', ''),
 (r'@Override\s*\n(\s*)(?=@)', r'@Override\n\1'),
 (r'\bMinecraftForge\.EVENT_BUS\b', 'NeoForge.EVENT_BUS'),
 (r'\bFMLCommonHandler\.instance\(\)\.bus\(\)', 'NeoForge.EVENT_BUS'),
 # guava optional
 (r'\bOptional\.absent\(\)', 'Optional.empty()'), (r'\bOptional\.fromNullable\(', 'Optional.ofNullable('), (r'\.orNull\(\)', '.orElse(null)'),
 # data manager
 (r'SynchedEntityData\.createKey\(', 'SynchedEntityData.defineId('), (r'\bEntityDataManager\.createKey\(', 'SynchedEntityData.defineId('),
 (r'\bDataSerializers\.VARINT\b', 'EntityDataSerializers.INT'), (r'\bDataSerializers\.OPTIONAL_BLOCK_POS\b', 'EntityDataSerializers.OPTIONAL_BLOCK_POS'),
 (r'\bDataSerializers\.OPTIONAL_UNIQUE_ID\b', 'EntityDataSerializers.OPTIONAL_UUID'), (r'\bDataSerializers\.ITEM_STACK\b', 'EntityDataSerializers.ITEM_STACK'),
 (r'\bDataSerializers\.', 'EntityDataSerializers.'), (r'\bEntityDataSerializers\.VARINT\b', 'EntityDataSerializers.INT'),
 (r'protected void entityInit\(\) \{', 'protected void defineSynchedData(SynchedEntityData.Builder builder) {'),
 (r'public void entityInit\(\) \{', 'protected void defineSynchedData(SynchedEntityData.Builder builder) {'),
 (r'super\.entityInit\(\);', 'super.defineSynchedData(builder);'),
 (r'this\.dataManager\.register\(', 'builder.define('), (r'\bdataManager\.register\(', 'builder.define('),
 (r'\.getEntityData\(\)', '.getPersistentData()'), (r'\bthis\.dataManager\b', 'this.entityData'), (r'\.getDataManager\(\)', '.getEntityData()'),
 # world
 (r'\.getEntityWorld\(\)', '.level()'), (r'\bgetEntityWorld\(\)', 'level()'),
 (r'\b(this|entity|target|host|player|owner|creature|attacker|entityLiving|livingEntity|entityLivingBase|thrower|shooter|projectile|rider|passenger|parent|partner|minion|this\.host|this\.entity|this\.player|this\.target|creatureEntity|entityCreature|this\.creature|mob|summon|spawnedEntity|entityPlayer|playerEntity)\.world\b(?!\s*=)', r'\1.level()'),
 (r'(?<![\w.])world\.isRemote\b', 'world.isClientSide'), (r'\.isRemote\b', '.isClientSide'),
 (r'\.setBlockState\(([^;]*?),\s*(\d+)\)', r'.setBlock(\1, \2)'),
 (r'\.isAirBlock\(', '.isEmptyBlock('), (r'\.spawnEntity\(', '.addFreshEntity('), (r'\.getTotalWorldTime\(\)', '.getGameTime()'),
 (r'\.getWorldTime\(\)', '.getDayTime()'), (r'\.getEntitiesWithinAABB\(', '.getEntitiesOfClass('), (r'\.getEntityByID\(', '.getEntity('),
 (r'\.isDaytime\(\)', '.isDay()'), (r'\.canBlockSeeSky\(', '.canSeeSky('), (r'\.getTileEntity\(', '.getBlockEntity('),
 (r'\.getMinecraftServer\(\)', '.getServer()'), (r'\.getGameRules\(\)\.getBoolean\("mobGriefing"\)', '.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)'),
 (r'\.getGameRules\(\)\.getBoolean\("doMobSpawning"\)', '.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)'),
 (r'\.getGameRules\(\)\.getBoolean\("doMobLoot"\)', '.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)'),
 (r'\.playSound\(\(Player\)\s*null,', '.playSound(null,'),
 (r'\.getHeight\(\)(?=\s*[;)+\-*<>])', '.getHeight()'),
 # entity fields / methods
 (r'\.posX\b(?!\s*[+\-*/]?=[^=])', '.getX()'), (r'\.posY\b(?!\s*[+\-*/]?=[^=])', '.getY()'), (r'\.posZ\b(?!\s*[+\-*/]?=[^=])', '.getZ()'),
 (r'\.prevPosX\b', '.xo'), (r'\.prevPosY\b', '.yo'), (r'\.prevPosZ\b', '.zo'),
 (r'\.lastTickPosX\b', '.xOld'), (r'\.lastTickPosY\b', '.yOld'), (r'\.lastTickPosZ\b', '.zOld'),
 (r'\.rotationYawHead\b', '.yHeadRot'), (r'\.prevRotationYawHead\b', '.yHeadRotO'), (r'\.renderYawOffset\b', '.yBodyRot'),
 (r'\.prevRenderYawOffset\b', '.yBodyRotO'), (r'\.prevRotationYaw\b', '.yRotO'), (r'\.prevRotationPitch\b', '.xRotO'),
 (r'\.ticksExisted\b', '.tickCount'), (r'\.hurtResistantTime\b', '.invulnerableTime'), (r'\.noClip\b', '.noPhysics'),
 (r'\.experienceValue\b', '.xpReward'), (r'\.isAirBorne\b', '.hasImpulse'), (r'\.velocityChanged\b', '.hurtMarked'),
 (r'\.collidedHorizontally\b', '.horizontalCollision'), (r'\.collidedVertically\b', '.verticalCollision'),
 (r'\.isJumping\b', '.jumping'), (r'\.moveForward\b', '.zza'), (r'\.moveStrafing\b', '.xxa'), (r'\.moveVertical\b', '.yya'),
 (r'\.jumpMovementFactor\b', '.flyingSpeed'), (r'\.deathTime\b', '.deathTime'), (r'\.hurtTime\b', '.hurtTime'),
 (r'\.limbSwingAmount\b', '.walkAnimation.speed()'), (r'\.swingProgress\b', '.attackAnim'), (r'\.isSwingInProgress\b', '.swinging'),
 (r'\.getRNG\(\)', '.getRandom()'), (r'\bgetRNG\(\)', 'getRandom()'), (r'\bthis\.rand\b', 'this.random'), (r'\.rand\b', '.random'),
 (r'\.getEntityBoundingBox\(\)', '.getBoundingBox()'), (r'\bgetEntityBoundingBox\(\)', 'getBoundingBox()'), (r'\.setEntityBoundingBox\(', '.setBoundingBox('),
 (r'\.isEntityAlive\(\)', '.isAlive()'), (r'\bisEntityAlive\(\)', 'isAlive()'), (r'\.setDead\(\)', '.discard()'), (r'\bsetDead\(\)', 'discard()'),
 (r'\.isDead\b', '.isRemoved()'),
 (r'\.getEntityId\(\)', '.getId()'), (r'\bgetEntityId\(\)', 'getId()'), (r'\.getUniqueID\(\)', '.getUUID()'), (r'\bgetUniqueID\(\)', 'getUUID()'),
 (r'\.getPosition\(\)', '.blockPosition()'), (r'\bgetPosition\(\)', 'blockPosition()'),
 (r'\.getPositionVector\(\)', '.position()'), (r'\bgetPositionVector\(\)', 'position()'),
 (r'\.getDistanceSq\(', '.distanceToSqr('), (r'\bgetDistanceSq\(', 'distanceToSqr('), (r'\.getDistance\(', '.distanceTo('), (r'\bgetDistance\(', 'distanceTo('),
 (r'\.setPosition\(', '.setPos('), (r'\bsetPosition\(', 'setPos('), (r'\.setLocationAndAngles\(', '.moveTo('), (r'\bsetLocationAndAngles\(', 'moveTo('),
 (r'\.setPositionAndUpdate\(', '.teleportTo('), (r'\.copyLocationAndAnglesFrom\(', '.copyPosition('),
 (r'\.attackEntityFrom\(', '.hurt('), (r'\bsuper\.attackEntityFrom\(', 'super.hurt('),
 (r'\.getAttackTarget\(\)', '.getTarget()'), (r'\bgetAttackTarget\(\)', 'getTarget()'),
 (r'\.setAttackTarget\(', '.setTarget('), (r'\bsetAttackTarget\(', 'setTarget('),
 (r'\.getRevengeTarget\(\)', '.getLastHurtByMob()'), (r'\bgetRevengeTarget\(\)', 'getLastHurtByMob()'),
 (r'\.getLastAttackedEntity\(\)', '.getLastHurtMob()'), (r'\.getHeldItem\(', '.getItemInHand('), (r'\bgetHeldItem\(', 'getItemInHand('),
 (r'\.getHeldItemMainhand\(\)', '.getMainHandItem()'), (r'\bgetHeldItemMainhand\(\)', 'getMainHandItem()'),
 (r'\.getHeldItemOffhand\(\)', '.getOffhandItem()'), (r'\.setHeldItem\(', '.setItemInHand('),
 (r'\.getItemStackFromSlot\(', '.getItemBySlot('), (r'\.setItemStackToSlot\(', '.setItemSlot('),
 (r'\.addPotionEffect\(', '.addEffect('), (r'\baddPotionEffect\(', 'addEffect('), (r'\.isPotionActive\(', '.hasEffect('), (r'\bisPotionActive\(', 'hasEffect('),
 (r'\.getActivePotionEffect\(', '.getEffect('), (r'\bgetActivePotionEffect\(', 'getEffect('),
 (r'\.removePotionEffect\(', '.removeEffect('), (r'\bremovePotionEffect\(', 'removeEffect('),
 (r'\.getActivePotionEffects\(\)', '.getActiveEffects()'), (r'\bgetActivePotionEffects\(\)', 'getActiveEffects()'),
 (r'\.clearActivePotions\(\)', '.removeAllEffects()'),
 (r'\.getPotion\(\)', '.getEffect()'), (r'\.getNavigator\(\)', '.getNavigation()'), (r'\bgetNavigator\(\)', 'getNavigation()'),
 (r'\.getMoveHelper\(\)', '.getMoveControl()'), (r'\bgetMoveHelper\(\)', 'getMoveControl()'),
 (r'\.getLookHelper\(\)', '.getLookControl()'), (r'\bgetLookHelper\(\)', 'getLookControl()'),
 (r'\.getJumpHelper\(\)', '.getJumpControl()'), (r'\.getEntitySenses\(\)', '.getSensing()'), (r'\bgetEntitySenses\(\)', 'getSensing()'),
 (r'\.canSee\(', '.hasLineOfSight('),
 (r'\.isBurning\(\)', '.isOnFire()'), (r'\bisBurning\(\)', 'isOnFire()'), (r'\.setFire\(', '.igniteForSeconds('), (r'\bsetFire\(', 'igniteForSeconds('),
 (r'\.extinguish\(\)', '.clearFire()'), (r'\.getRidingEntity\(\)', '.getVehicle()'), (r'\bgetRidingEntity\(\)', 'getVehicle()'),
 (r'\.isRiding\(\)', '.isPassenger()'), (r'\bisRiding\(\)', 'isPassenger()'), (r'\.dismountRidingEntity\(\)', '.stopRiding()'),
 (r'\bdismountRidingEntity\(\)', 'stopRiding()'), (r'\.isBeingRidden\(\)', '.isVehicle()'), (r'\bisBeingRidden\(\)', 'isVehicle()'),
 (r'\.removePassengers\(\)', '.ejectPassengers()'), (r'\.isSneaking\(\)', '.isShiftKeyDown()'), (r'\.getLookVec\(\)', '.getLookAngle()'),
 (r'\bgetLookVec\(\)', 'getLookAngle()'),
 (r'\.isChild\(\)', '.isBaby()'), (r'\bisChild\(\)', 'isBaby()'),
 (r'\.getEntityAttribute\(', '.getAttribute('), (r'\bgetEntityAttribute\(', 'getAttribute('),
 (r'\bAttributes\.MAX_HEALTH\b', 'Attributes.MAX_HEALTH'),
 (r'\bSharedMonsterAttributes\.', 'Attributes.'), (r'\bAttributes\.FLYING_SPEED\b', 'Attributes.FLYING_SPEED'),
 (r'\.isOnLadder\(\)', '.onClimbable()'), (r'\.getLeashed\(\)', '.isLeashed()'), (r'\bgetLeashed\(\)', 'isLeashed()'),
 (r'\.getLeashHolder\(\)', '.getLeashHolder()'), (r'\.clearLeashed\(', '.dropLeash('),
 (r'\.isCreativeMode\b', '.instabuild'), (r'\.capabilities\.', '.getAbilities().'), (r'\.disableDamage\b', '.invulnerable'),
 (r'\.isAllowFlying\b', '.mayfly'), (r'\.inventory\.armorInventory\b', '.getInventory().armor'), (r'\.inventory\.mainInventory\b', '.getInventory().items'),
 (r'\b(player|entityPlayer|playerEntity|this\.player)\.inventory\b', r'\1.getInventory()'),
 (r'\.inventory\.currentItem\b', '.getInventory().selected'),
 (r'\.isPlayerSleeping\(\)', '.isSleeping()'), (r'\.isSpectator\(\)', '.isSpectator()'),
 (r'\.getAir\(\)', '.getAirSupply()'), (r'\bgetAir\(\)', 'getAirSupply()'), (r'\.setAir\(', '.setAirSupply('),
 (r'\.getCreatureAttribute\(\)', '.getCreatureAttribute()'),
 (r'\.isOnSameTeam\(', '.isAlliedTo('), (r'\.getTeam\(\)', '.getTeam()'),
 (r'\.getCustomNameTag\(\)', '.getCustomName().getString()'), (r'\.setCustomNameTag\(', '.setCustomNameTag('),
 (r'\.getDisplayName\(\)\.getFormattedText\(\)', '.getDisplayName().getString()'), (r'\.getFormattedText\(\)', '.getString()'),
 (r'\.getUnformattedText\(\)', '.getString()'),
 (r'\.sendMessage\(', '.sendSystemMessage('), (r'\.sendStatusMessage\(', '.displayClientMessage('),
 (r'\.getActiveItemStack\(\)', '.getUseItem()'), (r'\.isHandActive\(\)', '.isUsingItem()'), (r'\.getItemInUseCount\(\)', '.getUseItemRemainingTicks()'),
 (r'\.swingArm\(', '.swing('), (r'\.addVelocity\(', '.push('), (r'\.knockBack\(', '.knockback('),
 (r'\.setSprinting\(', '.setSprinting('), (r'\.getBrightness\(\)', '.getLightLevelDependentMagicValue()'),
 (r'\.getEyeHeight\(\)', '.getEyeHeight()'), (r'\.isInsideOfMaterial\(', '.isEyeInMaterial('),
 (r'\.setSize\(', '.setSize('), (r'\.getMaxHealth\(\)', '.getMaxHealth()'),
 (r'\.dropItem\((\w+), (\d+)\)', r'.spawnAtLocation(\1, \2)'), (r'\.entityDropItem\(', '.spawnAtLocation('),
 (r'\.setNoAI\(', '.setNoAi('), (r'\.isAIDisabled\(\)', '.isNoAi()'), (r'\.enablePersistence\(\)', '.setPersistenceRequired()'),
 (r'\.setSilent\(', '.setSilent('), (r'\.setInvisible\(', '.setInvisible('), (r'\.setGlowing\(', '.setGlowingTag('),
 (r'\.getMaxFallHeight\(\)', '.getMaxFallDistance()'),
 (r'\.isWet\(\)', '.isInWaterRainOrBubble()'), (r'\.getMountedYOffset\(\)', '.getMountedYOffset()'),
 (r'\.onKillCommand\(\)', '.kill()'), (r'\.getHorizontalFacing\(\)', '.getDirection()'),

 # blocks / states
 (r'\.getDefaultState\(\)', '.defaultBlockState()'), (r'\bgetDefaultState\(\)', 'defaultBlockState()'), (r'\.withProperty\(', '.setValue('),
 
 (r'\.isOpaqueCube\(\)', '.canOcclude()'),
 (r'\.getBlockHardness\(([^,()]+), ([^,()]+)\)', r'.getDestroySpeed(\1, \2)'),
 (r'\.getPlayerRelativeBlockHardness\(', '.getDestroyProgress('),
 (r'\bBlocks\.FLOWING_WATER\b', 'Blocks.WATER'), (r'\bBlocks\.FLOWING_LAVA\b', 'Blocks.LAVA'), (r'\bBlocks\.MOB_SPAWNER\b', 'Blocks.SPAWNER'),
 (r'\bBlocks\.GRASS\b', 'Blocks.GRASS_BLOCK'), (r'\bBlocks\.TALLGRASS\b', 'Blocks.SHORT_GRASS'), (r'\bBlocks\.GRASS_PATH\b', 'Blocks.DIRT_PATH'),
 (r'\bBlocks\.HARDENED_CLAY\b', 'Blocks.TERRACOTTA'), (r'\bBlocks\.LIT_PUMPKIN\b', 'Blocks.JACK_O_LANTERN'),
 (r'\bBlocks\.LIT_REDSTONE_ORE\b', 'Blocks.REDSTONE_ORE'), (r'\bBlocks\.LOG\b', 'Blocks.OAK_LOG'), (r'\bBlocks\.PLANKS\b', 'Blocks.OAK_PLANKS'),
 (r'\bBlocks\.MAGMA\b', 'Blocks.MAGMA_BLOCK'), (r'\bBlocks\.MELON_BLOCK\b', 'Blocks.MELON'), (r'\bBlocks\.MONSTER_EGG\b', 'Blocks.INFESTED_STONE'),
 (r'\bBlocks\.SNOW_LAYER\b', 'Blocks.SNOW'), (r'\bBlocks\.SNOW\b(?!_)', 'Blocks.SNOW_BLOCK'), (r'\bBlocks\.STONE_SLAB\b', 'Blocks.SMOOTH_STONE_SLAB'),
 (r'\bBlocks\.DOUBLE_STONE_SLAB\b', 'Blocks.SMOOTH_STONE'), (r'\bBlocks\.TRAPDOOR\b', 'Blocks.OAK_TRAPDOOR'),
 (r'\bBlocks\.WATERLILY\b', 'Blocks.LILY_PAD'), (r'\bBlocks\.WOODEN_BUTTON\b', 'Blocks.OAK_BUTTON'),
 (r'\bBlocks\.WOODEN_PRESSURE_PLATE\b', 'Blocks.OAK_PRESSURE_PLATE'), (r'\bBlocks\.WOOL\b', 'Blocks.WHITE_WOOL'),
 (r'\bBlocks\.CONCRETE_POWDER\b', 'Blocks.WHITE_CONCRETE_POWDER'), (r'\bBlocks\.DOUBLE_PLANT\b', 'Blocks.TALL_GRASS'),
 (r'\bBlocks\.RED_MUSHROOM_BLOCK\b', 'Blocks.RED_MUSHROOM_BLOCK'), (r'\bBlocks\.WEB\b', 'Blocks.COBWEB'), (r'\bBlocks\.STONEBRICK\b', 'Blocks.STONE_BRICKS'),
 (r'\bItems\.FISH\b', 'Items.COD'), (r'\bItems\.COOKED_FISH\b', 'Items.COOKED_COD'), (r'\bItems\.DYE\b', 'Items.INK_SAC'),
 (r'\bItems\.MELON\b', 'Items.MELON_SLICE'), (r'\bItems\.SPAWN_EGG\b', 'Items.PIG_SPAWN_EGG'), (r'\bItems\.FIRE_CHARGE\b', 'Items.FIRE_CHARGE'),
 (r'\bItems\.MUSHROOM_STEW\b', 'Items.MUSHROOM_STEW'), (r'\bItems\.DIAMOND_HORSE_ARMOR\b', 'Items.DIAMOND_HORSE_ARMOR'),
 (r'\bMobEffects\.HASTE\b', 'MobEffects.DIG_SPEED'), (r'\bMobEffects\.MINING_FATIGUE\b', 'MobEffects.DIG_SLOWDOWN'),
 (r'\bMobEffects\.SLOWNESS\b', 'MobEffects.MOVEMENT_SLOWDOWN'), (r'\bMobEffects\.SPEED\b', 'MobEffects.MOVEMENT_SPEED'),
 (r'\bMobEffects\.STRENGTH\b', 'MobEffects.DAMAGE_BOOST'), (r'\bMobEffects\.INSTANT_DAMAGE\b', 'MobEffects.HARM'),
 (r'\bMobEffects\.INSTANT_HEALTH\b', 'MobEffects.HEAL'), (r'\bMobEffects\.JUMP_BOOST\b', 'MobEffects.JUMP'),
 (r'\bMobEffects\.NAUSEA\b', 'MobEffects.CONFUSION'), (r'\bMobEffects\.RESISTANCE\b', 'MobEffects.DAMAGE_RESISTANCE'),
 (r'\bSoundEvents\.ENTITY_HOSTILE_BIG_FALL\b', 'SoundEvents.HOSTILE_BIG_FALL'), (r'\bSoundEvents\.ENTITY_HOSTILE_SMALL_FALL\b', 'SoundEvents.HOSTILE_SMALL_FALL'),
 (r'\bSoundEvents\.ENTITY_HOSTILE_SPLASH\b', 'SoundEvents.HOSTILE_SPLASH'), (r'\bSoundEvents\.ENTITY_HOSTILE_SWIM\b', 'SoundEvents.HOSTILE_SWIM'),
 (r'\bSoundEvents\.ENTITY_PLAYER_ATTACK_SWEEP\b', 'SoundEvents.PLAYER_ATTACK_SWEEP'), (r'\bSoundEvents\.ENTITY_SHEEP_SHEAR\b', 'SoundEvents.SHEEP_SHEAR'),
 (r'\bSoundEvents\.ITEM_HOE_TILL\b', 'SoundEvents.HOE_TILL'),
 # particles
 (r'EnumParticleTypes\.SMOKE_NORMAL\b', 'ParticleTypes.SMOKE'), (r'EnumParticleTypes\.SMOKE_LARGE\b', 'ParticleTypes.LARGE_SMOKE'),
 (r'EnumParticleTypes\.SPELL_WITCH\b', 'ParticleTypes.WITCH'), (r'EnumParticleTypes\.SNOWBALL\b', 'ParticleTypes.ITEM_SNOWBALL'),
 (r'EnumParticleTypes\.SNOW_SHOVEL\b', 'ParticleTypes.ITEM_SNOWBALL'), (r'EnumParticleTypes\.REDSTONE\b', 'DustParticleOptions.REDSTONE'),
 (r'EnumParticleTypes\.VILLAGER_HAPPY\b', 'ParticleTypes.HAPPY_VILLAGER'), (r'EnumParticleTypes\.VILLAGER_ANGRY\b', 'ParticleTypes.ANGRY_VILLAGER'),
 (r'EnumParticleTypes\.DRIP_LAVA\b', 'ParticleTypes.DRIPPING_LAVA'), (r'EnumParticleTypes\.DRIP_WATER\b', 'ParticleTypes.DRIPPING_WATER'),
 (r'EnumParticleTypes\.WATER_DROP\b', 'ParticleTypes.RAIN'), (r'EnumParticleTypes\.WATER_BUBBLE\b', 'ParticleTypes.BUBBLE'),
 (r'EnumParticleTypes\.TOWN_AURA\b', 'ParticleTypes.MYCELIUM'), (r'EnumParticleTypes\.EXPLOSION_NORMAL\b', 'ParticleTypes.POOF'),
 (r'EnumParticleTypes\.EXPLOSION_LARGE\b', 'ParticleTypes.EXPLOSION'), (r'EnumParticleTypes\.EXPLOSION_HUGE\b', 'ParticleTypes.EXPLOSION_EMITTER'),
 (r'EnumParticleTypes\.CRIT_MAGIC\b', 'ParticleTypes.ENCHANTED_HIT'), (r'EnumParticleTypes\.WATER_SPLASH\b', 'ParticleTypes.SPLASH'),
 (r'EnumParticleTypes\.ENCHANTMENT_TABLE\b', 'ParticleTypes.ENCHANT'), (r'EnumParticleTypes\.BLOCK_CRACK\b', 'ParticleTypes.POOF'),
 (r'EnumParticleTypes\.', 'ParticleTypes.'),
 # nbt
 (r'\.setInteger\(', '.putInt('), (r'\.getInteger\(', '.getInt('), (r'\.setBoolean\(', '.putBoolean('), (r'\.setFloat\(', '.putFloat('),
 (r'\.setDouble\(', '.putDouble('), (r'\.setString\(', '.putString('), (r'\.setByte\(', '.putByte('), (r'\.setShort\(', '.putShort('),
 (r'\.setLong\(', '.putLong('), (r'\.setTag\(', '.put('), (r'\.getTag\(', '.get('), (r'\.hasKey\(', '.contains('),
 (r'\.getTagList\(', '.getList('), (r'\.tagCount\(\)', '.size()'), (r'\.getCompoundTagAt\(', '.getCompound('), (r'\.appendTag\(', '.add('),
 (r'\.getStringTagAt\(', '.getString('), (r'\.getUniqueId\(', '.getUUID('), (r'\.setUniqueId\(', '.putUUID('), (r'\.hasUniqueId\(', '.hasUUID('),
 (r'\.removeTag\(', '.remove('), (r'\.getKeySet\(\)', '.getAllKeys()'), (r'\.setIntArray\(', '.putIntArray('),
 
 # items
 (r'\.getItemDamage\(\)', '.getDamageValue()'), (r'\.setItemDamage\(', '.setDamageValue('), (r'\.getMaxDamage\(\)', '.getMaxDamage()'),
 (r'\.getTranslationKey\(\)', '.getDescriptionId()'), (r'\.getDisplayName\(\)', '.getHoverName()'),
 (r'\.getItemFromBlock\(', '.byBlock('),
 (r'\bItem\.getItemFromBlock\(', 'Item.byBlock('),
 # resource locations
 (r'new ResourceLocation\(([^(),]+(?:\([^()]*\))?[^(),]*),\s*', r'ResourceLocation.fromNamespaceAndPath(\1, '),
 (r'new ResourceLocation\(', 'ResourceLocation.parse('),
 # math
 (r'\bMth\.sqrt\(', '(float)Math.sqrt('), (r'\bMathHelper\.sqrt\(', '(float)Math.sqrt('),
 (r'\bMth\.wrapDegrees\(', 'Mth.wrapDegrees('), (r'\bMth\.floor\(', 'Mth.floor('),
 (r'\bMth\.clamp\(', 'Mth.clamp('), (r'\bMth\.abs\(', 'Mth.abs('), (r'\bMth\.atan2\(', 'Mth.atan2('),
 # block pos / vec
 (r'\.getX\(\)\.getX\(\)', '.getX()'),
 (r'\.add\((\-?[\w.()]+(?:\s*[+\-*/]\s*[\w.()]+)*), (\-?[\w.()]+(?:\s*[+\-*/]\s*[\w.()]+)*), (\-?[\w.()]+(?:\s*[+\-*/]\s*[\w.()]+)*)\)', r'.offset(\1, \2, \3)'),
 (r'\.getXOffset\(\)', '.getStepX()'), (r'\.getYOffset\(\)', '.getStepY()'), (r'\.getZOffset\(\)', '.getStepZ()'),
 (r'\.rotateYCCW\(\)', '.getCounterClockWise()'), (r'\.rotateY\(\)', '.getClockWise()'),
 (r'\.up\(\)', '.above()'), (r'\.down\(\)', '.below()'), (r'\.up\(', '.above('), (r'\.down\(', '.below('),
 (r'\.lengthVector\(\)', '.length()'), (r'\.normalize\(\)', '.normalize()'), (r'\.squareDistanceTo\(', '.distanceToSqr('),
 (r'\.crossProduct\(', '.cross('), (r'\.dotProduct\(', '.dot('), (r'\.addVector\(', '.add('), (r'\.scale\(', '.scale('),
 (r'\.grow\(', '.inflate('), (r'\.expand\(', '.expandTowards('), (r'\.offset\((\w+)\)(?=\s*[;,)])', r'.relative(\1)'),
 (r'\.contract\(', '.contract('), (r'\.minX\b', '.minX'), (r'\.calculateIntercept\(', '.clip('),
 (r'\.hitVec\b', '.getLocation()'), (r'\.typeOfHit\b', '.getType()'), (r'HitResult\.Type\.BLOCK\b', 'HitResult.Type.BLOCK'),
 
 (r'new BlockPos\.MutableBlockPos\(', 'new BlockPos.MutableBlockPos('), (r'BlockPos\.PooledMutableBlockPos\.retain\(\)', 'new BlockPos.MutableBlockPos()'),
 (r'BlockPos\.PooledMutableBlockPos', 'BlockPos.MutableBlockPos'), (r'\.setPos\(', '.set('),
 (r'new BlockPos\(([^;]*?)\)', lambda m: 'new BlockPos(' + m.group(1) + ')'),
 # damage sources (vanilla ones; Lycanites ones are fixed by hand)
 (r'\bDamageSource\.cause(\w+)Damage\(', r'EntityDamageSource.cause\1Damage('), (r'new DamageSource\(', 'EntityDamageSource.named('),
 (r'DamageSource\.IN_FIRE\b', 'LycDamage.IN_FIRE'), (r'DamageSource\.ON_FIRE\b', 'LycDamage.ON_FIRE'), (r'DamageSource\.LAVA\b', 'LycDamage.LAVA'),
 (r'DamageSource\.DROWN\b', 'LycDamage.DROWN'), (r'DamageSource\.MAGIC\b', 'LycDamage.MAGIC'), (r'DamageSource\.WITHER\b', 'LycDamage.WITHER'),
 (r'DamageSource\.FALL\b', 'LycDamage.FALL'), (r'DamageSource\.GENERIC\b', 'LycDamage.GENERIC'), (r'DamageSource\.OUT_OF_WORLD\b', 'LycDamage.OUT_OF_WORLD'),
 (r'DamageSource\.CACTUS\b', 'LycDamage.CACTUS'), (r'DamageSource\.IN_WALL\b', 'LycDamage.IN_WALL'), (r'DamageSource\.STARVE\b', 'LycDamage.STARVE'),
 (r'DamageSource\.HOT_FLOOR\b', 'LycDamage.HOT_FLOOR'), (r'DamageSource\.ANVIL\b', 'LycDamage.ANVIL'), (r'DamageSource\.LIGHTNING_BOLT\b', 'LycDamage.LIGHTNING_BOLT'),
 (r'\.getTrueSource\(\)', '.getEntity()'), (r'\.getImmediateSource\(\)', '.getDirectEntity()'), (r'\.isFireDamage\(\)', '.is(DamageTypeTags.IS_FIRE)'),
 (r'\.isProjectile\(\)', '.is(DamageTypeTags.IS_PROJECTILE)'), (r'\.isExplosion\(\)', '.is(DamageTypeTags.IS_EXPLOSION)'),
 (r'\.isUnblockable\(\)', '.is(DamageTypeTags.BYPASSES_ARMOR)'), (r'\.isMagicDamage\(\)', '.is(DamageTypeTags.WITCH_RESISTANT_TO)'),
 (r'\.getDamageType\(\)', '.getMsgId()'), (r'\.damageType\b', '.getMsgId()'),
 # text
 (r'new LycText\(', 'LycText.of('),
 (r'new RegistryFriendlyByteBuf\((\w+)\)', r'LycBuf.of(\1)'), (r'\.writeString\(', '.writeUtf('), (r'\.readString\(', '.readUtf('),
 (r'\.writeCompoundTag\(', '.writeNbt('), (r'\.readCompoundTag\(\)', '.readNbt()'), (r'\.writeUniqueId\(', '.writeUUID('), (r'\.readUniqueId\(\)', '.readUUID()'),
 (r'(\w+)\.writeItemStack\(([^;]*)\);', r'ItemStack.OPTIONAL_STREAM_CODEC.encode(\1, \2);'), (r'(\w+)\.readItemStack\(\)', r'ItemStack.OPTIONAL_STREAM_CODEC.decode(\1)'),
 # misc
 (r'\bInteractionResult\.PASS\b', 'InteractionResult.PASS'), (r'\bInteractionResult\.SUCCESS\b', 'InteractionResult.SUCCESS'),
 (r'\bDifficulty\.PEACEFUL\b', 'Difficulty.PEACEFUL'), (r'\bMobCategory\.WATER_CREATURE\b', 'MobCategory.WATER_CREATURE'),
 (r'\bSoundSource\.HOSTILE\b', 'SoundSource.HOSTILE'), (r'\bSoundSource\.NEUTRAL\b', 'SoundSource.NEUTRAL'),
 (r'\bInteractionHand\.MAIN_HAND\b', 'InteractionHand.MAIN_HAND'), (r'\bEquipmentSlot\.MAINHAND\b', 'EquipmentSlot.MAINHAND'),
 (r'\bLevel\.MAX_ENTITY_RADIUS\b', '2.0'),
 (r'((?:this\.)?\w+(?:\(\))?(?:\.\w+(?:\(\))?)*)\.provider\.getDimension\(\)', r'LycDim.id(\1)'),
 (r'\.getBiome\(([^;]*?)\)(?=[;.)])', r'.getBiome(\1).value()'),
 (r'\bthis\.width\b(?!\s*=[^=])', 'this.getBbWidth()'), (r'\bthis\.height\b(?!\s*=[^=])', 'this.getBbHeight()'),
 (r'\b(entity|target|host|this\.host|player|owner|attackTarget|entityLiving|livingEntity|rider|passenger|minion|creature|partner|getTarget\(\)|this\.getTarget\(\)|this\.host\.getTarget\(\)|this\.target|this\.entity|targetEntity|hitEntity|entityHit|projectile|parent|this\.parent|thrower|getControllingPassenger\(\)|this\.getControllingPassenger\(\)|getVehicle\(\)|this\.getVehicle\(\))\.width\b', r'\1.getBbWidth()'),
 (r'\b(entity|target|host|this\.host|player|owner|attackTarget|entityLiving|livingEntity|rider|passenger|minion|creature|partner|getTarget\(\)|this\.getTarget\(\)|this\.host\.getTarget\(\)|this\.target|this\.entity|targetEntity|hitEntity|entityHit|projectile|parent|this\.parent|thrower|getControllingPassenger\(\)|this\.getControllingPassenger\(\)|getVehicle\(\)|this\.getVehicle\(\))\.height\b', r'\1.getBbHeight()'),
 (r'\bthis\.onGround\b(?!\s*=[^=])', 'this.onGround()'), (r'\b(\w+)\.onGround\b(?!\s*=[^=]|\()', r'\1.onGround()'),
 (r'\bthis\.isImmuneToFire\b(?!\s*=[^=]|\s*\()', 'this.fireImmune()'), (r'\.isImmuneToFire\(\)', '.fireImmune()'),
 (r'\bthis\.stepHeight\s*=\s*([^;]+);', r'this.setStepHeightCompat(\1);'),
]


def find_recv_start(s, i):
    """i = index of the '.' before a method name; returns start index of the receiver expression."""
    j = i
    while j > 0:
        c = s[j - 1]
        if c.isspace() and s[j] == '.':
            k = j - 1
            while k > 0 and s[k].isspace():
                k -= 1
            if s[k].isalnum() or s[k] in '_)]':
                j = k + 1
                continue
            break
        if c.isalnum() or c in '_$.':
            j -= 1
        elif c in ')]':
            close = c; open_ = '(' if c == ')' else '['
            depth = 0; k = j - 1
            while k >= 0:
                if s[k] == close: depth += 1
                elif s[k] == open_:
                    depth -= 1
                    if depth == 0: break
                k -= 1
            j = k
            # a call: keep going over the method name; a cast/group: stop after it unless preceded by identifier
        else:
            break
    return j

def split_args(a):
    out = []; depth = 0; cur = ''
    for c in a:
        if c in '([{': depth += 1
        elif c in ')]}': depth -= 1
        if c == ',' and depth == 0:
            out.append(cur.strip()); cur = ''
        else:
            cur += c
    if cur.strip(): out.append(cur.strip())
    return out

def recv_rewrite(s, method, template):
    """Rewrites RECV.method(ARGS) to template.format(r=RECV, a=ARGS)."""
    out = []
    pos = 0
    pat = re.compile(r'\.' + method + r'\(')
    while True:
        m = pat.search(s, pos)
        if not m:
            out.append(s[pos:]); break
        start = find_recv_start(s, m.start())
        # args
        depth = 1; k = m.end()
        while k < len(s) and depth:
            if s[k] == '(': depth += 1
            elif s[k] == ')': depth -= 1
            k += 1
        recv = s[start:m.start()]; args = s[m.end():k - 1]
        if not recv:
            out.append(s[pos:m.end()]); pos = m.end(); continue
        out.append(s[pos:start]); out.append(template(recv, args) if callable(template) else template.format(r=recv, a=args)); pos = k
    return ''.join(out)

RECV = [
 ('getMaterial', 'Material.of({r})'),
 ('getPathToXYZ', '{r}.createPath({a}, 0)'), ('getPathToEntityLiving', '{r}.createPath({a}, 0)'), ('getPathToPos', '{r}.createPath({a}, 0)'),
 ('getTagCompound', 'LycNBT.getTag({r})'), ('hasTagCompound', 'LycNBT.hasTag({r})'), ('setTagCompound', 'LycNBT.setTag({r}, {a})'),
 ('isFullBlock', 'LycBlocks.isFullBlock({r})'), ('isFullCube', 'LycBlocks.isFullBlock({r})'),
 ('isItemEqual', 'ItemStack.isSameItem({r}, {a})'),
 ('getRegistryName', 'LycReg.key({r})'),
 ('setRegistryName', 'LycReg.setName({r}, {a})'),
 ('getCreativeTab', 'LycReg.tab({r})'),
 ('rayTraceBlocks', 'LycWorld.rayTraceBlocks({r}, {a})'),
 ('setBlockToAir', '{r}.removeBlock({a}, false)'),
 ('isShield', lambda r, a: split_args(a)[0] + '.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK)'),
 ('attackEntityAsMob', lambda r, a: '%s.%s(%s)' % (r, 'doHurtTarget' if len(split_args(a)) == 1 else 'attackEntityAsMob', a)),
 ('spawnParticle', lambda r, a: '%s.addParticle(%s)' % (r, ', '.join(split_args(a)[:7]))),
 ('setBlockState', lambda r, a: '%s.setBlock(%s%s)' % (r, a, ', 3' if len(split_args(a)) == 2 else '')),
 ('playSound', lambda r, a: '%s.%s(%s)' % (r, 'playLocalSound' if len(split_args(a)) == 8 else 'playSound', a)),
 ('distanceSq', lambda r, a: '%s.%s(%s)' % (r, 'distSqr' if len(split_args(a)) == 1 else 'distToLowCornerSqr', a)),
 ('getCollisionBoxes', lambda r, a: 'LycWorld.getCollisionBoxes(%s, %s)' % (r, a)),
 ('isAir', lambda r, a: (split_args(a)[0] + '.isAir()') if len(split_args(a)) == 3 else '%s.isAir(%s)' % (r, a)),
 ('getAttributeValue', lambda r, a: '%s.%s(%s)' % (r, 'getValue' if not a.strip() else 'getAttributeValue', a)),
]


PRE = [
 (r'new Random\(', 'RandomSource.create('),


 (r'\.getDifficultyForLocation\(', '.getCurrentDifficultyAt('), (r'\.getAdditionalDifficulty\(\)', '.getEffectiveDifficulty()'),
 (r'(PathType\.\w+)\.getPriority\(\)', r'\1.getMalus()'), (r'\.canPassengerSteer\(\)', '.isControlledByLocalInstance()'),
 (r'\bcanPassengerSteer\(\)', 'isControlledByLocalInstance()'), (r'\.isActiveItemStackBlocking\(\)', '.isBlocking()'),
 (r'\.getLook\(', '.getViewVector('), (r'\.canEntityBeSeen\(', '.hasLineOfSight('), (r'\.getLightBrightness\(', '.getLightLevelDependentMagicValue('),
 (r'\.provider\.getActualHeight\(\)', '.getMaxBuildHeight()'), (r'\.provider\.getHeight\(\)', '.getMaxBuildHeight()'),
 (r'\.updateArmSwingProgress\(\)', '.updateSwingTime()'), (r'\bthis\.idleTime\b', 'this.noActionTime'), (r'\bthis\.inWater\b', 'this.wasTouchingWater'),
 (r'\.isItemStackDamageable\(\)', '.isDamageableItem()'), (r'\.getAIMoveSpeed\(\)', '.getSpeed()'), (r'\bgetAIMoveSpeed\(\)', 'getSpeed()'),
 (r'\.setAIMoveSpeed\(', '.setSpeed('), (r'\.canBePushed\(\)', '.isPushable()'), (r'\.getCompoundTag\(', '.getCompound('),
 (r'\bForgeHooks\.onLivingJump\(', 'CommonHooks.onLivingJump('), (r'\.isRidingOrBeingRiddenBy\(', '.hasIndirectPassenger('),
 (r'\.getEntityAttribute\(', '.getAttribute('), (r'\.isPVPEnabled\(\)', '.isPvpAllowed()'), (r'\.getBlockState\(\)\.getBlock\(\)', '.getBlockState().getBlock()'),
 (r'\.getNodeProcessor\(\)', '.getNodeEvaluator()'), (r'\.setRevengeTarget\(', '.setLastHurtByMob('), (r'\bsetRevengeTarget\(', 'setLastHurtByMob('),
 (r'\.canEntityBeSeen\(', '.hasLineOfSight('), (r'\.getCreatureAttribute\(\)', '.getCreatureAttribute()'),
 (r'\.isOpaqueCube\(\)', '.canOcclude()'), (r'\.getLightValue\(\)', '.getLightEmission()'), (r'\.getDisplayName\(\)', '.getDisplayName()'),
 (r'\bItemStack\.EMPTY\b', 'ItemStack.EMPTY'), (r'\.getSlipperiness\(', '.getFriction('), (r'\.slipperiness\b', '.getFriction()'),
 (r'\.isInvisibleToPlayer\(', '.isInvisibleTo('), (r'\.getPassengers\(\)', '.getPassengers()'), (r'\.isPotionApplicable\(', '.canBeAffected('),
 (r'\.getMaxHealth\(\)', '.getMaxHealth()'), (r'\.setGrowingAge\(', '.setAge('), (r'\.getGrowingAge\(\)', '.getAge()'),
 (r'\.getAttackingEntity\(\)', '.getKillCredit()'), (r'\.isOnSameTeam\(', '.isAlliedTo('), (r'\.canAttackClass\(', '.canAttackClass('),
 (r'\.getTotalArmorValue\(\)', '.getArmorValue()'), (r'\.isInRangeToRenderDist\(', '.shouldRenderAtSqrDistance('),
 (r'\.dropItem\(', '.spawnAtLocation('), (r'\.getItemDropChance\(', '.getEquipmentDropChance('),
 (r'\.getEntityString\(\)', '.getEncodeId()'), (r'\.isOutsideBorder\(\)', '.isOutsideBorderCompat()'),
 (r'new TextComponentString\(', 'Component.literal('), (r'new TextComponentTranslation\(', 'Component.translatable('),
 (r'([\w.()]+) == DamageSource\.(IN_FIRE|ON_FIRE|LAVA|DROWN|MAGIC|WITHER|FALL|GENERIC|CACTUS|IN_WALL|STARVE|HOT_FLOOR|LIGHTNING_BOLT)\b', r'\1.is(DamageTypes.\2)'),
 (r'([\w.()]+) != DamageSource\.(IN_FIRE|ON_FIRE|LAVA|DROWN|MAGIC|WITHER|FALL|GENERIC|CACTUS|IN_WALL|STARVE|HOT_FLOOR|LIGHTNING_BOLT)\b', r'!\1.is(DamageTypes.\2)'),
 (r'DamageSource\.OUT_OF_WORLD\b', 'LycDamage.src(DamageTypes.FELL_OUT_OF_WORLD)'), (r'DamageSource\.ANVIL\b', 'LycDamage.src(DamageTypes.FALLING_ANVIL)'),
 (r'DamageSource\.(IN_FIRE|ON_FIRE|LAVA|DROWN|MAGIC|WITHER|FALL|GENERIC|CACTUS|IN_WALL|STARVE|HOT_FLOOR|LIGHTNING_BOLT)\b', r'LycDamage.src(DamageTypes.\1)'),
 (r'new BlockPos\(', 'LycPos.of('),
 (r'\.tryMoveToXYZ\(', '.moveTo('), (r'\.tryMoveToEntityLiving\(', '.moveTo('), (r'\.setPath\(', '.moveTo('),
 (r'\.clearPath\(\)', '.stop()'), (r'\.noPath\(\)', '.isDone()'), (r'\.getPath\(\)', '.getPath()'),
 (r'\.setCanSwim\(', '.setCanFloat('), (r'\.setBreakDoors\(', '.setCanOpenDoors('), (r'\.setEnterDoors\(', '.setCanPassDoors('),
 (r'\.getFinalPathPoint\(\)', '.getEndNode()'), (r'\.getCurrentPathLength\(\)', '.getNodeCount()'), (r'\.getPathPointFromIndex\(', '.getNode('),
 (r'\.isFinished\(\)', '.isDone()'), (r'\.getCurrentPathIndex\(\)', '.getNextNodeIndex()'),
 (r'\.setPathPriority\(', '.setPathfindingMalus('), (r'\.getPathPriority\(', '.getPathfindingMalus('),
 (r'\.setMoveTo\(', '.setWantedPosition('), (r'\.isUpdating\(\)', '.hasWanted()'), (r'\.getX\(\)Pos\b', '.getWantedX()'),
 (r'\.setLookPositionWithEntity\(', '.setLookAt('), (r'\.setLookPosition\(', '.setLookAt('),
 (r'\.getPosX\(\)', '.getWantedX()'), (r'\.getPosY\(\)', '.getWantedY()'), (r'\.getPosZ\(\)', '.getWantedZ()'),
 (r'\.setJumping\(\)', '.jump()'),
]

POST = [
 (r'\.isPushedByWater\(\)', '.isPushedByFluid()'), (r'super\.getAlwaysRenderNameTagForRender\(\)', 'super.shouldShowName()'),
 (r'\bcanBreatheUnderwater\(\)', 'canBreatheUnderwater112()'), (r'(\w+)\.openGui\(LycanitesMobs\.instance, ', r'GuiHandler.openGui(\1, '),
 (r'\.openContainer\b', '.containerMenu'), (r'\.setPickupDelay\(', '.setPickUpDelay('), (r'\.addItemStackToInventory\(', '.getInventory().add('),
 (r'\.getLightFromNeighbors\(', '.getMaxLocalRawBrightness('),
 (r'public void onUpdate\(\) \{', 'public void tick() {'), (r'super\.onUpdate\(\);', 'super.tick();'),
 (r'\bvoid onLivingUpdate\(\)', 'void aiStep()'), (r'super\.onLivingUpdate\(\)', 'super.aiStep()'), (r'\bthis\.onLivingUpdate\(\)', 'this.aiStep()'),
 (r'\bvoid updateAITasks\(\)', 'void customServerAiStep()'), (r'super\.updateAITasks\(\)', 'super.customServerAiStep()'),
 (r'\bvoid writeEntityToNBT\(CompoundTag', 'void addAdditionalSaveData(CompoundTag'), (r'super\.writeEntityToNBT\(', 'super.addAdditionalSaveData('),
 (r'\bvoid readEntityFromNBT\(CompoundTag', 'void readAdditionalSaveData(CompoundTag'), (r'super\.readEntityFromNBT\(', 'super.readAdditionalSaveData('),
 (r'\bthis\.writeEntityToNBT\(', 'this.addAdditionalSaveData('), (r'\bthis\.readEntityFromNBT\(', 'this.readAdditionalSaveData('),
 (r'\bvoid onDeath\(DamageSource', 'void die(DamageSource'), (r'super\.onDeath\(', 'super.die('),
 (r'\bboolean attackEntityFrom\(DamageSource', 'boolean hurt(DamageSource'),
 (r'\bboolean attackEntityAsMob\(Entity (\w+)\)', r'boolean doHurtTarget(Entity \1)'), (r'super\.attackEntityAsMob\((\w+)\)', r'super.doHurtTarget(\1)'),
 (r'\bvoid damageEntity\(DamageSource', 'void actuallyHurt(DamageSource'), (r'super\.damageEntity\(', 'super.actuallyHurt('),
 (r'\bboolean isEntityInvulnerable\(DamageSource', 'boolean isInvulnerableTo(DamageSource'), (r'super\.isEntityInvulnerable\(', 'super.isInvulnerableTo('),
 (r'\bboolean isPotionApplicable\(MobEffectInstance', 'boolean canBeAffected(MobEffectInstance'), (r'super\.isPotionApplicable\(', 'super.canBeAffected('),
 (r'\bvoid updateFallState\(', 'void checkFallDamage('), (r'super\.updateFallState\(', 'super.checkFallDamage('),
 (r'\bboolean isOnLadder\(\)', 'boolean onClimbable()'), (r'super\.isOnLadder\(\)', 'super.onClimbable()'),
 (r'\bint getTalkInterval\(\)', 'int getAmbientSoundInterval()'),
 (r'\bvoid addTrackingPlayer\(ServerPlayer', 'void startSeenByPlayer(ServerPlayer'), (r'super\.addTrackingPlayer\(', 'super.startSeenByPlayer('),
 (r'\bvoid removeTrackingPlayer\(ServerPlayer', 'void stopSeenByPlayer(ServerPlayer'), (r'super\.removeTrackingPlayer\(', 'super.stopSeenByPlayer('),
 (r'\bboolean getAlwaysRenderNameTagForRender\(\)', 'boolean shouldShowName()'),
 (r'\bboolean canBeRidden\(Entity', 'boolean canAddPassenger(Entity'), (r'super\.canBeRidden\(', 'super.canAddPassenger('),
 (r'\bboolean isPushedByWater\(\)', 'boolean isPushedByFluid()'), (r'super\.isPushedByWater\(\)', 'super.isPushedByFluid()'),
 (r'\b(?:ItemEntity|EntityItem) entityDropItem\(ItemStack', 'ItemEntity spawnAtLocation(ItemStack'),
 (r'\bvoid onStruckByLightning\(', 'void thunderHit(ServerLevel lycLevel, '), (r'\bvoid onKillEntity\(LivingEntity', 'boolean killedEntity(ServerLevel lycLevel, LivingEntity'),
 (r'\bvoid setInWeb\(\)', 'void makeStuckInBlock(BlockState lycState, Vec3 lycMotion)'),
 (r'\bboolean canBreatheUnderwater\(\)', 'boolean canBreatheUnderwater()'),
 (r'\bvoid notifyDataManagerChange\(EntityDataAccessor', 'void onSyncedDataUpdated(EntityDataAccessor'),
 (r'\bboolean processInteract\(Player', 'boolean processInteract(Player'),
 (r'\bvoid setDead\(\)', 'void legacySetDead()'),
 (r'\bvoid onImpact\(HitResult', 'void onImpact(HitResult'),
 (r'\.getThrower\(\)', '.getOwner()'), (r'\.getBlockHardness\(', '.getDestroySpeed('), (r'\.getExplosionResistance\((\w+)\)', '.getExplosionResistance()'),
 (r'(\w+)\.getItem\(\)\.isShield\(\1, \w+\)', r'\1.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK)'),
]

MOTION_ASSIGN = re.compile(r'((?:this\.)?[\w]+(?:\.[\w]+)*(?:\(\))?(?:\.[\w]+(?:\(\))?)*)\.motion([XYZ])\s*([+\-*/]?)=\s*([^;]+);')
ROT_ASSIGN = re.compile(r'((?:this\.)?[\w]+(?:\.[\w]+)*(?:\(\))?)\.rotation(Yaw|Pitch)\s*([+\-*/]?)=\s*([^;]+);')
POS_ASSIGN = re.compile(r'((?:this\.)?[\w]+(?:\.[\w]+)*(?:\(\))?)\.pos([XYZ])\s*([+\-*/]?)=\s*([^;]+);')

def declared(src, name):
    return re.search(r'\b(int|double|float|long|short|byte|boolean|var|Integer|Double)\s+' + name + r'\b', src) is not None

def motion(src):
    ops = {'': 'set', '+': 'add', '-': 'sub', '*': 'mul', '/': 'div'}
    if not any(declared(src, 'motion' + a) for a in 'XYZ'):
        src = MOTION_ASSIGN.sub(lambda m: 'LycMotion.%s%s(%s, %s);' % (ops[m.group(3)], m.group(2), m.group(1), m.group(4).strip()), src)
        src = re.sub(r'(?<![\w.])motion([XYZ])\s*([+\-*/]?)=\s*([^;]+);', lambda m: 'LycMotion.%s%s(this, %s);' % (ops[m.group(2)], m.group(1), m.group(3).strip()), src)
        src = re.sub(r'\.motion([XYZ])\b', lambda m: '.getDeltaMovement().' + m.group(1).lower(), src)
        src = re.sub(r'(?<![\w.])motion([XYZ])\b', lambda m: 'this.getDeltaMovement().' + m.group(1).lower(), src)
    if not any(declared(src, 'rotation' + a) for a in ('Yaw', 'Pitch')):
        def rot(m):
            recv, which, op, val = m.groups()
            get = 'getYRot' if which == 'Yaw' else 'getXRot'
            sett = 'setYRot' if which == 'Yaw' else 'setXRot'
            v = val.strip() if not op else '%s.%s() %s (%s)' % (recv, get, op, val.strip())
            return '%s.%s(%s);' % (recv, sett, v)
        src = ROT_ASSIGN.sub(rot, src)
        src = re.sub(r'(?<![\w.])rotation(Yaw|Pitch)\s*([+\-*/]?)=\s*([^;]+);', lambda m: 'this.%s(%s);' % ('setYRot' if m.group(1) == 'Yaw' else 'setXRot', m.group(3).strip() if not m.group(2) else 'this.%s() %s (%s)' % ('getYRot' if m.group(1) == 'Yaw' else 'getXRot', m.group(2), m.group(3).strip())), src)
        src = re.sub(r'\.rotationYaw\b', '.getYRot()', src)
        src = re.sub(r'\.rotationPitch\b', '.getXRot()', src)
        src = re.sub(r'(?<![\w.])rotationYaw\b', 'this.getYRot()', src)
        src = re.sub(r'(?<![\w.])rotationPitch\b', 'this.getXRot()', src)
    if not any(declared(src, 'pos' + a) for a in 'XYZ'):
        src = POS_ASSIGN.sub(lambda m: 'LycMotion.%sPos%s(%s, %s);' % (ops[m.group(3)], m.group(2), m.group(1), m.group(4).strip()), src)
        src = re.sub(r'(?<![\w.])pos([XYZ])\b', lambda m: 'this.get' + m.group(1) + '()', src)
    return src

def port(src, path):
    pkg = re.search(r'^package ([\w.]+);', src, re.M).group(1)
    imports = re.findall(r'^import (static )?([\w.]+)(\.\*)?;\n', src, re.M)
    keep = []
    for st, name, star in imports:
        if name.startswith('net.minecraft') or name.startswith('io.netty') or name in ('com.google.common.base.Optional', 'javax.annotation.Nullable'):
            continue
        if name == 'com.lycanitesmobs.client.AssetManager':
            name = 'com.lycanitesmobs.core.AssetManager'
        elif name == 'com.lycanitesmobs.client.localisation.LanguageManager':
            name = 'com.lycanitesmobs.core.LanguageManager'
        elif name.startswith('com.lycanitesmobs.client.') or name.startswith('org.lwjgl'):
            continue
        keep.append('import %s%s%s;' % (st, name, star))
    body = re.sub(r'^import [^\n]*\n', '', src, flags=re.M)
    body = re.sub(r'^package [^\n]*\n', '', body, count=1)
    for a, b in PRE:
        if 'onUpdate' in a and ('Extended' in path or 'mobevent' in path):
            continue
        body = re.sub(a, b, body)
    for a, b in TYPES:
        body = re.sub(r'(?<![\w."])' + a + r'(?=[\s<>\[\]),.;:(&|]|$)', b, body)
    body = motion(body)
    for meth, tpl in RECV:
        body = recv_rewrite(body, meth, tpl)
    for a, b in POST:
        if 'onUpdate' in a and ('Extended' in path or 'mobevent' in path):
            continue
        body = re.sub(a, b, body)
    posdecl = any(declared(body, 'pos' + a) for a in 'XYZ')
    for a, b in R:
        if posdecl and 'posX' in a:
            continue
        body = re.sub(a, b, body)
    wild = [w for w in WILDCARDS if not (w == 'net.minecraft.world.level.pathfinder' and 'java.nio.file' in src)]
    imp = ['import %s.*;' % w for w in wild] + ['import %s;' % e for e in EXPLICIT] + [
        'import io.netty.buffer.ByteBuf;', 'import javax.annotation.Nullable;', 'import java.util.Optional;',
        'import net.minecraft.world.level.GameRules;', 'import net.minecraft.tags.DamageTypeTags;', 'import net.minecraft.core.particles.ParticleTypes;',
        'import net.minecraft.core.particles.DustParticleOptions;', 'import net.minecraft.world.damagesource.DamageTypes;',
        'import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;', 'import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;', 'import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;']
    # explicit lycanites imports must win over the wildcard imports, so wildcard-import clashes are avoided by explicit 1.21 imports only
    out = 'package %s;\n\n%s\n%s\n%s' % (pkg, '\n'.join(sorted(set(imp))), '\n'.join(keep), body)
    out = out.replace('\t', '   ')
    return out

MANUAL = set(l.strip() for l in open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'port_lyc_manual.txt')) if l.strip() and not l.startswith('#'))

def main():
    root = os.path.join(SRC, 'com/lycanitesmobs')
    n = 0
    for dp, dn, fn in os.walk(root):
        for f in fn:
            if not f.endswith('.java'):
                continue
            p = os.path.join(dp, f)
            rel = os.path.relpath(p, SRC)
            if any(rel.startswith(s) for s in SKIP_DIRS):
                continue
            if ONLY and not any(rel.startswith(o) or o in rel for o in ONLY):
                continue
            out = os.path.join(DST, rel)
            if os.path.exists(out) and (not FORCE or rel in MANUAL):
                continue
            os.makedirs(os.path.dirname(out), exist_ok=True)
            open(out, 'w').write(port(open(p).read(), rel))
            n += 1
    print('wrote', n, 'files')

main()
