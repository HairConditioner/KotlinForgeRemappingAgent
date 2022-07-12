package com.github.conditioner.agent.mapping

import org.objectweb.asm.commons.Remapper
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class DeobfuscationRemapper(private val remapper: Any) : Remapper() {

    private var mapFieldName: MethodHandle? = null

    private var mapMethodName: MethodHandle? = null

    init {
        mapFieldName = MethodHandles.lookup().findVirtual(remapper::class.java, "mapFieldName", MethodType.methodType(String::class.java, String::class.java, String::class.java, String::class.java))
        mapMethodName = MethodHandles.lookup().findVirtual(remapper::class.java, "mapMethodName", MethodType.methodType(String::class.java, String::class.java, String::class.java, String::class.java))
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String? {
        return try {
            mapFieldName!!.invoke(remapper, owner, name, descriptor) as String?
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    override fun mapMethodName(owner: String?, name: String?, descriptor: String?): String? {
        return try {
            mapMethodName!!.invoke(remapper, owner, name, descriptor) as String?
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }
}