package com.nt.bindings;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveUser {
	
	private String email;
	private String tempPassword;
	private String newPassword;
	private String confirmPassword;
	


}
