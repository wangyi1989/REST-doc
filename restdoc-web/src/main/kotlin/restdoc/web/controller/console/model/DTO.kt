package restdoc.web.controller.console.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.omg.CORBA.Object
import org.springframework.data.domain.PageRequest
import restdoc.web.base.getBean
import restdoc.web.model.*
import restdoc.web.util.FieldType
import restdoc.web.util.dp.JsonDeProjector
import java.net.URI

data class CreateProjectDto(val name: String, val desc: String?, val type: ProjectType)

data class UpdateProjectDto(val id: String, val name: String, val desc: String, val type: ProjectType)

data class RequestDto(
        val projectId: String,
        var documentId: String?,
        var executeMode: String?,
        var remoteAddress: String?,
        var queryParams: MutableMap<String, Any>?,
        var url: String,
        val name: String?,
        val description: String?,
        val resource: String?,
        val method: String,
        val headers: List<HeaderDto>?,
        val requestFields: Map<String, Any?>?,
        val responseFields: Map<String, Any?>?,
        val uriFields: List<UriVarFieldDto>?,
        val executeResult: Map<String, Any>? = null) {

    fun lookupPath(): String {
        return if (this.url.contains("http(s)?")) {
            URI(url).rawPath
        } else {
            url
        }
    }

    /**
     *
     */
    fun mapToHeaderDescriptor(): List<HeaderFieldDescriptor> {
        return if (!(headers == null || this.headers.isEmpty())) {
            headers
                    .filter { it.headerKey.isNotBlank() }
                    .map {
                        HeaderFieldDescriptor(
                                field = it.headerKey,
                                value = it.headerValue.split(","),
                                description = it.headerDescription,
                                optional = it.headerConstraint)
                    }
                    // Fields Deduplication
                    .distinctBy { it.field }
        } else mutableListOf()
    }

    /**
     * Deduplication field path
     */
    fun mapToRequestDescriptor(): List<BodyFieldDescriptor> {
        return if (!(requestFields == null || this.requestFields.isEmpty()))
            JsonDeProjector(getBean(ObjectMapper::class.java).convertValue(requestFields, JsonNode::class.java))
                    .deProject()
                    .map {
                        BodyFieldDescriptor(
                                path = it.path.replace(Regex("\\[\\d\\]"), "[]"),
                                value = it.value,
                                description = it.description,
                                type = FieldType.valueOf(it.type.name.toUpperCase()),
                                optional = it.optional,
                                defaultValue = null
                        )
                    }
                    // Fields Deduplication
                    .distinctBy { it.path }
        else mutableListOf()
    }

    /**
     * Deduplication field path
     */
    fun mapToResponseDescriptor(): List<BodyFieldDescriptor> {
        return if (!(responseFields == null || this.responseFields.isEmpty()))
            JsonDeProjector(getBean(ObjectMapper::class.java).convertValue(requestFields, JsonNode::class.java))
                    .deProject()
                    .map {
                        BodyFieldDescriptor(
                                path = it.path.replace(Regex("\\[\\d\\]"), "[]"),
                                value = it.value,
                                description = it.description,
                                type = FieldType.valueOf(it.type.name.toUpperCase()),
                                optional = it.optional,
                                defaultValue = null
                        )
                    }
                    // Fields Deduplication
                    .distinctBy { it.path }
        else mutableListOf()

    }

    fun mapToURIVarDescriptor(): List<URIVarDescriptor> {
        return if (this.uriFields != null && !uriFields.isEmpty()) {
            uriFields.filter { it.field != null && it.field.isNotEmpty() }
                    .map {
                        URIVarDescriptor(
                                field = it.field.toString(),
                                value = it.value.toString(),
                                description = it.desc)
                    }
                    // Fields Deduplication
                    .distinctBy { it.field }
        } else mutableListOf()
    }
}

data class UriVarFieldDto(
        val field: String?,
        val value: Any?,
        val desc: String? = null
)

@Deprecated(message = "HeaderDto")
data class HeaderDto(
        val headerKey: String,
        val headerValue: String,
        val headerDescription: String? = null,
        val headerConstraint: Boolean
)

@Deprecated(message = "ResponseFieldDto")
data class ResponseFieldDto(
        val responseFieldPath: String,
        val responseFieldValue: Any? = null,
        val responseFieldType: String,
        val responseFieldDescription: String? = null,
        val responseFieldConstraint: Boolean
)

@Deprecated(message = "CreateResourceDto")
data class CreateResourceDto(
        val name: String,
        val tag: String,
        var pid: String
)


data class UpdateWikiDto(
        var id: String?,
        val projectId: String,
        val content: String,
        val resource: String? = null,
        val name: String? = null
)

data class UpdateNodeDto(val id: String, val name: String, val pid: String, val order: Int = 0)

data class SyncApiEmptyTemplateDto(val remoteAddress: String,
                                   val projectId: String,
                                   val service: String)


data class UpdateURIVarSnippetDto(val field: String, val value: String, val description: String)
data class UpdateRequestHeaderSnippetDto(val field: String, val value: String, val optional: Any, val description: String)
data class UpdateRequestBodySnippetDto(val path: String, val value: Any, val optional: Any, val description: String)
data class UpdateResponseBodySnippetDto(val path: String, val value: Any, val description: String)
data class UpdateDescriptionSnippetDto(val description: String)

data class UpdateDubboDocumentDto(val description: String? = null, val paramDescriptor: MethodParamDescriptor? = null,
                                  val returnValueDescriptor: MethodReturnValueDescriptor? = null)

data class AuthDto(val account: String, val password: String)

data class SyncRestApiDto(val projectId: String, val docIds: List<String>)

data class LayuiPageDto(val page: Int = 1, val limit: Int = 20) {
    fun toPageable() = PageRequest.of(page - 1, limit)
}

data class BatchDeleteDto(val ids: List<String>)

data class URLExtractDto(val url: String)

data class CreateEmptyDocDto(val name: String, val resourceId: String, val projectId: String, val docType: DocType)

data class CopyDocumentDocDto(val name: String, val documentId: String, val resourceId: String)

data class XmlTextDto(val text: String)


