package restdoc.web.core.schedule

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


/**
 * Schedule server config properties
 *
 * @author Maple
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "restdoc.schedule")
data
class ScheduleProperties(val port: Int)