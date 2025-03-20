import no.fintlabs.core.kafka.utils.CoreTopicService
import no.fintlabs.kafka.entity.topic.EntityTopicService
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicService
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import no.fintlabs.kafka.event.topic.EventTopicService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class CoreTopicServiceTest {

    private lateinit var entityTopicService: EntityTopicService
    private lateinit var eventTopicService: EventTopicService
    private lateinit var errorTopicService: ErrorEventTopicService
    private lateinit var coreTopicService: CoreTopicService

    @BeforeEach
    fun setUp() {
        entityTopicService = mock(EntityTopicService::class.java)
        eventTopicService = mock(EventTopicService::class.java)
        errorTopicService = mock(ErrorEventTopicService::class.java)
        coreTopicService = CoreTopicService(entityTopicService, eventTopicService, errorTopicService)
    }

    fun createEventTopicNameParameters(eventName: String) =
        EventTopicNameParameters.builder()
            .orgId("fintlabs-no")
            .domainContext("fint-core")
            .eventName(eventName)
            .build()

    @Test
    fun `addTopic returns null if topic was not present`() {
        val topicNameParameters = createEventTopicNameParameters("testTopic")
        val result = coreTopicService.addTopic(topicNameParameters, 1000L)
        assertNull(result)
        assertEquals(1000L, coreTopicService.getRetentionTime(topicNameParameters))
    }

}
