import no.fintlabs.core.kafka.utils.CoreTopicService
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters
import no.fintlabs.kafka.entity.topic.EntityTopicService
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters
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

    @Test
    fun `removeTopic returns retention time if topic is present`() {
        val topicNameParameters = createEventTopicNameParameters("testRemove")
        coreTopicService.addTopic(topicNameParameters, 2000L)
        val removed = coreTopicService.removeTopic(topicNameParameters)
        assertEquals(2000L, removed)
        assertNull(coreTopicService.getRetentionTime(topicNameParameters))
    }

    @Test
    fun `ensureTopic calls eventTopicService when retention time differs`() {
        val topicNameParameters = createEventTopicNameParameters("testEnsureEvent")
        coreTopicService.ensureTopic(topicNameParameters, 3000L)
        verify(eventTopicService).ensureTopic(topicNameParameters, 3000L)
        assertEquals(3000L, coreTopicService.getRetentionTime(topicNameParameters))
    }

    @Test
    fun `ensureTopic does not call eventTopicService when retention time is same`() {
        val topicNameParameters = createEventTopicNameParameters("testEnsureEventSame")
        coreTopicService.addTopic(topicNameParameters, 4000L)
        coreTopicService.ensureTopic(topicNameParameters, 4000L)
        verify(eventTopicService, never()).ensureTopic(topicNameParameters, 4000L)
        assertEquals(4000L, coreTopicService.getRetentionTime(topicNameParameters))
    }

    @Test
    fun `hasDifferentRetentionTime returns correct boolean`() {
        val topicNameParameters = createEventTopicNameParameters("testDiff")

        assertTrue(coreTopicService.hasDifferentRetentionTime(topicNameParameters.topicName, 5000L))

        coreTopicService.addTopic(topicNameParameters, 5000L)
        assertFalse(coreTopicService.hasDifferentRetentionTime(topicNameParameters.topicName, 5000L))
        assertTrue(coreTopicService.hasDifferentRetentionTime(topicNameParameters.topicName, 6000L))
    }

    @Test
    fun `ensureTopic calls entityTopicService when retention time differs`() {
        val topicNameParameters = mock(EntityTopicNameParameters::class.java)
        `when`(topicNameParameters.topicName).thenReturn("testEntity")
        coreTopicService.ensureTopic(topicNameParameters, 7000L)
        verify(entityTopicService).ensureTopic(topicNameParameters, 7000L)
        assertEquals(7000L, coreTopicService.getRetentionTime(topicNameParameters))
    }

    @Test
    fun `ensureTopic calls errorTopicService when retention time differs`() {
        val topicNameParameters = mock(ErrorEventTopicNameParameters::class.java)
        `when`(topicNameParameters.topicName).thenReturn("testError")
        coreTopicService.ensureTopic(topicNameParameters, 8000L)
        verify(errorTopicService).ensureTopic(topicNameParameters, 8000L)
        assertEquals(8000L, coreTopicService.getRetentionTime(topicNameParameters))
    }

}
