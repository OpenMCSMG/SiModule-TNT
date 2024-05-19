package cn.cyanbukkit.putfunname.tnteffect

import cn.cyanbukkit.tntaddon.cyanlib.launcher.CyanPluginLauncher.cyanPlugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.math.*

object BoomEffectOfSand : Listener {
    fun spawnSand(world: String, x: Int, y: Int, z: Int, up: Int, sandAmount: Int) {
        val loc = Location(Bukkit.getWorld(world), x.toDouble(), y.toDouble(), z.toDouble())
        val tnt = loc.world!!.spawn(loc, TNTPrimed::class.java)
        tnt.fuseTicks = 50 * 20
        tnt.setMetadata("sand", FixedMetadataValue(cyanPlugin, sandAmount))
        runUp(tnt, up).runTaskTimer(cyanPlugin, 0, 1)
    }

    @EventHandler
    fun onTNTExplode(event: org.bukkit.event.entity.EntityExplodeEvent) {
        val entity = event.entity
        if (entity is TNTPrimed) {
            if (entity.hasMetadata("sand")) {
                val sandAmount = entity.getMetadata("sand")[0].value() as Int
                // 在爆炸处位置放置对应数量fallblock然后在爆炸处位置弄个击退把这些沙子炸开弄出一个爆炸效果
                val loc = entity.location
                val world = loc.world
                val random = Random()
                for (i in 0 until sandAmount) {
                    val fallBlock = world!!.spawnFallingBlock(loc, org.bukkit.Material.SAND.createBlockData())
                    fallBlock.setMetadata("sand", FixedMetadataValue(cyanPlugin, true))
                    // Generate a random direction for the velocity
                    val theta = 2.0 * Math.PI * random.nextDouble()
                    val phi = acos(2.0 * random.nextDouble() - 1.0)
                    val x = sin(phi) * cos(theta)
                    val y = sin(phi) * sin(theta)
                    val z = cos(phi)
                    // Create the velocity vector and set it to the fallBlock
                    val velocity = org.bukkit.util.Vector(x, y, z)
                    fallBlock.velocity = velocity.normalize().multiply(1.0)
                }
            }
        }
    }


    @EventHandler
    fun onFallBlockLand(event: org.bukkit.event.entity.EntityChangeBlockEvent) {
        val entity = event.entity
        if (entity.hasMetadata("sand")) {
            entity.remove()
            event.isCancelled = true
        }
    }


    private fun runUp(tnt: TNTPrimed, up: Int): BukkitRunnable {
        val targetLocation = tnt.location.clone().add(0.0, up.toDouble(), 0.0)
        return object : BukkitRunnable() {
            override fun run() {
                val spawnLocation = tnt.location
                if (spawnLocation.y >= targetLocation.y) {
                    tnt.setGravity(false)
                    tnt.fuseTicks = 0
                    this.cancel()
                    return
                }
                val direction = targetLocation.toVector().subtract(spawnLocation.toVector()).normalize()
                tnt.velocity = direction.multiply(4.0) // Multiply by 4.0 to double the current speed
                val newLocation = spawnLocation.clone().add(direction.clone().multiply(1.0 / 10.0))
                val yaw = Math.toDegrees(atan2(direction.z, direction.x)).toFloat() - 90
                var pitch = Math.toDegrees(asin(direction.y)).toFloat()
                if (pitch.isNaN()) {
                    pitch = if (direction.y > 0) 90.0f else -90.0f
                }
                newLocation.yaw = yaw
                newLocation.pitch = pitch
                tnt.teleport(newLocation)
            }
        }
    }

}