package com.example.test_intisoft.service;

import com.example.test_intisoft.model.User;
import com.example.test_intisoft.model.UserDTO;
import com.example.test_intisoft.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public UserDTO saveUser(UserDTO userDTO) {
        User user = convertToEntity(userDTO);
        User saved = userRepository.save(user);
        return convertToDTO(saved);
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setFullname(userDTO.fullname());
            user.setUsername(userDTO.username());
            user.setEmail(userDTO.email());
            user.setPassword(userDTO.password());
            return convertToDTO(userRepository.save(user));
        } else {
            throw new RuntimeException("User not found with id " + id);
        }
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Mapping methods
    public UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getFullname(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }

    public User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.id());
        user.setFullname(userDTO.fullname());
        user.setUsername(userDTO.username());
        user.setEmail(userDTO.email());
        user.setPassword(userDTO.password());
        user.setRole(userDTO.role());
        return user;
    }

}
