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

    /**
     * Adds a topic to the retention map.
     *
     * **Note:** This method does not ensure that the topic is created in Kafka.
     *
     * @param topicName The name of the topic.
     * @param retentionTimeInMs The retention time in milliseconds for the topic.
     * @return The previous retention time associated with the topic, or null if it was not present.
     */
    fun addTopic(topicName: String, retentionTimeInMs: Long): Long? =
        topicRetentionMap.put(topicName, retentionTimeInMs)

    /**
     * Removes a topic from the retention map.
     *
     * **Note:** This method does not remove the topic from Kafka.
     *
     * @param topicName The name of the topic.
     * @return The retention time associated with the topic if it existed, or null otherwise.
     */
    fun removeTopic(topicName: String): Long? =
        topicRetentionMap.remove(topicName)

    /**
     * Ensures that a Kafka topic is created with the specified retention time.
     * If the topic exists with a different retention time, it is updated.
     *
     * @param topicNameParameters The topic parameters which include the topic name.
     * @param retentionTimeInMs The desired retention time in milliseconds.
     */
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

    /**
     * Helper method to ensure a topic using a specific service action.
     *
     * This method checks if the topic already exists in the retention map.
     * If it does not exist or if the retention time has changed, it will execute the provided action.
     *
     * @param topicParameters The topic parameters containing the topic name.
     * @param retentionTimeInMs The desired retention time.
     * @param ensureAction A lambda function that calls the appropriate topic service.
     */
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

    /**
     * Checks if the retention time for the given topic is different from the provided retention time.
     *
     * @param topicName The name of the topic.
     * @param newRetentionTimeInMs The new retention time to compare.
     * @return True if the topic does not exist or if the existing retention time differs from newRetentionTime, false otherwise.
     */
    fun hasDifferentRetentionTime(topicName: String, newRetentionTimeInMs: Long): Boolean =
        topicRetentionMap[topicName] != newRetentionTimeInMs

    /**
     * Retrieves the retention time for a given topic.
     *
     * @param topicName The name of the topic.
     * @return The retention time if present, null otherwise.
     */
    fun getRetentionTime(topicName: String): Long? = topicRetentionMap[topicName]

}
