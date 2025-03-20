package no.fintlabs.core.kafka.utils

import no.fintlabs.kafka.common.topic.TopicNameParameters
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters
import no.fintlabs.kafka.entity.topic.EntityTopicService
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicService
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import no.fintlabs.kafka.event.topic.EventTopicService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CoreTopicService(
    private val entityTopicService: EntityTopicService,
    private val eventTopicService: EventTopicService,
    private val errorTopicService: ErrorEventTopicService
) {

    private val topicRetentionMap = mutableMapOf<String, Long>()
    private val logger = LoggerFactory.getLogger(javaClass)


    fun addTopic(topicNameParameters: TopicNameParameters, retentionTimeInMs: Long): Long? =
        topicRetentionMap.put(topicNameParameters.topicName, retentionTimeInMs)

    fun removeTopic(topicNameParameters: TopicNameParameters): Long? =
        topicRetentionMap.remove(topicNameParameters.topicName)

    fun ensureTopic(topicNameParameters: TopicNameParameters, retentionTimeInMs: Long) {
        when (topicNameParameters) {
            is EntityTopicNameParameters -> ensureTopicInternal(topicNameParameters, retentionTimeInMs) { params, retention ->
                entityTopicService.ensureTopic(params, retention)
            }
            is EventTopicNameParameters -> ensureTopicInternal(topicNameParameters, retentionTimeInMs) { params, retention ->
                eventTopicService.ensureTopic(params, retention)
            }
            is ErrorEventTopicNameParameters -> ensureTopicInternal(topicNameParameters, retentionTimeInMs) { params, retention ->
                errorTopicService.ensureTopic(params, retention)
            }
            else -> logger.error(
                "Failed to ensure topic: {} due to non-matching topic parameter type",
                topicNameParameters.topicName
            )
        }
    }

    private fun <T : TopicNameParameters> ensureTopicInternal(
        topicParameters: T,
        retentionTimeInMs: Long,
        ensureAction: (T, Long) -> Unit
    ) = topicParameters.topicName.let { topicName ->
        if (hasDifferentRetentionTime(topicName, retentionTimeInMs)) {
            ensureAction(topicParameters, retentionTimeInMs)
            topicRetentionMap[topicName] = retentionTimeInMs
            logger.info("Ensured topic $topicName with retention time $retentionTimeInMs")
        } else logger.debug("Topic $topicName is already ensured with retention time $retentionTimeInMs")
    }

    fun hasDifferentRetentionTime(topicName: String, newRetentionTimeInMs: Long): Boolean =
        topicRetentionMap[topicName] != newRetentionTimeInMs

    fun getRetentionTime(topicName: String): Long? = topicRetentionMap[topicName]

}
