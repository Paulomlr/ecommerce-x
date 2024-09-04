package com.paulo.ecommerceX.services;

import com.paulo.ecommerceX.domain.User;
import com.paulo.ecommerceX.domain.dto.user.PasswordRequestDTO;
import com.paulo.ecommerceX.domain.dto.user.UserRequestDTO;
import com.paulo.ecommerceX.domain.dto.user.UserResponseDTO;
import com.paulo.ecommerceX.domain.enums.UserRole;
import com.paulo.ecommerceX.repositories.UserRepository;
import com.paulo.ecommerceX.services.exceptions.ResourceNotFoundException;
import com.paulo.ecommerceX.services.exceptions.UniqueConstraintViolationException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new ResourceNotFoundException("Login already exists."));
    }

    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    public UserResponseDTO findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found. Id: " + id));
        return new UserResponseDTO(user);
    }

    public User registerUser(UserRequestDTO body){
        return createUser(body, UserRole.USER);
    }

    public User registerAdmin(UserRequestDTO body) {
        return createUser(body, UserRole.ADMIN);
    }

    private User createUser(UserRequestDTO body, UserRole role) {
        if(userRepository.findByLogin(body.login()).isPresent()) {
            throw new UniqueConstraintViolationException("Login already exists.");
        }
        String encryptedPassword = new BCryptPasswordEncoder().encode(body.password());
        User user = new User(body.login(), encryptedPassword, role);

        return userRepository.save(user);
    }

    public void delete(UUID id){
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.delete(user.get());
        } else {
            throw new ResourceNotFoundException("User not found. Id: " + id);
        }
    }

    public void updatePassword(String login, PasswordRequestDTO body) {
        if (login == null) {
            throw new RuntimeException("Invalid or expired token.");
        }

        User user = (User) userRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        String encryptedPassword = new BCryptPasswordEncoder().encode(body.password());

        user.setPassword(encryptedPassword);
        userRepository.save(user);
    }
}
