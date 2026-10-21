
package com.example.demo.config;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InicializarUsuarios {

    @Bean
    public CommandLineRunner crearUsuariosIniciales(
            UsuarioRepository repository,
            PasswordEncoder encoder) {

        return args -> {
            crearUsuarioSiNoExiste(
                    repository, encoder,
                    System.getenv("ADMIN_USERNAME"),
                    System.getenv("ADMIN_PASSWORD"),
                    "ADMIN"
            );

            crearUsuarioSiNoExiste(
                    repository, encoder,
                    System.getenv("USER_USERNAME"),
                    System.getenv("USER_PASSWORD"),
                    "USER"
            );
        };
    }


private void crearUsuarioSiNoExiste(
        UsuarioRepository repository,
        PasswordEncoder encoder,
        String username,
        String password,
        String rol) {

    if (username == null || username.isBlank()
            || password == null || password.isBlank()) {
        throw new IllegalStateException(
                "Configura las variables de entorno de los usuarios iniciales");
    }

    var existente = repository.findByUsername(username);

    if (existente.isPresent()) {
        Usuario usuario = existente.get();

        if ("consulta".equals(username)) {
            usuario.setPassword(encoder.encode(password));
            repository.save(usuario);
            System.out.println("Contraseña de consulta actualizada.");
        }

        return;
    }

    Usuario usuario = new Usuario();
    usuario.setUsername(username);
    usuario.setPassword(encoder.encode(password));
    usuario.setRol(rol);

    repository.save(usuario);

    System.out.println(
            "Usuario inicial creado: " + username
            + " con rol " + rol);
}

}
