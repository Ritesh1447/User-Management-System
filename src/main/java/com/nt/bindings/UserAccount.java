package com.nt.bindings;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
    private String name;
    private String email;
    private Long mobileNo;       // use Long for big numbers
    private String gender = "Female";

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;       // use LocalDate for proper JSON mapping

    private Long aadharNo;       // use Long for big numbers
}