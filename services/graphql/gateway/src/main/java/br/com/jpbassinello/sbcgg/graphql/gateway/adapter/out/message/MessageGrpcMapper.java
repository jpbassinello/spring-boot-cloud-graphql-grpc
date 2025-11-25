package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.out.message;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.MessageStatus;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.MessageTemplate;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.Message;
import br.com.jpbassinello.sbcgg.mapstruct.ProtobufMapstructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(config = ProtobufMapstructConfig.class)
interface MessageGrpcMapper {

  MessageGrpcMapper INSTANCE = Mappers.getMapper(MessageGrpcMapper.class);

  MessageChannel mapToEnum(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageChannel channel);

  MessageTemplate mapToEnum(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageTemplate template);

  MessageStatus mapToEnum(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageStatus status);

  br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.Message mapToType(Message message);

}
