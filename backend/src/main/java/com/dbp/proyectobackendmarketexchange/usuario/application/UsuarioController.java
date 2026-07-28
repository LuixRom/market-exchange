package com.dbp.proyectobackendmarketexchange.usuario.application;

import com.dbp.proyectobackendmarketexchange.usuario.domain.UsuarioService;
import com.dbp.proyectobackendmarketexchange.usuario.dto.UsuarioRequestDto;
import com.dbp.proyectobackendmarketexchange.usuario.dto.UsuarioResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> obtenerUsuarioPorId(@PathVariable Long id) {
        UsuarioResponseDto usuario = usuarioService.buscarUsuarioPorId(id);
        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }


    @GetMapping("/listar")
    public ResponseEntity<List<UsuarioResponseDto>> listarUsuarios() {
        List<UsuarioResponseDto> usuarios = usuarioService.listarUsuarios();
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }


    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequestDto requestDTO) {
        UsuarioResponseDto usuarioActualizado = usuarioService.actualizarUsuario(id, requestDTO);
        return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDto> getMyInfo() {
        UsuarioResponseDto usuarioInfo = usuarioService.getUsuarioOwnInfo();
        return ResponseEntity.ok(usuarioInfo);
    }
}