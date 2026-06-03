package com.minimarket.config;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create roles if missing
        Rol adminRole = rolRepository.findByNombre("ADMIN").orElseGet(() -> rolRepository.save(createRole("ADMIN")));
        Rol empleadoRole = rolRepository.findByNombre("EMPLEADO").orElseGet(() -> rolRepository.save(createRole("EMPLEADO")));
        Rol clienteRole = rolRepository.findByNombre("CLIENTE").orElseGet(() -> rolRepository.save(createRole("CLIENTE")));

        // Create default admin user if missing
        Optional<Usuario> adminOpt = usuarioRepository.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            Set<Rol> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            usuarioRepository.save(admin);
            System.out.println("Default admin user created: username=admin password=admin123");
        }
    }

    private Rol createRole(String nombre) {
        Rol rol = new Rol();
        rol.setNombre(nombre);
        return rol;
    }
}
