/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.primitive.Clearable
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.reflection.generic.GenericUtil
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.Parentable
import de.bixilon.minosoft.data.registries.registries.registry.Registry
import java.lang.reflect.Field

object RegistriesUtil {
    private val clearable: MutableList<Field> = mutableListOf()
    private val parentable: MutableList<Field> = mutableListOf()
    private val types: MutableMap<Class<*>, Field> = mutableMapOf()

    init {
        for (field in Registries::class.java.declaredFields) {
            field.isAccessible = true
            if (Clearable::class.java.isAssignableFrom(field.type)) {
                clearable += field
            }
            if (Parentable::class.java.isAssignableFrom(field.type) && field.name != Parentable<*>::parent.name) {
                parentable += field
            }
            if (Registry::class.java.isAssignableFrom(field.type)) {
                putRegistry(field)
            }
        }
    }

    private fun putRegistry(field: Field) {
        var clazz = field.genericType

        if (field.type != Registry::class.java) {
            var type = field.type
            while (type != Object::class.java) {
                if (type.superclass == Registry::class.java) {
                    clazz = type.genericSuperclass
                    break
                }
                type = type.superclass
            }
        }

        types[GenericUtil.getGeneric(clazz)] = field
    }


    fun getRegistry(registries: Registries, type: Class<*>): Registry<*>? {
        var field: Field?
        var clazz: Class<*> = type

        do {
            field = types[clazz]
            clazz = clazz.superclass
        } while (field == null && clazz != Any::class.java)

        return field?.get(registries).unsafeCast()
    }

    fun Registries.postInit(latch: CountUpAndDownLatch) {
        val worker = UnconditionalWorker()
        for (field in types.values) {
            worker += { field.get(this).unsafeCast<Registry<*>>().postInit(this) }
        }
        worker.work(latch)
    }


    fun Registries.clear() {
        for (field in clearable) {
            val value = field.get(this)?.unsafeCast<Clearable>() ?: continue
            value.clear()
        }
    }

    fun Registries.setParent(parent: Registries?) {
        for (field in parentable) {
            val value = field.get(this)?.unsafeCast<Parentable<Any?>>() ?: continue
            value.parent = parent?.let { field.get(it) }
        }
    }
}
