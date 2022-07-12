package com.github.conditioner.agent.mapping

import org.objectweb.asm.commons.Remapper

class TransformerRemapper(private val transformerName: String, private val count: Int) : Remapper() {

    override fun map(internalName: String?): String {
        if (internalName == transformerName) {
            return internalName + count
        }
        return super.map(internalName)
    }
}