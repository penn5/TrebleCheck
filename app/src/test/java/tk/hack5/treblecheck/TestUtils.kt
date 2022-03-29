/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2022 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package tk.hack5.treblecheck

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

internal fun mockField(klass: KClass<*>, fieldName: String, value: Any?, block: () -> Unit) {
    val field = klass.java.getField(fieldName)
    field.isAccessible = true
    Field::class.java.getDeclaredField("modifiers").also {
        it.isAccessible = true
        it.set(field, field.modifiers and Modifier.FINAL.inv())
    }
    val oldValue = field.get(null)
    field.set(null, value)
    block()
    field.set(null, oldValue)
}