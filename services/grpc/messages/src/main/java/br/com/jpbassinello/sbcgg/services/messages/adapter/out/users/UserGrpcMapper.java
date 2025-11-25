package br.com.jpbassinello.sbcgg.services.messages.adapter.out.users;

import br.com.jpbassinello.sbcgg.mapstruct.ProtobufMapstructConfig;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(config = ProtobufMapstructConfig.class)
interface UserGrpcMapper {

  UserGrpcMapper INSTANCE = Mappers.getMapper(UserGrpcMapper.class);

  User mapToDomain(br.com.jpbassinello.sbcgg.grpc.interfaces.users.User proto);
}
