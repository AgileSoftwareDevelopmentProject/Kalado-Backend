package com.kalado.common.feign.user;

import static com.kalado.common.util.UrlConstraint.*;

import com.kalado.common.dto.LocationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserApi {
  @PutMapping(USER + "/{id}")
  Boolean modifyUserProfile(@PathVariable long id, @RequestBody UserDto userDto);
}
