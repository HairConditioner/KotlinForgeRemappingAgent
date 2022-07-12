package com.github.conditioner.agent.util

import java.io.File
import java.io.FileOutputStream
import java.lang.instrument.Instrumentation

class BytecodeManager {

    companion object {
        var instrumentation: Instrumentation? = null

        fun writeCodeDump(buffer: ByteArray, name: String) {
            if (System.getProperty("debugBytecode", "false") == "true") {
                val bytecodeDir = File("bytecode")

                val transformedName: String = if (name.contains("$")) {
                    name.replace("$", ".") + ".class"
                } else {
                    "$name.class"
                }
                if (!bytecodeDir.exists()) {
                    bytecodeDir.mkdirs()
                }
                val bytecodeOutput = File(bytecodeDir, transformedName)

                if (!bytecodeOutput.exists()) {
                    bytecodeOutput.createNewFile()
                }
                val os = FileOutputStream(bytecodeOutput)

                os.write(buffer)
            }
        }
    }
}