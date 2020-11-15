package org.http4k.graphql

import com.expediagroup.graphql.SchemaGeneratorConfig
import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.toSchema
import graphql.ExecutionInput
import graphql.GraphQL
import org.dataloader.DataLoaderRegistry
import org.http4k.graphql.schema.BookQueryService
import org.http4k.graphql.schema.CourseQueryService
import org.http4k.graphql.schema.HelloQueryService
import org.http4k.graphql.schema.LoginMutationService
import org.http4k.graphql.schema.UniversityQueryService
import org.http4k.graphql.schema.models.BATCH_BOOK_LOADER_NAME
import org.http4k.graphql.schema.models.COURSE_LOADER_NAME
import org.http4k.graphql.schema.models.UNIVERSITY_LOADER_NAME
import org.http4k.graphql.schema.models.batchBookLoader
import org.http4k.graphql.schema.models.batchCourseLoader
import org.http4k.graphql.schema.models.batchUniversityLoader

class MySchemaHandler() : GraphQLWithContextHandler<String> {
    private val graphQL = GraphQL.newGraphQL(
        toSchema(
            SchemaGeneratorConfig(supportedPackages = listOf("org.http4k.graphql.schema")),
            listOf(
                HelloQueryService(),
                BookQueryService(),
                CourseQueryService(),
                UniversityQueryService()
            ).asTopLevelObject(),
            listOf(LoginMutationService()).asTopLevelObject()
        )).build()

    private val dataLoaderRegistry = DataLoaderRegistry().apply {
        register(UNIVERSITY_LOADER_NAME, batchUniversityLoader)
        register(COURSE_LOADER_NAME, batchCourseLoader)
        register(BATCH_BOOK_LOADER_NAME, batchBookLoader)
    }

    override fun invoke(payload: GraphQLRequest, context: String) = GraphQLResponse.from(
        graphQL.execute(
            ExecutionInput.Builder()
                .query(payload.query)
                .variables(payload.variables)
                .dataLoaderRegistry(dataLoaderRegistry)
                .context(context)
        ))
}

private fun List<Any>.asTopLevelObject() = map(::TopLevelObject)
