package com.example.demo.registration;

import com.example.demo.email.EmailService;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

//@RestController
//@RequestMapping(path = "api/v1/passwordreset")
//@AllArgsConstructor
//public class PasswordResetController {
//
//    private final PasswordResetService passwordResetService;
//
//    @PostMapping
//    public ResponseEntity<String> requestResetPassword(@RequestBody PasswordResetRequest request) {
//        String response = passwordResetService.resetPassword(request);
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/confirm")
//    public ResponseEntity<String> confirmResetPassword(@RequestParam("token") String token,
//                                                       @RequestParam("newPassword") String newPassword) {
//        String response = passwordResetService.confirmResetPassword(token, newPassword);
//        return ResponseEntity.ok(response);
//    }
//
//}

@Controller
@RequestMapping(path = "api/v1")
public class PasswordResetController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;



    @PostMapping("/forgot_password")
    public String processForgotPassword(HttpServletRequest request) {
        String email = request.getParameter("email");
        String token = RandomString.make(30);
        userService.updateResetPasswordToken(token, email);
        String resetPasswordLink = "http://localhost:8080/api/v1/reset_password?token=" + token;
        emailService.send(email, "Reset password", resetPasswordLink);
        return "message sended to email";
    }

    @PostMapping("/reset_password")
    public void processResetPassword(HttpServletRequest request) {
        String token = request.getParameter("token");
        String password = request.getParameter("password");
        User user = userService.getByResetPasswordToken(token);
        userService.updatePassword(user, password);
        emailService.send(user.getEmail(), "Password changed", "You have successfully changed your password!");
    }
}