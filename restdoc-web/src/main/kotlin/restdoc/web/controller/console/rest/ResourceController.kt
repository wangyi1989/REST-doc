package restdoc.web.controller.console.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.*
import restdoc.remoting.common.ApplicationType
import restdoc.web.base.auth.Verify
import restdoc.web.controller.console.model.*
import restdoc.web.core.Result
import restdoc.web.core.Status
import restdoc.web.core.ok
import restdoc.web.model.DocType
import restdoc.web.model.ProjectType
import restdoc.web.model.Resource
import restdoc.web.repository.DubboDocumentRepository
import restdoc.web.repository.ProjectRepository
import restdoc.web.repository.ResourceRepository
import restdoc.web.repository.RestWebDocumentRepository
import restdoc.web.util.IDUtil
import restdoc.web.util.IDUtil.now
import javax.validation.Valid


/**
 *
 */
@RestController
class ResourceController {

    @Autowired
    lateinit var resourceRepository: ResourceRepository

    @Autowired
    lateinit var restWebDocumentRepository: RestWebDocumentRepository

    @Autowired
    lateinit var dubboDocumentRepository: DubboDocumentRepository

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @PostMapping("/{projectId}/resource")
    @Verify(role = ["SYS_ADMIN"])
    fun create(@PathVariable projectId: String, @Valid @RequestBody dto: CreateResourceDto): Result {
        val resource = Resource(
                id = IDUtil.id(),
                tag = dto.tag,
                name = dto.name,
                pid = dto.pid,
                projectId = projectId,
                createTime = now(),
                createBy = "System")
        resourceRepository.save(resource)
        return ok()
    }

    @RequestMapping("/{projectId}/resource/dtree")
    @Verify(role = ["*"])
    fun getResourceDTree(@PathVariable projectId: String,
                         @RequestParam at: ApplicationType): Any {

        val resourceQuery = Query(Criteria("projectId").`is`(projectId))
        resourceQuery.with(Sort.by(Sort.Order.desc("order"), Sort.Order.desc("createTime")))
        val resources = resourceRepository.list(resourceQuery)

        val resourceIds = resources.map { it.id }.toMutableList()
        resourceIds.add("root")

        val resourceNodes = resources.map {
            DTreeVO(id = it.id!!,
                    title = it.name!!,
                    parentId = it.pid!!,
                    type = NodeType.RESOURCE,
                    iconClass = "dtree-icon-weibiaoti5")
        }

        val docQuery = Query(Criteria("resource").`in`(resourceIds).and("projectId").`is`(projectId))
        docQuery.with(Sort.by(Sort.Order.desc("order"), Sort.Order.asc("createTime")))

        val apiNodes = if (at == ApplicationType.REST_WEB) {
            docQuery.fields()
                    .exclude("requestHeaderDescriptor")
                    .exclude("requestBodyDescriptor")
                    .exclude("responseBodyDescriptors")
                    .exclude("description")
                    .exclude("uriVarDescriptors")
                    .exclude("queryParamFieldDescriptor")

            val webDocs = restWebDocumentRepository.list(docQuery)
            webDocs.map {
                DTreeVO(id = it.id!!,
                        title = it.name!!,
                        parentId = it.resource,
                        type = if (it.docType == DocType.API) NodeType.API else NodeType.WIKI,
                        iconClass = "dtree-icon-normal-file")
            }
        } else {
            docQuery.fields().exclude("paramDescriptors").exclude("returnValueDescriptor")
            val dubboDocs = dubboDocumentRepository.list(docQuery)
            dubboDocs.map {
                DTreeVO(id = it.id,
                        title = it.name,
                        parentId = it.resource,
                        type = if (it.docType == DocType.API) NodeType.API else NodeType.WIKI,
                        iconClass = "dtree-icon-normal-file")
            }
        }

        val rootNode = DTreeVO(
                id = "root",
                title = "一级目录(虚拟)",
                parentId = "0",
                type = NodeType.RESOURCE,
                spread = true,
                iconClass = "dtree-icon-weibiaoti5")

        val nodes = mutableListOf<DTreeVO>()
        nodes.addAll(resourceNodes)
        nodes.addAll(apiNodes)
        nodes.add(rootNode)

        return layuiTableOK(data = nodes, count = 1)
    }

    @GetMapping("/{projectId}/resource/flatten")
    fun getFlattenResource(@PathVariable projectId: String): Any {
        val resources = resourceRepository.list(Query(Criteria("projectId").`is`(projectId)))
        val navNodes = resources.map {
            NavNode(id = it.id!!,
                    title = it.name!!,
                    field = "name",
                    children = null,
                    pid = it.pid!!)
        }.toMutableList()

        val rootNav = NavNode(
                id = "root",
                title = "一级目录(虚拟)",
                field = "title",
                children = mutableListOf(),
                href = null,
                pid = "0",
                checked = true)

        navNodes.add(rootNav)

        return navNodes
    }

    /**
     *
     * TODO   code review
     */
    @GetMapping("/{projectId}/resource/tree")
    fun getTree(@PathVariable projectId: String, @RequestParam(required = false, defaultValue = "false") onlyResource: Boolean): Result {
        val resources = resourceRepository.list(Query(Criteria("projectId").`is`(projectId)))

        val navNodes = resources.map {
            NavNode(id = it.id!!,
                    title = it.name!!,
                    field = "name",
                    children = null,
                    pid = it.pid!!)
        }

        val rootNav = NavNode(
                id = "root",
                title = "一级目录",
                field = "title",
                children = mutableListOf(),
                href = null,
                pid = "0",
                checked = true)

        findChild(rootNav, navNodes)

        if (onlyResource) return ok(mutableListOf(rootNav))
        val allNode = mutableListOf<NavNode>()

        allNode.add(rootNav)
        allNode.addAll(navNodes)

        val nodeIds = allNode.map { it.id }.toMutableList()

        val docs = restWebDocumentRepository.list(Query(Criteria("resource").`in`(nodeIds).and("projectId").`is`(projectId)))

        for (navNode in allNode) {
            val childrenDocNode: MutableList<NavNode> = docs
                    .filter { navNode.id == it.resource }
                    .map {
                        val node = NavNode(
                                id = it.id!!,
                                title = it.name!!,
                                field = "",
                                children = mutableListOf(),
                                href = null,
                                pid = navNode.id,
                                spread = true,
                                checked = false)

                        node.type = if (DocType.API == it.docType) NodeType.API else NodeType.WIKI
                        node
                    }.toMutableList()

            if (navNode.children != null) {
                navNode.children!!.addAll(childrenDocNode)
            } else {
                navNode.children = childrenDocNode
            }
        }
        return ok(mutableListOf(rootNav))
    }

    @Verify(role = ["SYS_ADMIN"])
    @DeleteMapping("/{projectId}/resource/{id}")
    fun delete(@PathVariable id: String, @PathVariable projectId: String): Result {
        val project = projectRepository.findById(projectId)
                .orElseThrow { Status.BAD_REQUEST.instanceError("${projectId}项目不存在") }

        val quantity = if (project.type == ProjectType.REST_WEB) {
            restWebDocumentRepository.count(Query(Criteria("resource").`is`(id)))
        } else if (project.type == ProjectType.DUBBO) {
            dubboDocumentRepository.count(Query(Criteria("resource").`is`(id)))
        } else 0

        if (quantity != 0L) Status.BAD_REQUEST.error("当前目录下存在关联的文档，无法删除")
        resourceRepository.deleteById(id)
        return ok()
    }

    @PatchMapping("/resource")
    @Verify(role = ["SYS_ADMIN"])
    fun patch(@RequestBody @Valid dto: UpdateNodeDto): Result {
        val updateResult = resourceRepository.update(Query().addCriteria(Criteria("_id").`is`(dto.id)),
                Update().set("name", dto.name)
                        .set("order", dto.order)
                        .set("pid", dto.pid
                        )
        )
        return ok()
    }
}