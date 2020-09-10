package restdoc.web.controller.rpctest

import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import restdoc.client.api.model.DubboInvocation
import restdoc.client.api.model.ObjectHolder
import restdoc.remoting.InvokeCallback
import restdoc.remoting.common.ApplicationType
import restdoc.remoting.common.DubboExposedAPI
import restdoc.remoting.common.RequestCode
import restdoc.remoting.protocol.RemotingCommand
import restdoc.remoting.protocol.RemotingSerializable
import restdoc.web.core.ok
import restdoc.web.core.schedule.*

@RestController
class DubboExposedAPITest {

    @Autowired
    lateinit var exposedAPIManager: ExposedAPIManager

    @Autowired
    lateinit var clientChannelManager: ClientChannelManager

    @Autowired
    lateinit var scheduleController: ScheduleController

    @GetMapping("/invokeSimpleDubboAPI")
    fun invokeSimpleDubboAPI(): Any {
        val apiList = exposedAPIManager.listBy(ApplicationType.DUBBO, "echo-service-provider") as List<DubboExposedAPI>

        val pair = exposedAPIManager.dubboExposedExposedAPI
                .filter { it.key.service == "echo-service-provider" }
                .map { it.key.address to it.value }
                .first()

        val remoteAddress = pair.first

        for (api in pair.second) {

            for (method in api.exposedMethods) {
                val request = RemotingCommand.createRequestCommand(RequestCode.INVOKE_API, null)
                method.parameterClasses = arrayOf(String::class.java.name)

                request.body = DubboInvocation(
                        methodName = method.methodName,
                        parameters = listOf(ObjectHolder<Any>(String::class.java.name, "HelloWorld")),
                        refName = api.refName,
                        returnType = method.returnClass
                ).encode()

                val remotingTask = RemotingTask(
                        type = RemotingTaskType.SYNC,
                        request = request,
                        timeoutMills = 100000L,
                        invokeCallback = InvokeCallback { }
                )

                val executeResult = scheduleController.executeRemotingTask(remoteAddress, remotingTask)

                if (executeResult.success) {
                    val result = RemotingSerializable.decode(executeResult.response!!.body, ObjectNode::class.java)
                    println(result)
                }
            }
        }

        return ok()
    }
}