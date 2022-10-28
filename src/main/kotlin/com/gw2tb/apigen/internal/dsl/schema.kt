/*
 * Copyright (c) 2019-2022 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
@file:OptIn(LowLevelApiGenApi::class)
@file:Suppress("FunctionName")
package com.gw2tb.apigen.internal.dsl

import com.gw2tb.apigen.internal.impl.*
import com.gw2tb.apigen.ir.*
import com.gw2tb.apigen.ir.model.IRAPIType
import com.gw2tb.apigen.model.*
import com.gw2tb.apigen.model.v2.*
import com.gw2tb.apigen.schema.*

internal fun Alias(type: DeferredPrimitiveType, name: String): DeferredPrimitiveType =
    Alias(type, Name.deriveFromTitleCase(name))

internal fun Alias(type: DeferredPrimitiveType, name: Name): DeferredPrimitiveType {
    return type // TODO impl
}

/** Alias for [SchemaBitfield] */
internal val BITFIELD: DeferredPrimitiveType = DeferredPrimitiveType(IRBitfield)

/** Alias for [SchemaBoolean]. */
internal val BOOLEAN: DeferredPrimitiveType = DeferredPrimitiveType(IRBoolean)

/** Alias for [SchemaDecimal]. */
internal val DECIMAL: DeferredPrimitiveType = DeferredPrimitiveType(IRDecimal)

/** Alias for [SchemaInteger]. */
internal val INTEGER: DeferredPrimitiveType = DeferredPrimitiveType(IRInteger)

/** Alias for [SchemaString]. */
internal val STRING: DeferredPrimitiveType = DeferredPrimitiveType(IRString)

internal fun <T : IRTypeUse<*>> DeferredSchemaType(
    factory: (ScopedTypeRegistry<*>?, Boolean) -> SchemaVersionedData<out T>
): DeferredSchemaType<T> = object : DeferredSchemaType<T>() {

    override fun get(
        typeRegistry: ScopedTypeRegistry<*>?,
        interpretationHint: APIType.InterpretationHint?,
        isTopLevel: Boolean
    ): SchemaVersionedData<out T> = factory(typeRegistry, isTopLevel)

}

internal abstract class DeferredSchemaType<T : IRTypeUse<*>> {

    abstract fun get(
        typeRegistry: ScopedTypeRegistry<*>?,
        interpretationHint: APIType.InterpretationHint?,
        isTopLevel: Boolean = false
    ): SchemaVersionedData<out T>

    fun getFlat(): T =
        get(typeRegistry = null, interpretationHint = null).single().data

}

internal data class DeferredPrimitiveType(
    private val value: IRPrimitive,
) : DeferredSchemaType<IRPrimitive>() {

    override fun get(
        typeRegistry: ScopedTypeRegistry<*>?,
        interpretationHint: APIType.InterpretationHint?,
        isTopLevel: Boolean
    ): SchemaVersionedData<out IRPrimitive> {
        return wrapVersionedSchemaData(value)
    }

}

@APIGenDSL
internal abstract class DeferredSchemaClass<T : IRAPIType> : DeferredSchemaType<IRTypeReference>() {

    abstract val name: Name

    abstract val apiTypeFactory: (SchemaVersionedData<out IRTypeDeclaration<*>>, APIType.InterpretationHint?, Boolean) -> T

    abstract val typeRegistry: ScopedTypeRegistry<T>?

    open val nestedTypeRegistry: ScopedTypeRegistry<T>? get() = typeRegistry?.nestedScope(name)

    fun array(
        items: DeferredSchemaType<out IRTypeUse<*>>,
        nullableItems: Boolean = false
    ): DeferredSchemaType<IRArray> =
        DeferredSchemaType { typeRegistry, isTopLevel ->
            items.get(typeRegistry, interpretationHint = null, isTopLevel).mapData { IRArray(it, nullableItems, description = null) }
        }

    fun map(
        keys: DeferredSchemaType<out IRPrimitive>,
        values: DeferredSchemaType<out IRTypeUse<*>>,
        nullableValues: Boolean = false
    ): DeferredSchemaType<IRMap> =
        DeferredSchemaType { typeRegistry, isTopLevel ->
            values.get(typeRegistry, interpretationHint = null, isTopLevel).mapData { IRMap(keys.getFlat(), it, nullableValues, description = null) }
        }


    fun conditional(
        name: String,
        description: String,
        disambiguationBy: String = "type",
        disambiguationBySideProperty: Boolean = false,
        interpretationInNestedProperty: Boolean = false,
        sharedConfigure: (AbstractSchemaRecordBuilder<T>.() -> Unit)? = null,
        block: SchemaConditionalBuilder<T>.() -> Unit
    ): DeferredSchemaClass<T> =
        conditional(Name.deriveFromTitleCase(name), description, disambiguationBy, disambiguationBySideProperty, interpretationInNestedProperty, sharedConfigure, block)

    fun conditional(
        name: Name,
        description: String,
        disambiguationBy: String = "type",
        disambiguationBySideProperty: Boolean = false,
        interpretationInNestedProperty: Boolean = false,
        sharedConfigure: (AbstractSchemaRecordBuilder<T>.() -> Unit)? = null,
        block: SchemaConditionalBuilder<T>.() -> Unit
    ): DeferredSchemaClass<T> =
        SchemaConditionalBuilder(
            name, description, disambiguationBy, disambiguationBySideProperty, interpretationInNestedProperty,
            sharedConfigure, apiTypeFactory, nestedTypeRegistry
        ).also(block)


    fun enum(type: DeferredPrimitiveType, name: String, description: String, block: SchemaEnumBuilder<T>.() -> Unit): DeferredSchemaClass<T> =
        enum(type, Name.deriveFromTitleCase(name), description, block)

    fun enum(type: DeferredPrimitiveType, name: Name, description: String, block: SchemaEnumBuilder<T>.() -> Unit): DeferredSchemaClass<T> =
        SchemaEnumBuilder(type, name, description, apiTypeFactory, typeRegistry).also(block)


    fun record(name: String, description: String, block: SchemaRecordBuilder<T>.() -> Unit): DeferredSchemaClass<T> =
        record(Name.deriveFromTitleCase(name), description, block)

    fun record(name: Name, description: String, block: SchemaRecordBuilder<T>.() -> Unit): DeferredSchemaClass<T> =
        SchemaRecordBuilder(name, description, apiTypeFactory, nestedTypeRegistry).also(block)


    fun tuple(name: String, description: String, block: SchemaTupleBuilder<T>.() -> Unit): DeferredSchemaClass<T> =
        tuple(Name.deriveFromTitleCase(name), description, block)

    fun tuple(name: Name, description: String, block: SchemaTupleBuilder<T>.() -> Unit): DeferredSchemaClass<T> =
        SchemaTupleBuilder(name, description, apiTypeFactory, nestedTypeRegistry).also(block)

}

internal abstract class AbstractSchemaRecordBuilder<T : IRAPIType> : DeferredSchemaClass<T>() {

    private val _properties = mutableListOf<SchemaRecordPropertyBuilder>()

    fun buildProperties(typeRegistry: ScopedTypeRegistry<T>?): SchemaVersionedData<Map<String, IRProperty>>? =
        if (_properties.isEmpty()) {
            null
        } else {
            buildVersionedSchemaData {
                V2SchemaVersion.values().forEach { version ->
                    if (_properties.any { it.hasChangedInVersion(typeRegistry, version) }) {
                        val relevantProperties = _properties.getForVersion(
                            SchemaRecordPropertyBuilder::since,
                            SchemaRecordPropertyBuilder::until,
                            version
                        )

                        add(relevantProperties.map { it.get(typeRegistry, version) }.associateBy { it.serialName }, since = version)
                    }
                }
            }
        }

    operator fun String.invoke(type: DeferredSchemaType<out IRTypeUse<*>>, description: String): SchemaRecordPropertyBuilder =
        SchemaRecordPropertyBuilder(this, type, description).also { _properties += it }

    /** Marks an optional property whose presents is mandated by the given `scope`. */
    fun optional(scope: TokenScope): IPropertyModifier = object : IPropertyModifier {
        override fun applyTo(property: SchemaRecordPropertyBuilder) {
            property.optionality = Optionality.MANDATED(scope)
        }
    }

    /** The minimal [V2SchemaVersion] (inclusive) required for the property. */
    fun since(version: V2SchemaVersion): IPropertyModifier = object : IPropertyModifier {
        override fun applyTo(property: SchemaRecordPropertyBuilder) {
            property.since = version
        }
    }

    /** The maximum [V2SchemaVersion] (exclusive) required for the property. */
    fun until(version: V2SchemaVersion): IPropertyModifier = object : IPropertyModifier {
        override fun applyTo(property: SchemaRecordPropertyBuilder) {
            property.until = version
        }
    }

    /** Explicitly specifies the _camelCase_ name for the property. */
    @Suppress("FunctionName")
    fun CamelCase(value: String): IPropertyModifier = object : IPropertyModifier {
        override fun applyTo(property: SchemaRecordPropertyBuilder) {
            property.camelCase = value
        }
    }

    /** Explicitly specifies the serial name for the property. */
    @Suppress("FunctionName")
    fun SerialName(value: String): IPropertyModifier = object : IPropertyModifier {
        override fun applyTo(property: SchemaRecordPropertyBuilder) {
            property.serialName = value
        }
    }

}

internal class SchemaConditionalSharedPropertyBuilder<T : IRAPIType>(
    override val apiTypeFactory: (SchemaVersionedData<out IRTypeDeclaration<*>>, APIType.InterpretationHint?, Boolean) -> T,
    override val typeRegistry: ScopedTypeRegistry<T>?
) : AbstractSchemaRecordBuilder<T>() {

    override val nestedTypeRegistry: ScopedTypeRegistry<T>?
        get() = typeRegistry

    override val name: Name
        get() = error("Not implemented")

    override fun get(
        typeRegistry: ScopedTypeRegistry<*>?,
        interpretationHint: APIType.InterpretationHint?,
        isTopLevel: Boolean
    ): SchemaVersionedData<out IRTypeReference> {
        error("Not implemented")
    }

}