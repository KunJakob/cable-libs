package com.cable.library.data

import com.cable.library.CableLibs
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minecraft.entity.player.EntityPlayerMP
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.PrintWriter
import java.util.UUID

public abstract class SimpleData {
    public enum class FileMode {
        FLAT,
        DEEP,
        SINGULAR
    }

    public abstract val fileMode: FileMode
    public abstract val root: String
    public open fun getFileName(): String = name
    public open fun shouldCache(): Boolean = true

    @Transient
    public var name: String = UUID.randomUUID().toString()

    public open fun getGson(): Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    public fun save(uncache: Boolean = true) {
        val file = getFile()
        val gson = getGson()

        PrintWriter(file).use {
            try {
                val json = gson.toJsonTree(this, this::class.java) as JsonObject
                json.remove("fileMode")
                json.remove("root")
                it.print(gson.toJson(json))
                if (uncache) {
                    uncache()
                }
            } catch (e: Exception) {
                CableLibs.logError("There was a problem saving ${file.path}")
                e.printStackTrace()
            }
        }
    }

    public fun <T : SimpleData> load(clazz: Class<T>): T?  {
        val file = getFile()
        if (!file.exists()) {
            return null
        } else {
            try {
                FileReader(file).use {
                    return getGson().fromJson(it, clazz)
                }
            } catch (e: Exception) {
                CableLibs.logError("An error occurred loading ${file.path}")
                e.printStackTrace()
                return null
            }
        }
    }

    public fun delete() {
        val file = getFile()
        if (file.exists()) {
            try {
                file.delete()
            } catch (e: IOException) {
                CableLibs.logError("Error while deleting ${getFile().path}")
                e.printStackTrace()
            }
        }
    }

    public fun getFile(): File {
        if (fileMode == FileMode.SINGULAR) {
            val file = File(root)
            file.parentFile.mkdirs()
            return file
        }

        val folder = if (this.root.endsWith("/")) this.root else (this.root + "/")
        val fileName = getFileName()

        if (fileMode == FileMode.FLAT) {
            val file = File("$folder$fileName.json")
            file.parentFile.mkdirs()
            return file
        } else if (fileMode == FileMode.DEEP) {
            val file = File(folder + fileName.substring(0, 2) + "/" + fileName + ".json")
            file.parentFile.mkdirs()
            return file
        }

        // Won't be reached, I just want to be explicit about the enums
        return File(root)
    }

    public fun uncache() {
        simpleDataCache[javaClass]?.remove(name)
    }
}

public val simpleDataCache: HashMap<Class<*>, HashMap<String, SimpleData>> = hashMapOf()

public inline fun <reified T : SimpleData> getSimpleData(uuid: UUID): T = getSimpleData(uuid.toString())

public inline fun <reified T : SimpleData> getSimpleData(name: String): T {
    val existing = simpleDataCache[T::class.java]?.get(name) as? T
    if (existing != null) {
        return existing
    }

    val empty = T::class.java.newInstance().also { it.name = name }
    val loaded = empty.load(T::class.java)?.also { it.name = name }

    if (loaded != null) {
        return loaded
    } else {
        if (empty.shouldCache()) {
            val cacheMap = simpleDataCache[empty::class.java]
                    ?: hashMapOf<String, SimpleData>().also { simpleDataCache[empty::class.java] = it }

            cacheMap[name] = empty
        }
        return empty
    }
}

public inline fun <reified T : SimpleData> deleteSimpleData(uuid: UUID): Unit = deleteSimpleData<T>(uuid.toString())

public inline fun <reified T : SimpleData> deleteSimpleData(name: String): Unit = T::class.java
        .newInstance()
        .also { it.name = name }
        .delete()

public inline fun <reified T : SimpleData> EntityPlayerMP.getData(): T = getSimpleData(uniqueID)

public inline fun <reified T : SimpleData> EntityPlayerMP.deleteData(): Unit = deleteSimpleData<T>(uniqueID)