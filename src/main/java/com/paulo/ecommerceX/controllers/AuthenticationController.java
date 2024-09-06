package com.paulo.ecommerceX.controllers;

import com.paulo.ecommerceX.config.security.TokenService;
import com.paulo.ecommerceX.domain.User;
import com.paulo.ecommerceX.domain.dto.user.*;
import com.paulo.ecommerceX.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final UserService service;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll(){
        List<UserResponseDTO> usersList = service.findAll();
        return ResponseEntity.ok(usersList);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id) {
        UserResponseDTO user = service.findById(id);
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO body){
        var usernamePassword = new UsernamePasswordAuthenticationToken(body.login(), body.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        var user = (User) auth.getPrincipal();
        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(user.getUserId(), token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid UserRequestDTO body){
        User registerUser = service.registerUser(body);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registerUser.getUserId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PostMapping("/register/admin")
    public ResponseEntity<Void> registerAdmin(@RequestBody @Valid UserRequestDTO body){
        User registerUser = service.registerAdmin(body);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registerUser.getUserId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<LoginResponseDTO> requestPasswordReset(@RequestBody LoginRequestDTO body) {
        User user = (User) service.loadUserByUsername(body.login());
        String resetToken = tokenService.generateResetToken(user);
        return ResponseEntity.ok(new LoginResponseDTO(user.getUserId(), resetToken));
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<Void> resetPassword(@PathVariable String token, @RequestBody PasswordRequestDTO body) {
        String login = tokenService.validateToken(token);
        service.updatePassword(login, body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
