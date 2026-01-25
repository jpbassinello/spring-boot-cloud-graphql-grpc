package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.out.user;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.Role;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs.RegisterUserInput;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.User;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserContactMethod;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserRole;
import br.com.jpbassinello.sbcgg.mapstruct.ProtobufMapstructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(config = ProtobufMapstructConfig.class)
interface UserGrpcMapper {

  UserGrpcMapper INSTANCE = Mappers.getMapper(UserGrpcMapper.class);

  Role mapToEnum(UserRole role);

  UserRole mapToProto(Role role);

  @Mapping(target = "messages", ignore = true)
  br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User mapToType(User user);

  @Mapping(target = "roles", ignore = true)
  br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserInput mapToProto(RegisterUserInput userInput);

  UserContactMethod mapToProto(br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.UserContactMethod method);

}
