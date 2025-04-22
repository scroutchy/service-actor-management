package com.scr.project.sam.domains.actor.mapper

import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.srm.RewardedEntityTypeKafkaDto.ACTOR
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Locale

class ActorKafkaMappingsTest {

    @Test
    fun `toRewardedKafka should succeed`() {
        val actor = Actor("surname", "name", Locale.FRANCE, LocalDate.now(), id = ObjectId.get())
        val rewarded = actor.toRewardedKafka()
        assertThat(rewarded.id).isEqualTo(actor.id.toHexString())
        assertThat(rewarded.type).isEqualTo(ACTOR)
    }
}