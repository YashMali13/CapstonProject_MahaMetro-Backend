package com.aurionpro.app.controller;

import com.aurionpro.app.dto.*;
import com.aurionpro.app.security.JwtService;
import com.aurionpro.app.service.AuthService;
import com.aurionpro.app.service.OtpService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final OtpService otpService;
	private final JwtService jwtService;
	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignupRequest request) {
		authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponse("Registration initiated. Please check your email for a verification OTP."));
	}

	@PostMapping("/verify-registration")
	public ResponseEntity<AuthResponse> verifyRegistration(@Valid @RequestBody VerifyOtpRequest request,
			HttpServletResponse response) {
		AuthResponse tokens = authService.verifyRegistration(request);
		ResponseCookie refreshTokenCookie = createRefreshTokenCookie(tokens.getRefreshToken());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
		return ResponseEntity.ok(tokens);
	}

	@PostMapping("/send-otp")
	public ResponseEntity<ApiResponse> sendLoginOtp(@Valid @RequestBody SendOtpRequest request) {
		otpService.generateAndSendLoginOtp(request.getEmail());
		return ResponseEntity.ok(new ApiResponse("OTP sent successfully to " + request.getEmail()));
	}

	@PostMapping("/verify-login")
	public ResponseEntity<AuthResponse> verifyLogin(@Valid @RequestBody VerifyOtpRequest request,
			HttpServletResponse response) {
		AuthResponse tokens = authService.verifyLogin(request);
		ResponseCookie refreshTokenCookie = createRefreshTokenCookie(tokens.getRefreshToken());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
		return ResponseEntity.ok(tokens);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
		AuthResponse tokens = authService.login(request);
		ResponseCookie refreshTokenCookie = createRefreshTokenCookie(tokens.getRefreshToken());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
		return ResponseEntity.ok(tokens);
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request.getEmail());
		return ResponseEntity.ok(new ApiResponse("If a privileged account with this email exists, a password reset OTP has been sent."));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.ok(new ApiResponse("Password has been reset successfully."));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AccessTokenResponse> refreshAccessToken(
			@CookieValue(name = "refreshToken") String refreshToken) {
		return jwtService.refreshAccessToken(refreshToken)
				.map(newAccessToken -> ResponseEntity.ok(new AccessTokenResponse(newAccessToken)))
				.orElseThrow(() -> new RuntimeException("Invalid or expired refresh token."));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
			HttpServletResponse response) {
		if (refreshToken != null) {
			authService.logout(refreshToken);
		}
		ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "").httpOnly(true).secure(true)
				.path("/api/v1/auth").sameSite("Strict").maxAge(0).build();
		response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
		return ResponseEntity.ok(new ApiResponse("Logged out successfully."));
	}

	private ResponseCookie createRefreshTokenCookie(String token) {
		return ResponseCookie.from("refreshToken", token).httpOnly(true).secure(true).path("/api/v1/auth")
				.sameSite("Strict").maxAge(7 * 24 * 60 * 60).build();
	}
}

