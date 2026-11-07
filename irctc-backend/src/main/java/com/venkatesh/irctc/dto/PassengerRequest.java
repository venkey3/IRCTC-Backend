package com.venkatesh.irctc.dto;

import com.venkatesh.irctc.entity.Passenger.Gender;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PassengerRequest {

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @Min(0)
    @Max(120)
    @NotNull
    private Integer age;

    @NotNull
    private Gender gender;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
