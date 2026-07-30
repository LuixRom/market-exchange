package com.dbp.proyectobackendmarketexchange.config;

import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import com.dbp.proyectobackendmarketexchange.category.infrastructure.CategoryRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Role;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminFirstname;
    private final String adminLastname;
    private final String adminPhone;
    private final String adminAddress;
    private final SeedUser user1;
    private final SeedUser user2;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      CategoryRepository categoryRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.enabled:false}") boolean enabled,
                      @Value("${app.seed.admin.email:admin@marketexchange.local}") String adminEmail,
                      @Value("${app.seed.admin.password:}") String adminPassword,
                      @Value("${app.seed.admin.firstname:Admin}") String adminFirstname,
                      @Value("${app.seed.admin.lastname:MarketExchange}") String adminLastname,
                      @Value("${app.seed.admin.phone:900000000}") String adminPhone,
                      @Value("${app.seed.admin.address:Direccion admin}") String adminAddress,
                      @Value("${app.seed.user1.email:cliente1@marketexchange.local}") String user1Email,
                      @Value("${app.seed.user1.password:Cliente12345}") String user1Password,
                      @Value("${app.seed.user1.firstname:Cliente}") String user1Firstname,
                      @Value("${app.seed.user1.lastname:Uno}") String user1Lastname,
                      @Value("${app.seed.user1.phone:900000001}") String user1Phone,
                      @Value("${app.seed.user1.address:Direccion cliente uno}") String user1Address,
                      @Value("${app.seed.user2.email:cliente2@marketexchange.local}") String user2Email,
                      @Value("${app.seed.user2.password:Cliente12345}") String user2Password,
                      @Value("${app.seed.user2.firstname:Cliente}") String user2Firstname,
                      @Value("${app.seed.user2.lastname:Dos}") String user2Lastname,
                      @Value("${app.seed.user2.phone:900000002}") String user2Phone,
                      @Value("${app.seed.user2.address:Direccion cliente dos}") String user2Address) {
        this.usuarioRepository = usuarioRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminFirstname = adminFirstname;
        this.adminLastname = adminLastname;
        this.adminPhone = adminPhone;
        this.adminAddress = adminAddress;
        this.user1 = new SeedUser(user1Email, user1Password, user1Firstname, user1Lastname, user1Phone, user1Address, Role.USER);
        this.user2 = new SeedUser(user2Email, user2Password, user2Firstname, user2Lastname, user2Phone, user2Address, Role.USER);
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        seedAdmin();
        seedUser(user1);
        seedUser(user2);
        seedCategories();
    }

    private void seedAdmin() {
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("SEED_ADMIN_PASSWORD es requerido cuando app.seed.enabled=true");
        }

        seedUser(new SeedUser(adminEmail, adminPassword, adminFirstname, adminLastname, adminPhone, adminAddress, Role.ADMIN));
    }

    private void seedUser(SeedUser seed) {
        if (seed.password() == null || seed.password().isBlank()) {
            throw new IllegalStateException("La contrasena seed para " + seed.email() + " es requerida");
        }

        usuarioRepository.findByEmail(seed.email()).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setFirstname(seed.firstname());
            usuario.setLastname(seed.lastname());
            usuario.setEmail(seed.email());
            usuario.setPhone(seed.phone());
            usuario.setAddress(seed.address());
            usuario.setPassword(passwordEncoder.encode(seed.password()));
            usuario.setRole(seed.role());
            usuario.setEmailVerified(true);
            usuario.setEmailVerifiedAt(java.time.LocalDateTime.now());
            return usuarioRepository.save(usuario);
        });
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<CategorySeed> seeds = List.of(
                new CategorySeed("Tecnologia", "Celulares, computadoras, accesorios y electronica."),
                new CategorySeed("Hogar", "Objetos para casa, cocina, decoracion y muebles."),
                new CategorySeed("Ropa", "Prendas, calzado y accesorios personales."),
                new CategorySeed("Libros", "Libros, comics, revistas y material educativo."),
                new CategorySeed("Deportes", "Equipos deportivos, bicicletas y accesorios."),
                new CategorySeed("Juguetes", "Juguetes, juegos de mesa y articulos infantiles."),
                new CategorySeed("Herramientas", "Herramientas, equipos y articulos de taller."),
                new CategorySeed("Otros", "Objetos que no encajan en una categoria especifica.")
        );

        for (CategorySeed seed : seeds) {
            Category category = new Category();
            category.setName(seed.name());
            category.setDescription(seed.description());
            categoryRepository.save(category);
        }
    }

    private record CategorySeed(String name, String description) {
    }

    private record SeedUser(String email,
                            String password,
                            String firstname,
                            String lastname,
                            String phone,
                            String address,
                            Role role) {
    }
}
