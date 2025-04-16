package com.scr.project.sam.domains.actor.mapper

import com.scr.project.sam.RewardedEntityTypeKafkaDto.ACTOR
import com.scr.project.sam.RewardedKafkaDto
import com.scr.project.sam.domains.actor.model.entity.Actor

fun Actor.toRewardedKafka() = RewardedKafkaDto(id?.toHexString(), ACTOR)