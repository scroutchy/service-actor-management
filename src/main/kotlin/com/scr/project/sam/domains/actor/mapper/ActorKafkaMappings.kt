package com.scr.project.sam.domains.actor.mapper

import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.srm.RewardedEntityTypeKafkaDto.ACTOR
import com.scr.project.srm.RewardedKafkaDto

fun Actor.toRewardedKafka() = RewardedKafkaDto(id?.toHexString(), ACTOR)