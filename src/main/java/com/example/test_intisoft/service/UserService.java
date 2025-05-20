package com.example.test_intisoft.service;

import com.example.test_intisoft.model.User;
import com.example.test_intisoft.model.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDTO> getAllUsers();
    Optional<UserDTO> getUserById(Long id);
    UserDTO saveUser(UserDTO userDTO);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);

    UserDTO convertToDTO(User user);
    User convertToEntity(UserDTO userDTO);
}
