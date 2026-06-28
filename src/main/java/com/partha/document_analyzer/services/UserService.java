package com.partha.document_analyzer.services;

import com.partha.document_analyzer.dto.*;
import com.partha.document_analyzer.entities.User;
import com.partha.document_analyzer.enums.Role;
import com.partha.document_analyzer.exceptions.EmailAlreadyExistsException;
import com.partha.document_analyzer.exceptions.InvalidRequestException;
import com.partha.document_analyzer.exceptions.UserNotFoundException;
import com.partha.document_analyzer.exceptions.UsernameAlreadyExistException;
import com.partha.document_analyzer.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inject BCrypt encoder
    private final JwtService jwtService;

    @Transactional
    public UserResponseDto registerUser(RegisterRequestDto request) {

        if (userRepository.existsByUsername(request.getUsername().toLowerCase())) {
            throw new UsernameAlreadyExistException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        user.setPassword(hashedPassword);
        user.setPhone(request.getPhone());
        user.setRole(Role.USER);

        userRepository.save(user);

        return new UserResponseDto(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username.toLowerCase());
    }

    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username.toUpperCase())
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return new UserResponseDto(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        return new UserResponseDto(user);
    }

    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(
                user-> new UserResponseDto(user)).collect(Collectors.toList());
    }

    public List<UserResponseDto> getUsersByRole(Role role) {
        List<User> users = userRepository.findByRole(role);

        return users.stream().map(user -> new UserResponseDto(user)).collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateUser(Long userId, UpdateUserRequestDto request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            Optional<User> userWithEmail = userRepository.findByEmail(request.getEmail().toLowerCase());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(userId)) {
                throw new EmailAlreadyExistsException("Email already taken by another user");
            }
            existingUser.setEmail(request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            existingUser.setPhone(request.getPhone());
        }

        User updatedUser = userRepository.save(existingUser);

        return new UserResponseDto(updatedUser);
    }

    @Transactional
    public UserResponseDto updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId).
                orElseThrow(()-> new UserNotFoundException("User not found with id: "+ userId));
        user.setRole(newRole);
        userRepository.save(user);

        return new UserResponseDto(user);

    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public long countUsers() {
        return userRepository.count();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username.toLowerCase());
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }

    public UserLoginResponseDto loginUser(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid Password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        UserLoginResponseDto response = new UserLoginResponseDto();
        response.setToken(token);
        response.setType("Bearer");
        response.setExpiresIn(86400000L);
        response.setUser(new UserResponseDto(user));

        return response;
    }
}