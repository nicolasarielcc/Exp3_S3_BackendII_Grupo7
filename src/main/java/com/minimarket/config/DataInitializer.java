package com.minimarket.config;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
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
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoRepository productoRepository,
                           PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create roles if missing
        Rol adminRole = rolRepository.findByNombre("ADMIN").orElseGet(() -> rolRepository.save(createRole("ADMIN")));
        Rol empleadoRole = rolRepository.findByNombre("EMPLEADO").orElseGet(() -> rolRepository.save(createRole("EMPLEADO")));
        Rol clienteRole = rolRepository.findByNombre("CLIENTE").orElseGet(() -> rolRepository.save(createRole("CLIENTE")));

        ensureUser("admin", "admin123", adminRole);
        ensureUser("empleado", "empleado123", empleadoRole);
        ensureUser("cliente", "cliente123", clienteRole);

        Categoria lacteos = ensureCategoria("Lácteos");
        Categoria bebidas = ensureCategoria("Bebidas");
        Categoria limpieza = ensureCategoria("Limpieza");

        ensureProducto("Leche", 1200.0, 50, lacteos);
        ensureProducto("Yogurt", 1500.0, 30, lacteos);
        ensureProducto("Agua Mineral", 900.0, 100, bebidas);
        ensureProducto("Jabón Multiuso", 2500.0, 20, limpieza);

        System.out.println("Seed loaded: roles, users, categories and products");
    }

    private Rol createRole(String nombre) {
        Rol rol = new Rol();
        rol.setNombre(nombre);
        return rol;
    }

    private Usuario ensureUser(String username, String rawPassword, Rol role) {
        Optional<Usuario> userOpt = usuarioRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(rawPassword));
        Set<Rol> roles = new HashSet<>();
        roles.add(role);
        usuario.setRoles(roles);
        return usuarioRepository.save(usuario);
    }

    private Categoria ensureCategoria(String nombre) {
        return categoriaRepository.findAll().stream()
                .filter(categoria -> nombre.equals(categoria.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(nombre);
                    return categoriaRepository.save(categoria);
                });
    }

    private Producto ensureProducto(String nombre, Double precio, Integer stock, Categoria categoria) {
        return productoRepository.findAll().stream()
                .filter(producto -> nombre.equals(producto.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Producto producto = new Producto();
                    producto.setNombre(nombre);
                    producto.setPrecio(precio);
                    producto.setStock(stock);
                    producto.setCategoria(categoria);
                    return productoRepository.save(producto);
                });
    }
}
