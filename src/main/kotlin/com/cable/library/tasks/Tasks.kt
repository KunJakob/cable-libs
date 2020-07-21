package com.cable.library.tasks

/**
 * Simple shortcut to the Task builder for simple, delayed, synchronous actions.
 */
public fun after(seconds: Number, action: () -> Unit): Any = Task.builder()
        .delay((seconds.toDouble()/20.0).toLong())
        .execute(action)
        .build()