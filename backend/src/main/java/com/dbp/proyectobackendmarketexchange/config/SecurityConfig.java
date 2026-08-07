package com.dbp.proyectobackendmarketexchange.config;

import com.dbp.proyectobackendmarketexchange.usuario.domain.UserDetailsServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";
    private static final String ITEM_PATH = "/item";
    private static final String ITEM_WILDCARD_PATH = "/item/**";
    private static final String CATEGORY_ID_PATH = "/category/{id}";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userService;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private List<String> allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsServiceImpl userService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // SonarQube security hotspot (S4502), reviewed and confirmed safe:
        // CSRF exploits rely on the browser automatically attaching an ambient credential
        // (a cookie) to a cross-site request. This API has none to attach:
        //   - JwtAuthenticationFilter reads the JWT only from the Authorization header.
        //   - Session creation is STATELESS below; Spring Security never issues a session
        //     cookie.
        //   - The frontend stores both the access and refresh tokens in sessionStorage
        //     (frontend/src/context/AuthProvider.tsx, frontend/src/apis/api.ts) and attaches
        //     them to the Authorization header from JS — sessionStorage is per-origin and is
        //     never sent automatically by the browser the way a cookie would be.
        //   - grep across backend/src/main for Cookie/HttpOnly/Set-Cookie: no matches.
        // With no ambient credential, a forged cross-site request cannot carry a valid
        // Authorization header, so CSRF protection has nothing to defend here.
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        // Rutas públicas
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasAuthority(ADMIN)


                        // Acceso de USER a sus propios ítems
                        .requestMatchers(HttpMethod.POST, ITEM_PATH).hasAuthority(USER)
                        .requestMatchers(HttpMethod.DELETE, "/item/images/orphans").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/item/{itemId}/images").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/item/{itemId}/images/{imageId}").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/item/{itemId}/images").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/item/{itemId}/images/{imageId}").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/item/{itemId}/images/{imageId}/primary").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, ITEM_WILDCARD_PATH).hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, ITEM_WILDCARD_PATH).hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/item/category/{categoryId}").hasAuthority(USER)
                        .requestMatchers(HttpMethod.GET, "/item/user/{userId}").hasAuthority(USER)
                        .requestMatchers(HttpMethod.GET, "/item/mine").hasAuthority(USER)
                        .requestMatchers(HttpMethod.GET, "/item/catalog").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/item/favorites").hasAuthority(USER)
                        .requestMatchers(HttpMethod.POST, "/item/{itemId}/favorite").hasAuthority(USER)
                        .requestMatchers(HttpMethod.DELETE, "/item/{itemId}/favorite").hasAuthority(USER)
                        .requestMatchers(HttpMethod.GET, "/item/{id}").hasAuthority(USER)
                        .requestMatchers(HttpMethod.GET, ITEM_PATH).hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/item/{id}/image").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, ITEM_PATH).hasAnyAuthority(USER, ADMIN)

                        // Acceso de ADMIN a sus propios ítems
                        .requestMatchers(HttpMethod.POST, "/item/{itemId}/approve").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/item/{itemId}/moderation-history").hasAuthority(ADMIN)


                        // Acceso de USER a categorías
                        .requestMatchers(HttpMethod.GET, "/category").hasAuthority(USER) // Ver todas las categorías
                        .requestMatchers(HttpMethod.GET, CATEGORY_ID_PATH).hasAuthority(USER) // Ver una categoría por ID
                        // Acceso de ADMIN a categorías
                        .requestMatchers(HttpMethod.POST, "/category").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.PUT, CATEGORY_ID_PATH).hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, CATEGORY_ID_PATH).hasAuthority(ADMIN)

                        // Acceso de USER a ratings
                        .requestMatchers(HttpMethod.POST, "/ratings/crear").hasAuthority(USER)
                        .requestMatchers(HttpMethod.PUT, "/ratings/{id}").hasAuthority(USER)
                        .requestMatchers(HttpMethod.GET, "/ratings/usuario/{usuarioId}/reputation").hasAuthority(USER)
                        .requestMatchers(HttpMethod.GET, "/ratings/usuario/{usuarioId}").hasAuthority(USER)
                        // Acceso de ADMIN a ratings
                        .requestMatchers(HttpMethod.DELETE, "/ratings/{id}").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/ratings/listar").hasAuthority(ADMIN)


                        // Acceso de USER a agreements
                        .requestMatchers(HttpMethod.POST, "/agreements").hasAuthority(USER)
                        .requestMatchers(HttpMethod.GET, "/agreements/{id}").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/agreements").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/agreements/sent").hasAuthority(USER)
                        .requestMatchers(HttpMethod.GET, "/agreements/received").hasAuthority(USER)
                        .requestMatchers(HttpMethod.PUT, "/agreements/{id}/accept").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/agreements/{id}/reject").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/agreements/{id}/cancel").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/agreements/{tradeProposalId}/messages").hasAuthority(USER)
                        .requestMatchers(HttpMethod.POST, "/agreements/{tradeProposalId}/messages").hasAuthority(USER)
                        // Acceso de ADMIN a agreements

                        .requestMatchers(HttpMethod.DELETE, "/agreements/{id}").hasAuthority(ADMIN)

                        // Acceso de USER (proposer/receiver, verificado en el servicio con
                        // AuthorizationUtils) a shipments; GET listado global y DELETE quedan
                        // ADMIN vía la regla catch-all de abajo.
                        .requestMatchers(HttpMethod.GET, "/shipments/trade-proposal/{tradeProposalId}").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/shipments/{id}").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/shipments/{id}").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/shipments/{id}/prepare").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/shipments/{id}/ship").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/shipments/{id}/deliver").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/shipments/{id}/confirm-delivery").hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/shipments/{id}/cancel").hasAnyAuthority(USER, ADMIN)

                        // Acceso de USER a sus propios datos de usuario
                        .requestMatchers(HttpMethod.GET, "/usuarios/me").hasAuthority(USER)
                        .requestMatchers(HttpMethod.PUT, "/usuarios/me/profile").hasAuthority(USER)
                        .requestMatchers(HttpMethod.PUT, "/usuarios/{id}/suspend").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/usuarios/{id}/unsuspend").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/usuarios/{id}/block").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/usuarios/{id}/unblock").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/usuarios/{id}").hasAuthority(USER)
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/{id}").hasAuthority(USER)
                        .requestMatchers("/notifications/**").hasAuthority(USER)
                        .requestMatchers(HttpMethod.POST, "/reports").hasAuthority(USER)
                        .requestMatchers("/admin/reports/**").hasAuthority(ADMIN)
                        // Acceso de ADMIN a sus propios datos de usuario
                        .requestMatchers(HttpMethod.GET, "/usuarios/listar").hasAuthority(ADMIN)

                        // Acceso de ADMIN a todas las rutas
                        .requestMatchers(ITEM_WILDCARD_PATH).hasAuthority(ADMIN)
                        .requestMatchers("/category/**").hasAuthority(ADMIN)
                        .requestMatchers("/usuarios/**").hasAuthority(ADMIN)
                        .requestMatchers("/shipments/**").hasAuthority(ADMIN)
                        .requestMatchers("/agreements/**").hasAuthority(ADMIN)
                        .requestMatchers("/ratings/**").hasAuthority(ADMIN)

                        // Regla final explícita: cualquier otra ruta no listada requiere autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(allowedOrigins);
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userService.userDetailsService());
        return authProvider;
    }


    @Bean
    static RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ADMIN > USER");

        return hierarchy;
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        expressionHandler.setDefaultRolePrefix("");

        return expressionHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
