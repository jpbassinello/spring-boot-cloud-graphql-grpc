package br.com.jpbassinello.sbcgg.services.messages.application.service;

import br.com.jpbassinello.sbcgg.mapstruct.DefaultMapstructConfig;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(config = DefaultMapstructConfig.class)
interface MessageMapper {

  MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);

  @Mapping(target = "nextAttemptAt", source = "scheduledAt")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "registeredAt", ignore = true)
  @Mapping(target = "sentAt", ignore = true)
  @Mapping(target = "retries", ignore = true)
  @Mapping(target = "recipient", ignore = true)
  @Mapping(target = "status", ignore = true)
  Message mapToEntity(ScheduleMessagesUseCase.ScheduleMessageInput input);
}
