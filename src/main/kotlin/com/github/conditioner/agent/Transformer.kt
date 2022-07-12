package com.github.conditioner.agent

import com.github.conditioner.agent.mapping.DeobfuscationRemapper
import com.github.conditioner.agent.util.BytecodeManager
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.RemappingClassAdapter
import org.objectweb.asm.tree.ClassNode
import java.io.InputStream
import java.lang.instrument.ClassDefinition
import java.lang.instrument.ClassFileTransformer
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.security.ProtectionDomain

class Transformer : ClassFileTransformer {

    @Suppress("UNCHECKED_CAST")
    override fun transform(
        loader: ClassLoader?,
        className: String?,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray?
    ): ByteArray? {
        if (classBeingRedefined == null) {
            return null
        }
        try {
            for (annotation in classBeingRedefined.annotations) {
                if (annotation.annotationClass.simpleName!!.endsWith("HotReloadable")) {
                    val memoryClassLoader = MemoryClassLoader(classfileBuffer!!, className!!.replace("/", "."))
                    val clazz: Class<*> = memoryClassLoader.load()
                    val getClassNameMethod: MethodHandle = MethodHandles.lookup().findVirtual(clazz, "getClassName", MethodType.methodType(Array<String>::class.java))
                    val transformMethod: MethodHandle = MethodHandles.lookup().findVirtual(clazz, "transform", MethodType.methodType(Void.TYPE, ClassNode::class.java, String::class.java))
                    val transformerInstance: Any = clazz.newInstance()

                    for (mcClassName: String in getClassNameMethod.invoke(transformerInstance) as ArrayList<String>) {
                        val stream: InputStream = ClassLoader.getSystemResourceAsStream(mcClassName.replace(".", "/") + ".class") as InputStream
                        val streamBuffer: ByteArray = IOUtils.toByteArray(stream)
                        val cr = ClassReader(streamBuffer)
                        val cn = ClassNode()

                        cr.accept(cn, 8)
                        transformMethod.invoke(transformerInstance, cn, mcClassName)
                        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
                        val remapClass = Class.forName("net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper", false, loader)
                        val remapInstance: Any = remapClass.getField("INSTANCE").get(null)

                        cn.accept(RemappingClassAdapter(cw, DeobfuscationRemapper(remapInstance)))
                        val classBuffer: ByteArray = cw.toByteArray()

                        BytecodeManager.writeCodeDump(classBuffer, mcClassName)
                        BytecodeManager.instrumentation!!.redefineClasses(ClassDefinition(Class.forName(mcClassName, false, loader), classBuffer))
                    }
                }
            }
        } catch (t: Throwable) {
            val sb = StringBuilder()

            sb.append("Error occurred during during Forge HotReloading:\n").append(t.javaClass).append(": ").append(t.message).append("\n")

            for (ste in t.stackTrace) {
                sb.append(ste.toString()).append("\n")
            }
            println(sb.toString())
        }
        return null
    }
}