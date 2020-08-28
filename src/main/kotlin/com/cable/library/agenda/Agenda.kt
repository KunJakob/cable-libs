package com.cable.library.agenda

public val scheduled: MutableMap<Class<*>, MutableList<Runnable>> = hashMapOf()

public inline fun <reified T> addAgenda(crossinline action: () -> Unit) {
    if (!scheduled.containsKey(T::class.java)) {
        scheduled[T::class.java] = arrayListOf( Runnable { action() })
    } else {
        scheduled[T::class.java]?.add( Runnable { action() } )
    }
}

public inline fun <reified T> runAgenda(): Unit? = scheduled[T::class.java]?.forEach { it.run() }