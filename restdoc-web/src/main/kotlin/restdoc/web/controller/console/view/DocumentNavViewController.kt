package restdoc.web.controller.console.view

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import restdoc.remoting.common.ApplicationType
import restdoc.web.base.auth.Verify
import restdoc.web.core.schedule.ClientRegistryCenter
import restdoc.web.model.ProjectType
import restdoc.web.repository.ProjectRepository

@Controller
@Verify
@Deprecated(message = "DocumentNavViewController")
class DocumentNavViewController {

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var clientRegistryCenter: ClientRegistryCenter

    @GetMapping("/{projectId}/document/nav/view")
    fun index(@PathVariable projectId: String, model: Model): String {

        model.addAttribute("projectId", projectId)

        val project = projectRepository.findById(projectId)
                .orElseThrow(restdoc.web.core.Status.BAD_REQUEST::instanceError)

        model.addAttribute("projectName", project.name)

        return when {
            ProjectType.REST_WEB == project.type -> {
                val instanceNumber = clientRegistryCenter.getClientKeysFilterApplicationType(ApplicationType.REST_WEB).size
                model.addAttribute("instanceNumber", instanceNumber)
                if (instanceNumber > 0) model.addAttribute("hasInstance", true)
                "explorer/nav"
            }
            ProjectType.DUBBO.equals(project.type) -> {
                "overview/dubbo-overview"
            }
            else -> {
                ""
            }
        }
    }
}