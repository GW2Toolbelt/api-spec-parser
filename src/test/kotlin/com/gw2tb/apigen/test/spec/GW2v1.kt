/*
 * Copyright (c) 2019-2021 Leon Linhart
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
package com.gw2tb.apigen.test.spec

import com.gw2tb.apigen.*
import com.gw2tb.apigen.internal.dsl.*
import com.gw2tb.apigen.model.*
import com.gw2tb.apigen.test.*
import kotlinx.serialization.json.*
import kotlin.time.*

class GW2v1 : SpecTest<APIQuery.V1, APIType.V1, GW2v1.ExpectedAPIv1Query>(
    "GW2v1",
    APIVersion.API_V1,
    V1SpecBuilder {
        expectQuery(
            "/SkinDetails",
            isLocalized = true,
            queryParameters = listOf(ExpectedQueryParameter("skin_id", INTEGER))
        )

        expectQuery("/Skins")
    }
) {

    override fun assertProperties(expected: ExpectedAPIv1Query, actual: APIQuery.V1) {}

    override fun assertSchema(expected: ExpectedAPIv1Query, actual: APIQuery.V1) {
        val schema = actual.schema
        val data = TestData[spec, actual.route]

        val element = Json.parseToJsonElement(data)
        schema.assertMatches(element)
    }

    data class ExpectedAPIv1Query(
        override val route: String,
        override val isLocalized: Boolean,
        override val cache: Duration?,
        override val pathParameters: List<ExpectedPathParameter>,
        override val queryParameters: List<ExpectedQueryParameter>
    ) : ExpectedAPIQuery

    class V1SpecBuilder {

        private val queries = mutableListOf<ExpectedAPIv1Query>()

        companion object {
            operator fun invoke(block: V1SpecBuilder.() -> Unit): List<ExpectedAPIv1Query> {
                return V1SpecBuilder().also(block).queries
            }
        }

        fun expectQuery(
            route: String,
            isLocalized: Boolean = false,
            cache: Duration? = null,
            pathParameters: List<ExpectedPathParameter> = emptyList(),
            queryParameters: List<ExpectedQueryParameter> = emptyList()
        ) {
            queries += ExpectedAPIv1Query(
                route,
                isLocalized,
                cache,
                pathParameters,
                queryParameters
            )
        }

    }

}