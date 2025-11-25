package br.com.jpbassinello.sbcgg.services.messages.adapter.in;

import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.Message;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageInput;
import br.com.jpbassinello.sbcgg.mapstruct.ProtobufMapstructConfig;
import br.com.jpbassinello.sbcgg.services.messages.application.service.ScheduleMessagesUseCase;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageTemplate;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;

@Mapper(config = ProtobufMapstructConfig.class)
interface MessagesGrpcMapper {

  MessagesGrpcMapper INSTANCE = Mappers.getMapper(MessagesGrpcMapper.class);

  @EnumMapping(nameTransformationStrategy = MappingConstants.STRIP_PREFIX_TRANSFORMATION, configuration = "MESSAGE_TYPE_")
  @ValueMapping(source = "MESSAGE_CHANNEL_UNSPECIFIED", target = MappingConstants.NULL)
  @ValueMapping(source = "UNRECOGNIZED", target = MappingConstants.NULL)
  MessageChannel map(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageChannel protoEnum);

  @EnumMapping(nameTransformationStrategy = MappingConstants.STRIP_PREFIX_TRANSFORMATION, configuration = "MESSAGE_TEMPLATE_")
  @ValueMapping(source = "MESSAGE_TEMPLATE_UNSPECIFIED", target = MappingConstants.NULL)
  @ValueMapping(source = "UNRECOGNIZED", target = MappingConstants.NULL)
  MessageTemplate map(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageTemplate protoEnum);

  @Mapping(target = "variables", source = "additionalVariables")
  ScheduleMessagesUseCase.ScheduleMessageInput mapToInput(SendMessageInput input);

  Message mapToProto(br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message message);

}
