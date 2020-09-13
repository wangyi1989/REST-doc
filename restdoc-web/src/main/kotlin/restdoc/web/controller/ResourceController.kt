package restdoc.web.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.*
import restdoc.web.controller.obj.*
import restdoc.web.core.Result
import restdoc.web.core.ok
import restdoc.web.model.DocType
import restdoc.web.model.Resource
import restdoc.web.repository.DocumentRepository
import restdoc.web.repository.ResourceRepository
import restdoc.web.util.IDUtil
import restdoc.web.util.IDUtil.now
import javax.validation.Valid

@RestController
class ResourceController {

    @Autowired
    lateinit var resourceRepository: ResourceRepository

    @Autowired
    lateinit var documentRepository: DocumentRepository

    @PostMapping("/{projectId}/resource")
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

        findChild(ROOT_NAV, navNodes)

        if (onlyResource) return ok(mutableListOf(ROOT_NAV))

        val allNode = mutableListOf<NavNode>()
        allNode.add(ROOT_NAV)
        allNode.addAll(navNodes)

        val nodeIds = allNode.map { it.id }.toMutableList()

        val docs = documentRepository.list(Query(Criteria("resource").`in`(nodeIds)))

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
        return ok(mutableListOf(ROOT_NAV))
    }


    @DeleteMapping("/resource/{id}")
    fun delete(@PathVariable id: String): Result {
        resourceRepository.deleteById(id)
        return ok()
    }

    @PatchMapping("/resource/{id}")
    fun patch(@PathVariable id: String, @RequestBody @Valid dto: UpdateNodeDto): Result {
        val updateResult = resourceRepository.update(Query().addCriteria(Criteria("_id").`is`(id)),
                Update().set("name", dto.name))

        return ok()
    }
}