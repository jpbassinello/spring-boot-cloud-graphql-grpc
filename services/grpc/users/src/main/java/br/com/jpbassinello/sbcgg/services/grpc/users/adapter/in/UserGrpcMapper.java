package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.in;

import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserContactMethod;
import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UserInput;
import br.com.jpbassinello.sbcgg.mapstruct.ProtobufMapstructConfig;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.services.ManageUsersUseCase;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.UserVerificationCodeType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(config = ProtobufMapstructConfig.class)
interface UserGrpcMapper {

  UserGrpcMapper INSTANCE = Mappers.getMapper(UserGrpcMapper.class);

  br.com.jpbassinello.sbcgg.grpc.interfaces.users.User mapToProto(User user);

  ManageUsersUseCase.RegisterUserInput mapToInput(UserInput userInput);

  UserVerificationCodeType mapToEnum(UserContactMethod userContactMethod);

}
