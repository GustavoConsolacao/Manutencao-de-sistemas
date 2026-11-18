package com.example.SIST25.Chamado_Emergencia;

import com.example.SIST25.Chamado_Emergencia.enums.Perfil;
import com.example.SIST25.Chamado_Emergencia.models.Unidade;
import com.example.SIST25.Chamado_Emergencia.models.Usuario;
import com.example.SIST25.Chamado_Emergencia.repositories.UnidadeRepository;
import com.example.SIST25.Chamado_Emergencia.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;
    private final String unitId;

    public AdminInitializer(
            UsuarioRepository usuarioRepository,
            UnidadeRepository unidadeRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password,
            @Value("${app.admin.nome:Administrador}") String name,
            @Value("${app.admin.unidade-id:}") String unitId
    ) {
        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.name = name;
        this.unitId = unitId;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) {
            logger.info("Bootstrap de administrador desativado; configure ADMIN_EMAIL e ADMIN_PASSWORD para ativá-lo.");
            return;
        }
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Configure ADMIN_EMAIL e ADMIN_PASSWORD juntos.");
        }
        if (password.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD deve ter pelo menos 12 caracteres.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        usuarioRepository.findByEmail(normalizedEmail).ifPresentOrElse(usuario -> {
            if (usuario.getPerfil() != Perfil.ADMINISTRADOR) {
                throw new IllegalStateException(
                        "ADMIN_EMAIL já pertence a uma conta que não é administradora."
                );
            }
            logger.info("A conta administradora configurada já existe.");
        }, () -> criarAdministrador(normalizedEmail));
    }

    private void criarAdministrador(String email) {
        Unidade unidade = obterUnidadeAtiva();

        Usuario administrador = new Usuario();
        administrador.setNome(name.isBlank() ? "Administrador" : name.trim());
        administrador.setEmail(email);
        administrador.setSenha(passwordEncoder.encode(password));
        administrador.setPerfil(Perfil.ADMINISTRADOR);
        administrador.setUnidade(unidade);
        administrador.setAtivo(true);

        usuarioRepository.save(administrador);
        logger.info("Conta administradora inicial criada.");
    }

    private Unidade obterUnidadeAtiva() {
        if (!unitId.isBlank()) {
            long id;
            try {
                id = Long.parseLong(unitId.trim());
            } catch (NumberFormatException exception) {
                throw new IllegalStateException("ADMIN_UNIDADE_ID deve ser um número válido.", exception);
            }

            return unidadeRepository.findById(id)
                    .filter(unidade -> Boolean.TRUE.equals(unidade.getAtivo()))
                    .orElseThrow(() -> new IllegalStateException(
                            "ADMIN_UNIDADE_ID não corresponde a uma unidade ativa."
                    ));
        }

        return unidadeRepository.findAll().stream()
                .filter(unidade -> Boolean.TRUE.equals(unidade.getAtivo()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Cadastre uma unidade ativa ou configure ADMIN_UNIDADE_ID para criar o administrador."
                ));
    }
}
