package de.cubeisland.engine.module.yougottacatchemall

import de.cubeisland.engine.core.module.Module

class Yougottacatchemall extends Module {
    override def onEnable() {
        System.out.println("\nI wanna be the very best\nLike no one ever was.\nTo catch them is my real test,\nTo train them is my cause.")
        this.getCore.getEventManager.registerListener(this, new EggListener)
    }
}
