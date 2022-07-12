package com.github.conditioner.agent

import com.github.conditioner.agent.mapping.TransformerRemapper
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.RemappingClassAdapter

class MemoryClassLoader(private val buffer: ByteArray, private val className: String) : ClassLoader() {

    private var count: Int = -1

    init {
        count++
    }

    override fun findClass(name: String?): Class<*>? {
        if (name == className) {
            val newBuffer: ByteArray = remapBytes()

            return defineClass(name + count, newBuffer, 0, newBuffer.size)
        }
        return null
    }

    fun load(): Class<*> {
        return findClass(className)!!
    }

    private fun remapBytes(): ByteArray {
        val cr = ClassReader(buffer)
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)

        cr.accept(RemappingClassAdapter(cw, TransformerRemapper(className.replace(".", "/"), count)), 8)
        return cw.toByteArray()
    }
}