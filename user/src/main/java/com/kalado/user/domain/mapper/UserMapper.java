package com.kalado.user.domain.mapper;

import com.kalado.common.dto.LocationDto;
import com.kalado.user.domain.model.Location;
import com.kalado.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserDto userToDto(User user);

  User dtoToUser(UserDto userDto);

  LocationDto locationToDto(Location location);
}
