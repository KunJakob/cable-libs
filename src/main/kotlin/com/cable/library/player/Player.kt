package com.cable.library.player

import net.minecraft.entity.player.EntityPlayerMP

public fun EntityPlayerMP.isInvisibleTo(other: EntityPlayerMP) : Boolean {
    return if (!this.isInvisible) {
        false
    } else if (other.isSpectator) {
        false
    } else {
        val team = this.team
        team == null || other.team !== team || !team.seeFriendlyInvisiblesEnabled
    }
}