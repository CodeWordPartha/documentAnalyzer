package com.partha.document_analyzer.controller;

import com.partha.document_analyzer.dto.SuccessResponseDto;
import com.partha.document_analyzer.dto.UpdateUserRequestDto;
import com.partha.document_analyzer.dto.UserResponseDto;
import com.partha.document_analyzer.enums.Role;
import com.partha.document_analyzer.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        UserResponseDto user = userService.getUserByUsername(username);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponseDto>> getUserByRole(@PathVariable Role role) {
        List<UserResponseDto> user = userService.getUsersByRole(role);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponseDto> updateUserById(@PathVariable Long id, @Valid @RequestBody UpdateUserRequestDto request) {
        UserResponseDto user = userService.updateUser(id, request);
        SuccessResponseDto response = new SuccessResponseDto("User succesfully created",user);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<SuccessResponseDto> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        UserResponseDto user = userService.updateUserRole(id, role);
        SuccessResponseDto response = new SuccessResponseDto("user role has been updated", user);

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponseDto> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        SuccessResponseDto response = new SuccessResponseDto(
                "User deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<SuccessResponseDto> getUserCount() {
        long count = userService.countUsers();

        SuccessResponseDto response = new SuccessResponseDto(
                "Total users",
                count
        );

        return ResponseEntity.ok(response);
    }
}