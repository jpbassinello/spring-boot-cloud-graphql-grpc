package br.com.jpbassinello.sbcgg.services.grpc.users.application.services;

import br.com.jpbassinello.sbcgg.mapstruct.DefaultMapstructConfig;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(config = DefaultMapstructConfig.class)
interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "emailVerified", ignore = true)
  @Mapping(target = "mobilePhoneNumberVerified", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "registeredAt", ignore = true)
  User mapToEntity(ManageUsersUseCase.RegisterUserInput input);
}
