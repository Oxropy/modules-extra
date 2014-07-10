package de.cubeisland.engine.module.yougottacatchemall

import org.bukkit.FireworkEffect.Type
import org.bukkit.{Color, FireworkEffect, Material}
import org.bukkit.Material.EGG
import org.bukkit.entity.{Firework, EntityType, Egg, Player}
import org.bukkit.event.entity.{EntityDamageByEntityEvent, ProjectileLaunchEvent}
import org.bukkit.event.player.PlayerEggThrowEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.material.SpawnEgg

import scala.collection.mutable


class EggListener extends Listener {

    val eggs = new mutable.HashMap[Player, mutable.Set[Egg]] with mutable.MultiMap[Player, Egg]
    val eggTypes = new mutable.HashMap[Egg, EggType]
    val preventHatching = new mutable.HashSet[Egg]()
    
    @EventHandler
    def onEntityDamage(event: EntityDamageByEntityEvent) = {
        event.getDamager match {
            case egg: Egg =>
                egg.getShooter match {
                    case player: Player =>
                        if (eggs.entryExists(player, _ .equals(egg))){
                            eggs.removeBinding(player, egg)
                            eggTypes.remove(egg) match {
                                case Some(eggType) =>
                                    val item = new ItemStack(Material.MONSTER_EGG, 1)
                                    val data = item.getData.asInstanceOf[SpawnEgg]
                                    data.setSpawnedType(event.getEntity.getType)
                                    item.setData(data)
                                    item.setDurability(data.getData) // because setting MaterialData doesn't work :(
                                    val loc = event.getEntity.getLocation
                                    loc.getWorld.dropItemNaturally(loc, item)
                                    event.getEntity.remove()

                                    val fw = loc.getWorld.spawnEntity(loc, EntityType.FIREWORK).asInstanceOf[Firework]
                                    val fwMeta = fw.getFireworkMeta
                                    fwMeta.clearEffects()
                                    fwMeta.addEffect(FireworkEffect.builder().`with`(Type.BALL).withColor(Color.WHITE).build())
                                    fw.setFireworkMeta(fwMeta)
                                    fw.detonate()

                                case None =>
                            }
                        }
                }
        }
    }
    
    

    @EventHandler
    def onEggThrow(event: ProjectileLaunchEvent) = {
        event.getEntity match {
            case egg: Egg =>
                egg.getShooter match {
                    case player: Player =>
                        val eggItem = player.getItemInHand
                        if (eggItem.getType == EGG){
                            // TODO special Eggs
                            eggs.addBinding(player, egg)
                            eggTypes.put(egg, EggType.NORMAL)
                            preventHatching.add(egg)
                        }
                }
                
        }
    }
    
    @EventHandler
    def onEggHatch(event: PlayerEggThrowEvent){
        if (preventHatching contains event.getEgg){
            preventHatching.remove(event.getEgg)
            event.setHatching(false)
        }
    }
}
