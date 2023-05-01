package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

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
    public List<UserDto> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        long userID = userDto.getId();
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "User with id = %d doesn't exist.", userID)));

        String nameUpdate = userDto.getName();
        String emailUpdate = userDto.getEmail();

        if (emailUpdate != null) {
            user.setEmail(emailUpdate);
        }
        if (nameUpdate != null) {
            user.setName(nameUpdate);
        }

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getUserByID(long userID) {
        User user = userRepository.findById(userID).orElseThrow(
                () -> new NotFoundException(String.format(
                        "User with id = %d doesn't exist", userID)));

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto deleteUserByID(long userID) {
        //TODO: удалить все его вещи?
        Optional<User> userToDelete = userRepository.findById(userID);

        if (userToDelete.isEmpty()) {
            throw new NotFoundException(String.format(
                    "User with id = %d doesn't exist.", userID));
        }
        userRepository.deleteById(userID);
        return UserMapper.toUserDto(userToDelete.get());
    }
}
