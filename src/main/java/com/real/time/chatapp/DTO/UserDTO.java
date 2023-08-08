package com.real.time.chatapp.DTO;

import java.util.Date;

import com.real.time.chatapp.Entities.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
	private Long id;
	private String firstName;
	private String lastName;
	private String username;
	private String password;
	private Role role;
}
