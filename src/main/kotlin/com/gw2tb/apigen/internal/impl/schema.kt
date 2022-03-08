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
package com.gw2tb.apigen.internal.impl

import com.gw2tb.apigen.internal.dsl.*
import com.gw2tb.apigen.model.*
import com.gw2tb.apigen.schema.*

internal fun <T : APIType> conditionalImpl(
    name: String,
    description: String,
    disambiguationBy: String,
    disambiguationBySideProperty: Boolean,
    interpretationInNestedProperty: Boolean,
    sharedConfigure: (SchemaRecordBuilder<T>.() -> Unit)?,
    apiTypeFactory: (SchemaVersionedData<out SchemaTypeDeclaration>) -> T,
    configure: SchemaConditionalBuilder<T>.() -> Unit
): DeferredSchemaClass<T> {
    return SchemaConditionalBuilder(
        name = name,
        description = description,
        disambiguationBy,
        disambiguationBySideProperty,
        interpretationInNestedProperty,
        sharedConfigure,
        apiTypeFactory
    ).also(configure)
}

internal fun <T : APIType> recordImpl(
    name: String,
    description: String,
    apiTypeFactory: (SchemaVersionedData<out SchemaTypeDeclaration>) -> T,
    configure: SchemaRecordBuilder<T>.() -> Unit
): DeferredSchemaClass<T> {
    return SchemaRecordBuilder(
        name = name,
        description = description,
        apiTypeFactory
    ).also(configure)
}