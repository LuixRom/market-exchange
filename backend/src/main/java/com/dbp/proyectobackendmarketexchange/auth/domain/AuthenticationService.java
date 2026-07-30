package com.dbp.proyectobackendmarketexchange.auth.domain;

import com.dbp.proyectobackendmarketexchange.auth.dto.JwtAuthResponse;
import com.dbp.proyectobackendmarketexchange.auth.dto.LoginRequest;
import com.dbp.proyectobackendmarketexchange.auth.dto.RegisterRequest;
import com.dbp.proyectobackendmarketexchange.auth.exception.UserAlreadyExistException;
import com.dbp.proyectobackendmarketexchange.auth.infrastructure.AccountTokenRepository;
import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import com.dbp.proyectobackendmarketexchange.config.JwtService;
import com.dbp.proyectobackendmarketexchange.event.usuario.UsuarioCreadoEvent;
import com.dbp.proyectobackendmarketexchange.exception.InvalidUserFieldException;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Role;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final UsuarioRepository usuarioRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final long emailVerificationHours;
    private final long passwordResetMinutes;
    private final long refreshTokenDays;

    public AuthenticationService(UsuarioRepository usuarioRepository,
                                 AccountTokenRepository accountTokenRepository,
                                 JwtService jwtService,
                                 PasswordEncoder passwordEncoder,
                                 ApplicationEventPublisher eventPublisher,
                                 @Value("${app.auth.email-verification-token-hours:24}") long emailVerificationHours,
                                 @Value("${app.auth.password-reset-token-minutes:30}") long passwordResetMinutes,
                                 @Value("${app.auth.refresh-token-days:14}") long refreshTokenDays) {
        this.usuarioRepository = usuarioRepository;
        this.accountTokenRepository = accountTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = new ModelMapper();
        this.eventPublisher = eventPublisher;
        this.emailVerificationHours = emailVerificationHours;
        this.passwordResetMinutes = passwordResetMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public JwtAuthResponse signin(LoginRequest req) {
        String email = EmailNormalizer.normalize(req.getUsername());
        Optional<Usuario> user = usuarioRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Email is not registered");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.get().getPassword())) {
            throw new IllegalArgumentException("Password is incorrect");
        }

        if (!user.get().isEmailVerified()) {
            throw new DisabledException("Debes verificar tu correo antes de iniciar sesion");
        }

        return createAuthResponse(user.get());
    }

    public JwtAuthResponse signup(RegisterRequest req) {
        String email = EmailNormalizer.normalize(req.getEmail());
        if (usuarioRepository.existsByEmail(email)) {
            throw new UserAlreadyExistException("El correo electronico ya esta registrado");
        }

        Usuario newUser = modelMapper.map(req, Usuario.class);
        newUser.setFirstname(req.getFirstName().trim());
        newUser.setLastname(req.getLastName().trim());
        newUser.setEmail(email);
        newUser.setAddress(req.getAddress().trim());
        newUser.setPhone(req.getPhone().trim());
        newUser.setPassword(passwordEncoder.encode(req.getPassword()));
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setRole(Role.USER);
        newUser.setEmailVerified(false);

        usuarioRepository.save(newUser);
        AccountToken verificationToken = createToken(newUser, AccountTokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusHours(emailVerificationHours));
        eventPublisher.publishEvent(new UsuarioCreadoEvent(this, newUser, verificationToken.getToken()));

        JwtAuthResponse response = new JwtAuthResponse();
        response.setEmailVerified(false);
        response.setEmailVerificationToken(verificationToken.getToken());
        return response;
    }

    public void verifyEmail(String token) {
        AccountToken accountToken = loadActiveToken(token, AccountTokenType.EMAIL_VERIFICATION);
        Usuario usuario = accountToken.getUsuario();
        usuario.setEmailVerified(true);
        usuario.setEmailVerifiedAt(LocalDateTime.now());
        usuarioRepository.save(usuario);
        markUsed(accountToken);
    }

    public String requestPasswordReset(String emailInput) {
        String email = EmailNormalizer.normalize(emailInput);
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if (usuario.isEmpty()) {
            return null;
        }

        return createToken(usuario.get(), AccountTokenType.PASSWORD_RESET,
                LocalDateTime.now().plusMinutes(passwordResetMinutes)).getToken();
    }

    public void resetPassword(String token, String newPassword) {
        AccountToken accountToken = loadActiveToken(token, AccountTokenType.PASSWORD_RESET);
        Usuario usuario = accountToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        markUsed(accountToken);
    }

    public JwtAuthResponse refresh(String refreshToken) {
        AccountToken accountToken = loadActiveToken(refreshToken, AccountTokenType.REFRESH);
        Usuario usuario = accountToken.getUsuario();
        if (!usuario.isEmailVerified()) {
            throw new DisabledException("Debes verificar tu correo antes de renovar sesion");
        }
        markUsed(accountToken);
        return createAuthResponse(usuario);
    }

    public void logout(String refreshToken) {
        accountTokenRepository.findByTokenAndType(refreshToken, AccountTokenType.REFRESH)
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    accountTokenRepository.save(token);
                });
    }

    private JwtAuthResponse createAuthResponse(Usuario usuario) {
        JwtAuthResponse response = new JwtAuthResponse();
        response.setToken(jwtService.generateToken(usuario));
        response.setRefreshToken(createToken(usuario, AccountTokenType.REFRESH,
                LocalDateTime.now().plusDays(refreshTokenDays)).getToken());
        response.setEmailVerified(usuario.isEmailVerified());
        return response;
    }

    private AccountToken createToken(Usuario usuario, AccountTokenType type, LocalDateTime expiresAt) {
        AccountToken accountToken = new AccountToken();
        accountToken.setToken(UUID.randomUUID().toString() + UUID.randomUUID());
        accountToken.setType(type);
        accountToken.setUsuario(usuario);
        accountToken.setExpiresAt(expiresAt);
        return accountTokenRepository.save(accountToken);
    }

    private AccountToken loadActiveToken(String token, AccountTokenType type) {
        AccountToken accountToken = accountTokenRepository.findByTokenAndType(token, type)
                .orElseThrow(() -> new InvalidUserFieldException("Token invalido"));
        if (!accountToken.isActive(LocalDateTime.now())) {
            throw new InvalidUserFieldException("Token expirado o ya utilizado");
        }
        return accountToken;
    }

    private void markUsed(AccountToken accountToken) {
        accountToken.setUsedAt(LocalDateTime.now());
        accountTokenRepository.save(accountToken);
    }
}
