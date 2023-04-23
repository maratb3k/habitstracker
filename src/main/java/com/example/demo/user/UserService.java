package com.example.demo.user;

import com.example.demo.email.EmailSender;
import com.example.demo.registration.token.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG = "user with email %s not found";
    private final static String TOKEN_NOT_FOUND_MSG = "invalid or expired token";

    private final UserRepository userRepository;
    //private final PasswordResetTokenService passwordResetTokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format(USER_NOT_FOUND_MSG, email)));
    }

    public String signUpUser(User user) {
        boolean userExists = userRepository.findByEmail(user.getEmail()).isPresent();
        if (userExists) {
            throw new IllegalStateException("email already taken");
        }
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);


        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                user
        );
        confirmationTokenService.saveConfirmationToken(
                confirmationToken);
       return token;
   }
// dddddddddddddddddddddddddddddddd
    public void updateResetPasswordToken(String token, String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isPresent()) {
            User foundUser = user.get();
            foundUser.setPasswordResetToken(token);
            userRepository.save(foundUser);
        } else {
            throw new IllegalStateException("Could not find any user with the email " + email);
        }
    }
    public User getByResetPasswordToken(String token) {
        return userRepository.findByPasswordResetToken(token);
    }

    public void updatePassword(User user, String newPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setPasswordResetToken(null);
        userRepository.save(user);
    }

    public int enableUser(String email) {
        return userRepository.enableUser(email);
    }

}