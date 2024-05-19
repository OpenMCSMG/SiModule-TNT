package cn.cyanbukkit.putfunname

import cn.cyanbukkit.putfunname.tnteffect.BoomEffectOfSand
import cn.cyanbukkit.putfunname.utils.Mode

class GameHandle {

    @Mode("1")
    fun spawnSand(world: String,x: Int, y: Int, z: Int, up: Int, sandAmount: Int) {
        BoomEffectOfSand.spawnSand(world, x, y, z, up, sandAmount)
    }

}