package com.github.conditioner.agent

import com.github.conditioner.agent.util.BytecodeManager
import java.lang.instrument.Instrumentation

fun premain(arg: String, instrumentation: Instrumentation) {
    BytecodeManager.instrumentation = instrumentation
    instrumentation.addTransformer(Transformer(), true)
}